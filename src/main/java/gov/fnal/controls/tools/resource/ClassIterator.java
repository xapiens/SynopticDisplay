//  (c) 2010 Fermi Research Alliance
//  $Id: ClassIterator.java,v 1.3 2010/08/30 16:05:08 apetrov Exp $
package gov.fnal.controls.tools.resource;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/08/30 16:05:08 $
 */

public class ClassIterator<T> extends AbstractResourceIterator<Class<? extends T>> {

    private static final Pattern CLASS_PATTERN = Pattern.compile( "^.*\\.class$" );

    private static final Logger log = Logger.getLogger( ClassIterator.class.getName());

    private final ResourceIterator delegate;
    private final String prefix;
    private final Class<? extends Annotation> anno;
    private final Class<T> type;

    public ClassIterator( String directory, Class<T> type ) throws IOException {
        this( directory, type, null );
    }
    
    public ClassIterator( String directory, Class<T> type,
            Class<? extends Annotation> anno ) throws IOException {
        if (type == null) {
            throw new NullPointerException();
        }
        this.type = type;
        this.anno = anno;
        delegate = new ResourceIterator( directory, CLASS_PATTERN );
        prefix = "/" + delegate.getNormalizedDirectory() + "/";
        prefetch();
    }

    @Override
    protected Class<? extends T> fetch() {
        while (delegate.hasNext()) {
            URL url = delegate.next();
            String className = getClassName( url );
            try {
                Class<?> res = Class.forName( className );
                if (type.isAssignableFrom( res ) &&
                        (anno == null || res.isAnnotationPresent( anno ))) {
                    return res.asSubclass( type );
                }
            } catch (Exception ex) {
                log.log( Level.WARNING, "Cannot create class " + className, ex );
            }
        }
        return null;
    }

    private String getClassName( URL url ) {
        String s = url.toString();
        int i = s.lastIndexOf( prefix );
        return s.substring( i + 1, s.length() - 6 ).replace( '/', '.' );
    }

}
