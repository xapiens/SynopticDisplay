//  (c) 2010 Fermi Research Alliance
//  $Id: ResourceIterator.java,v 1.2 2010/08/30 16:05:08 apetrov Exp $
package gov.fnal.controls.tools.resource;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/08/30 16:05:08 $
 */

public class ResourceIterator extends AbstractResourceIterator<URL> {

    private final boolean recursive;
    private final Pattern pattern;
    private final String normDir;
    private final Enumeration<URL> resources;

    private Iterator<URL> delegate;

    public ResourceIterator( String directory ) throws IOException {
        this( directory, null );
    }

    public ResourceIterator( String directory, Pattern pattern ) throws IOException {
        if (directory == null) {
            throw new NullPointerException();
        }
        this.pattern = pattern;
        String dir = stripSlashes( directory );
        recursive = dir.endsWith( "*" );
        dir = recursive ? stripAsterisk( dir ) : dir;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        resources = loader.getResources( dir );
        normDir = dir;
        prefetch();
    }

    public String getNormalizedDirectory() {
        return normDir;
    }

    @Override
    protected URL fetch() {
        do {
            if (delegate != null && delegate.hasNext()) {
                return delegate.next();
            }
            if (!resources.hasMoreElements()) {
                return null;
            }
            URL url = resources.nextElement();
            String proto = url.getProtocol();
            try {
                if ("file".equals( proto )) {
                    delegate = new DirIterator( url, pattern, recursive );
                } else if ("jar".equals(  proto )) {
                    delegate = new JarIterator( url, pattern, recursive );
                }
            } catch (IOException ex) {
                throw new RuntimeException( ex );
            }
        } while (true);
    }

    private static String stripSlashes( String str ) {
        int i1 = str.length() - 1;
        while (i1 >= 0 && isSlash( str.charAt( i1 ))) {
            --i1;
        }
        if (i1 == -1) {
            return "";
        }
        int i0 = 0;
        while (i0 < i1 && isSlash( str.charAt( i0 ))) {
            ++i0;
        }
        return str.substring( i0, i1 + 1 );
    }

    private static String stripAsterisk( String str ) {
        int i1 = str.length() - 1;
        while (i1 >= 0 && isAsteriskOrSlash( str.charAt( i1 ))) {
            --i1;
        }
        if (i1 == -1) {
            return "";
        }
        return str.substring( 0, i1 + 1 );
    }

    private static boolean isSlash( char c ) {
        return (c == '/');
    }

    private static boolean isAsteriskOrSlash( char c ) {
        return (c == '*') || isSlash( c );
    }

}
