//  (c) 2009 Fermi Research Alliance
//  $Id: SVGPathIterator.java,v 1.1 2009/07/27 21:03:01 apetrov Exp $
package gov.fnal.controls.tools.svg;

import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

class SVGPathIterator implements PathIterator {

    private static final String commands = "MmZzLlHhVvCcSsQqTtAa";

    private static final int[] points = {
        2, //M
        0, //Z
        2, //L
        1, //H
        1, //V
        6, //C
        4, //S
        4, //Q
        2, //T
        7  //A
    };

    private static int[] segments = {
        SEG_MOVETO,
        SEG_CLOSE,
        SEG_LINETO,
        Integer.MAX_VALUE, //H
        Integer.MAX_VALUE, //V
        SEG_CUBICTO,
        Integer.MAX_VALUE, //S
        SEG_QUADTO,
        Integer.MAX_VALUE, //T
        Integer.MAX_VALUE //A
    };

    private List<Segment> data = new ArrayList<Segment>();
    private int index = 0;

    SVGPathIterator( String str ) {
        if (str == null) {
            throw new NullPointerException();
        }
        parse( str );
    }

    @Override
    public int getWindingRule() {
        return PathIterator.WIND_EVEN_ODD;
    }

    @Override
    public boolean isDone() {
        return index >= data.size();
    }

    @Override
    public void next() {
        if (isDone()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        index++;
        if (isDone()) {
            return;
        }
        Segment s = data.get( index );
        if ((s.getCmd() == 'a') || (s.getCmd() == 'A')) {
            next();
        }
    }

    @Override
    public int currentSegment( float[] coords ) {
        if (isDone()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        Segment s = data.get( index );
        int n = points[s.getCmdIndex() / 2];
        float[] crd = s.getData();
        for (int i = 0; i < n; i++) {
            coords[i] = crd[i];
        }
        return segments[s.getCmdIndex() / 2];
    }

    @Override
    public int currentSegment( double[] coords ) {
        if (isDone()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        Segment s = data.get( index );
        int n = points[s.getCmdIndex() / 2];
        float[] crd = s.getData();
        for (int i = 0; i < n; i++) {
            coords[i] = (double)crd[i];
        }
        return segments[s.getCmdIndex() / 2];
    }

    private void parse( String str ) {
        String buf = "";
        Segment s = null;
        char c;
        for (int i = 0; i < str.length(); i++) {
            c = str.charAt( i );
            if (isNumber( c )) {
                buf = buf + c;
            } else {
                if (buf.length() > 0) {
                    if (s != null) {
                        s.add( new Float( buf ).floatValue() );
                    } else {
                        throw new IllegalArgumentException( "Command required" );
                    }
                    buf = "";
                }
                if (isCommand( c )) {
                    if (s != null) {
                        data.add( s );
                    }
                    s = new Segment( c );
                }
            }
        }
        if (s != null) {
            if (buf.length() > 0) {
                s.add( new Float( buf ).floatValue() );
            }
            data.add( s );
        }

        float x = 0f;
        float y = 0f;
        float xx, yy;
        float x0, y0;
        boolean relative;

        Segment s1;

        for (int i = 0; i < data.size(); i++) {
            x0 = x;
            y0 = y;
            s = data.get( i );
            if (s.dataIndex != points[s.cmdIndex / 2]) {
                throw new IllegalArgumentException( "Illegal number of arguments for '" + s.getCmd() + "' command" );
            }
            if ((s.getCmd() == 'a') || (s.getCmd() == 'A')) {
                continue;
            }
            relative = (s.getCmdIndex() % 2) == 1;
            for (int j = 0; j < s.getDataIndex(); j++) {
                if (relative) {
                    if (((j % 2) == 0) && (s.getCmd() != 'v') && (s.getCmd() != 'V')) {
                        s.getData()[j] = s.getData()[j] + x;
                    } else {
                        s.getData()[j] = s.getData()[j] + y;
                    }
                }
                if (((j % 2) == 0) && (s.getCmd() != 'v') && (s.getCmd() != 'V')) {
                    x = s.getData()[j];
                } else {
                    y = s.getData()[j];
                }
            }
            if (relative) {
                s.setCmd( commands.charAt( s.getCmdIndex() - 1 ) );
            }
            float[] d = s.getData();
            switch (s.getCmd()) {
                case 'H':
                    s.setCmd( 'L' );
                    d[ 1] = y;
                    s.setDataIndex( 2 );
                    break;
                case 'V':
                    s.setCmd( 'L' );
                    d[ 1] = d[ 0];
                    d[ 0] = x;
                    s.setDataIndex( 2 );
                    break;
                case 'S':
                    s.setCmd( 'C' );
                    xx = x0;
                    yy = y0;
                    if (i > 0) {
                        s1 = data.get( i - 1 );
                        if (s1.getCmd() == 'C') {
                            xx = 2.0f * x0 - s1.getData()[ 2];
                            yy = 2.0f * y0 - s1.getData()[ 3];
                        }
                        d[ 5 ] = d[ 3 ];
                        d[ 4 ] = d[ 2 ];
                        d[ 3 ] = d[ 1 ];
                        d[ 2 ] = d[ 0 ];
                        d[ 1 ] = yy;
                        d[ 0 ] = xx;
                    }
                    s.setDataIndex( 6 );
                    break;
                case 'T':
                    s.setCmd( 'Q' );
                    xx = x0;
                    yy = y0;
                    if (i > 0) {
                        s1 = data.get( i - 1 );
                        if (s1.getCmd() == 'Q') {
                            xx = 2.0f * x0 - s1.getData()[ 0 ];
                            yy = 2.0f * y0 - s1.getData()[ 1 ];
                        }
                        d[ 3 ] = d[ 1 ];
                        d[ 2 ] = d[ 0 ];
                        d[ 1 ] = yy;
                        d[ 0 ] = xx;
                    }
                    s.setDataIndex( 4 );
                    break;
            }
        }

    }

    private boolean isCommand( char c ) {
        return ((c >= 'A') && (c <= 'Z') && (c != 'E')) ||
                ((c >= 'a') && (c <= 'z') && (c != 'e'));
    }

    private boolean isNumber( char c ) {
        return ((c >= '0') && (c <= '9')) ||
                (c == '+') ||
                (c == '-') ||
                (c == '.') ||
                (c == 'e') ||
                (c == 'E');
    }

    private static class Segment {

        private char cmd;
        private float[] data = new float[7];
        private int dataIndex = 0;
        private int cmdIndex;

        Segment( char cmd ) {
            setCmd( cmd );
        }

        char getCmd() {
            return cmd;
        }

        int getCmdIndex() {
            return cmdIndex;
        }

        void setCmd( char cmd ) {
            int j = commands.indexOf( cmd );
            if (j < 0) {
                throw new IllegalArgumentException( "Command '" + cmd + "' invalid" );
            }
            this.cmd = cmd;
            cmdIndex = j;
        }

        float[] getData() {
            return data;
        }

        int getDataIndex() {
            return dataIndex;
        }

        void setDataIndex( int val ) {
            dataIndex = val;
        }

        void add( float val ) {
            if (dataIndex < points[cmdIndex / 2]) {
                data[dataIndex] = val;
                dataIndex++;
            } else {
                throw new IllegalArgumentException( "Illegal number of arguments for '" + cmd + "' command" );
            }
        }

        @Override
        public String toString() {
            String res = "" + cmd;
            for (int i = 0; i < dataIndex; i++) {
                res = res + " " + data[i];
            }
            return res;
        }
    }
}
