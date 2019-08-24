// (c) 2008 Fermi Research Alliance
// $Id: SVGColor.java,v 1.4 2009/09/24 21:22:58 apetrov Exp $
package gov.fnal.controls.tools.svg;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SVGColor extends Color {

    public static final Color NO_COLOR = new SVGColor();
    
    private static final Map<String,Color> COLORS = new TreeMap<String,Color>();

    private static final Pattern P_HEX_SHORT = Pattern.compile(
        "^#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])$"
    );
    private static final Pattern P_HEX_LONG = Pattern.compile(
        "^#([0-9a-fA-F]{2})([0-9a-fA-F]{2})([0-9a-fA-F]{2})$"
    );
    private static final Pattern P_DEC_ABSOLUTE = Pattern.compile(
        "^rgb\\s*\\(\\s*([0-9]{1,3})\\s*,\\s*([0-9]{1,3})\\s*,\\s*([0-9]{1,3})\\s*\\)$"
    );
    private static final Pattern P_DEC_PERCENT = Pattern.compile(
        "^rgb\\s*\\(\\s*([0-9]{1,3})\\s*%\\s*,\\s*([0-9]{1,3})\\s*%\\s*,\\s*([0-9]{1,3})\\s*%\\s*\\)$"
    );
    
    static {
        String fileName = SVGColor.class.getSimpleName() + ".properties";
        try {
            URL url = SVGColor.class.getResource( fileName );
            if (url == null) {
                throw new FileNotFoundException( fileName );
            }
            Pattern p = Pattern.compile( "^\\s*([^#].*)\\s*=\\s*(\\d+),(\\d+),(\\d+)\\s*$" );
            BufferedReader r = new BufferedReader( new InputStreamReader( url.openStream()));
            try {
                String s;
                while ((s = r.readLine()) != null) {
                    Matcher m = p.matcher( s );
                    if (!m.matches()) {
                        continue;
                    }
                    String name = m.group( 1 );
                    Color color = new SVGColor(
                        Integer.parseInt( m.group( 2 )),
                        Integer.parseInt( m.group( 3 )),
                        Integer.parseInt( m.group( 4 )),
                        name
                    );
                    COLORS.put( name, color );
                }
            } finally {
                r.close();
            }
        } catch (Exception ex) {
            throw new RuntimeException( "Can't initialize color map", ex );
        }
    }

    public static Color parseColor( String str ) throws IllegalArgumentException {

        if (str == null) {
            throw new NullPointerException();
        }
        
        if ("none".equals( str )) {
            return NO_COLOR;
        }
        
        if (COLORS.containsKey( str )) {
            return COLORS.get( str );
        }
        
        Matcher m = P_HEX_LONG.matcher( str );
        if (m.matches()) {
            return new Color(
                Integer.parseInt( m.group( 1 ), 16 ),
                Integer.parseInt( m.group( 2 ), 16 ),
                Integer.parseInt( m.group( 3 ), 16 )
            );
        }
        
        m = P_HEX_SHORT.matcher( str );
        if (m.matches()) {
            return new Color(
                Integer.parseInt( m.group( 1 ) + m.group( 1 ), 16 ),
                Integer.parseInt( m.group( 2 ) + m.group( 2 ), 16 ),
                Integer.parseInt( m.group( 3 ) + m.group( 3 ), 16 )
            );
        }

        m = P_DEC_ABSOLUTE.matcher( str );
        if (m.matches()) {
            return new Color(
                Integer.parseInt( m.group( 1 )),
                Integer.parseInt( m.group( 2 )),
                Integer.parseInt( m.group( 3 ))
            );
        }

        m = P_DEC_PERCENT.matcher( str );
        if (m.matches()) {
            return new Color(
                Math.round( Float.parseFloat( m.group( 1 )) * 2.55f ),
                Math.round( Float.parseFloat( m.group( 2 )) * 2.55f ),
                Math.round( Float.parseFloat( m.group( 3 )) * 2.55f )
            );
        }
        
        throw new IllegalArgumentException( "Illegal color: " + str );
    }
    
    public static Collection<Color> getStandardColors() {
        return new ArrayList<Color>( COLORS.values());
    }
    
    private final String name;

    private SVGColor() {
        super( 0, 0, 0, 0 );
        this.name = "none";
    }

    private SVGColor( int r, int g, int b, String name ) {
        super( r, g, b );
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static String toString( Color color ) {
        if (color == null) {
            return "none";
        }
        if (color instanceof SVGColor) {
            return color.toString();
        }
        return String.format( "#%02X%02X%02X",
            color.getRed(),
            color.getGreen(),
            color.getBlue()
        );
    }

}