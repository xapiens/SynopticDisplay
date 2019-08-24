// (c) 2001-2010 Fermi Research Alliance
// $Id: DisplayParameters.java,v 1.2 2010/09/15 15:53:52 apetrov Exp $
package gov.fnal.controls.applications.syndi.repository;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:53:52 $
 */
public class DisplayParameters extends LinkedHashMap<String,String> implements DisplayAddressSyntax {

    private static final Logger log = Logger.getLogger( DisplayParameters.class.getName());

    public static DisplayParameters parse( String str ) throws DisplayAddressSyntaxException {
        if (str == null) {
            throw new NullPointerException();
        }
        if (!PARAM_PATTERN.matcher( str ).matches()) {
            throw new DisplayAddressSyntaxException( "Invalid display parameters" );
        }
        DisplayParameters params = new DisplayParameters();
        Matcher m = KEYVAL_PATTERN.matcher( str );
        while (m.find()) {
            params.put( m.group( 1 ), m.group( 2 ));
        }
        return params;
    }

    private String stringValue;

    DisplayParameters() {}

    DisplayParameters( Map<String,String> params ) {
        for (Entry<String,String> e : params.entrySet()) {
            put( e.getKey(), e.getValue());
        }
    }

    @Override
    public String put( String key, String value ) throws IllegalArgumentException {
        if (key == null) {
            throw new NullPointerException();
        }
        if (!NAME_PATTERN.matcher( key ).matches()) {
            throw new IllegalArgumentException( "Invalid parameter name: " + key );
        }
        if (value != null) {
            value = value.trim();
        }
        String decValue;
        if (value == null || value.isEmpty()) {
            decValue = null;
        } else {
            try {
                decValue = URLDecoder.decode( value, "UTF-8" );
            } catch (Exception ex) {
                log.log( Level.WARNING, "Cannot decode parameter value: " + value, ex );
                decValue = value;
            }
        }
        return super.put( key, decValue );
    }

    static String toString( Map<String,String> params, StringBuilder buf ) {
        boolean first = true;
        for (Entry<String,String> e : params.entrySet()) {
            if (first) {
                first = false;
            } else {
                buf.append( ',' );
            }
            buf.append( e.getKey());
            buf.append( '=' );
            String value = e.getValue();
            String encValue;
            if (value == null) {
                encValue = "";
            } else {
                try {
                    encValue = URLEncoder.encode( value, "UTF-8" );
                } catch (UnsupportedEncodingException ex) {
                    log.log( Level.WARNING, "Cannot encode parameter value: " + value, ex );
                    encValue = value;
                }
            }
            buf.append( encValue );
        }
        return buf.toString();
    }

    @Override
    public synchronized String toString() {
        if (stringValue == null) {
            stringValue = toString( this, new StringBuilder());
        }
        return stringValue;
    }

}
