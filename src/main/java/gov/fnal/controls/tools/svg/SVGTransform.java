//  (c) 2009 Fermi Research Alliance
//  $Id: SVGTransform.java,v 1.1 2009/07/27 21:03:01 apetrov Exp $
package gov.fnal.controls.tools.svg;

import java.awt.geom.AffineTransform;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SVGTransform extends AffineTransform {

    private static final Pattern P_TRANSFORM = Pattern.compile(
        "^(matrix|translate|scale|rotate)\\s*\\(\\s*(.*)\\s*\\)\\s*$"
    );

    public static AffineTransform parseTransform( String str ) throws IllegalArgumentException {

        if (str == null) {
            throw new NullPointerException();
        }

        Matcher m = P_TRANSFORM.matcher( str );
        if (!m.matches()) {
            throw new IllegalArgumentException();
        }

        String[] ss = m.group( 2 ).split( "\\s+,\\s*|,\\s*" );
        float[] args = new float[ ss.length ];
        for (int i = 0; i < ss.length; i++) {
            args[ i ] = Float.parseFloat( ss[ i ]);
        }
        
        String name = m.group( 1 );
        if ("matrix".equals( name )) {
            if (args.length != 6) {
                throw new IllegalArgumentException();
            }
            return new AffineTransform( args );
        } else if ("translate".equals( name )) {
            if (args.length != 1 && args.length != 2) {
                throw new IllegalArgumentException();
            }
            return AffineTransform.getTranslateInstance(
                args[ 0 ],
                args.length == 1 ? args[ 0 ] : args[ 1 ]
            );
        } else if ("scale".equals( name )) {
            if (args.length != 1 && args.length != 2) {
                throw new IllegalArgumentException();
            }
            return AffineTransform.getScaleInstance(
                args[ 0 ],
                args.length == 1 ? args[ 0 ] : args[ 1 ]
            );
        } else if ("rotate".equals( name )) {
            if (args.length != 1 && args.length != 3) {
                throw new IllegalArgumentException();
            }
            return AffineTransform.getRotateInstance(
                args[ 0 ],
                args.length == 1 ? 0 : args[ 1 ],
                args.length == 1 ? 0 : args[ 2 ]
            );
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static String toString( AffineTransform xform ) {
        double[] matrix = new double[ 6 ];
        xform.getMatrix( matrix );
        StringBuilder buf = new StringBuilder( "matrix(" );
        for (int i = 0; i < 6; i++) {
            buf.append( SVGNumber.toString( matrix[ i ]));
            if (i != 5) {
                buf.append( ',' );
            }
        }
        buf.append( ")" );
        return buf.toString();
    }

    private SVGTransform() {}

}
