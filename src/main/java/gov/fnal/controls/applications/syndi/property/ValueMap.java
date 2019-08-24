//  (c) 2001-2010 Fermi Research Alliance
//  $Id: ValueMap.java,v 1.2 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public class ValueMap {

    public static final ValueMap EMPTY_INSTANCE = new ValueMap();

    private static final Pattern KEY_VALUE_P = Pattern.compile( "\\s*(.+?)\\s*=\\s*(.*?)\\s*" );

    private final Map<Double,String> map = new TreeMap<Double,String>( new Tolerator( Double.MIN_NORMAL ));

    private String str;

    public ValueMap() throws IllegalArgumentException {
        this( null );
    }

    public ValueMap( String str ) throws IllegalArgumentException {
        if (str != null) {
            for (StringTokenizer z = new StringTokenizer( str, "," ); z.hasMoreElements();) {
                String token = z.nextToken();
                Matcher m = KEY_VALUE_P.matcher( token );
                if (!m.matches()) {
                    throw new IllegalArgumentException( "Illegal token: " + token );
                }
                Double value = new Double( m.group( 1 ));
                map.put( value, m.group( 2 ));
            }
        }
    }

    public SortedMap<Double,String> createMap( double tolerance ) {
        SortedMap<Double,String> res = new TreeMap<Double,String>( new Tolerator( tolerance ));
        res.putAll( map );
        return res;
    }
    
    @Override
    public String toString() {
        if (str == null) {
            StringBuilder buf = new StringBuilder();
            for (Entry<Double,String> e : map.entrySet()) {
                if (buf.length() > 0) {
                    buf.append( ',' );
                }
                buf.append( e.getKey());
                buf.append( '=' );
                buf.append( e.getValue());
            }
            str = buf.toString();
        }
        return str;
    }

}
