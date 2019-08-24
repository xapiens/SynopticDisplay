// (c) 2001-2010 Fermi Research Alliance
// $Id: DisplayAddress.java,v 1.2 2010/09/15 15:53:52 apetrov Exp $
package gov.fnal.controls.applications.syndi.repository;

import java.net.URI;
import java.net.URLDecoder;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:53:52 $
 */
public class DisplayAddress implements DisplayAddressSyntax {

    private static final Logger log = Logger.getLogger( DisplayAddress.class.getName());

    public static DisplayAddress parse( URI uri ) throws DisplayAddressSyntaxException {
        String str = uri.toString();
        String decStr;
        try {
            decStr = URLDecoder.decode( str, "UTF-8" );
        } catch (Exception ex) {
            log.log( Level.WARNING, "Cannot decode uri: " + str, ex );
            decStr = str;
        }
        return parse( decStr );
    }

    public static DisplayAddress parse( String str ) throws DisplayAddressSyntaxException {
        if (str == null) {
            throw new NullPointerException();
        }
        Matcher m = ADDRESS_PATTERN.matcher( str );
        if (!m.matches()) {
            throw new DisplayAddressSyntaxException( "Invalid display address" );
        }
        String schema = m.group( 1 );
        if (schema == null) {
            schema = DEFAULT_SCHEMA;
        }
        String path = m.group( 2 );
        String simpleName = m.group( 3 );
        String paramStr = m.group( 4 );
        DisplayParameters params = (paramStr != null)
                ? DisplayParameters.parse( paramStr )
                : new DisplayParameters();
        return new DisplayAddress( schema, path, simpleName, params );
    }

    public static DisplayAddress create(
            String schema,
            String path,
            Map<String,String> params ) throws IllegalArgumentException {
        if (path == null) {
            throw new NullPointerException();
        }
        Matcher m = PATH_PATTERN.matcher( path );
        if (!m.matches()) {
            throw new IllegalArgumentException( "Invalid path: " + schema );
        }
        String simpleName = m.group( 1 );
        if (schema == null) {
            schema = DEFAULT_SCHEMA;
        } else {
            if (!SCHEMA_PATTERN.matcher( schema ).matches()) {
                throw new IllegalArgumentException( "Invalid schema: " + schema );
            }
        }
        DisplayParameters pp = (params != null) 
                ? new DisplayParameters( params )
                : new DisplayParameters();
        return new DisplayAddress( schema, path, simpleName, pp );
    }

    private final String schema, path, simpleName;
    private final DisplayParameters params;

    private String stringValue;

    private DisplayAddress( 
            String schema,
            String path,
            String simpleName,
            DisplayParameters params ) {
        assert (schema != null);
        assert (path != null);
        assert (simpleName != null);
        assert (params != null);
        this.schema = schema;
        this.path = path;
        this.simpleName = simpleName;
        this.params = params;
    }

    public String getSchema() {
        return schema;
    }

    public String getPath() {
        return path;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public Map<String,String> getParams() {
        return new DisplayParameters( params );
    }

    private static String toString( DisplayAddress addr, StringBuilder buf ) {
        String schema = addr.getSchema();
        if (!DEFAULT_SCHEMA.equals( schema )) {
            buf.append( schema );
            buf.append( ':' );
        }
        buf.append( addr.getPath());
        Map<String,String> params = addr.getParams();
        if (!params.isEmpty()) {
            buf.append( '(' );
            DisplayParameters.toString( params, buf );
            buf.append( ')' );
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

    public URI toURI() {
        return URI.create( toString());
    }

}
