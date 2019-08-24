//  (c) 2010 Fermi Research Alliance
//  $Id: DirIterator.java,v 1.2 2010/08/30 16:05:08 apetrov Exp $
package gov.fnal.controls.tools.resource;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Pattern;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/08/30 16:05:08 $
 */

class DirIterator extends AbstractResourceIterator<URL> {

    private final Pattern pattern;
    private final boolean recursive;
    private final Queue<File> items = new LinkedList<File>();

    private Iterator<URL> delegate;

    DirIterator( URL url, Pattern pattern, boolean recursive ) {
        this( new File( url.getFile()), pattern, recursive );
    }

    DirIterator( File dir, Pattern pattern, boolean recursive ) {
        this.pattern = pattern;
        this.recursive = recursive;
        File[] files = dir.listFiles();
        if (files != null) {
            items.addAll( Arrays.asList( files ));
        }
        prefetch();
    }

    @Override
    protected URL fetch() {
        do {
            if (delegate != null && delegate.hasNext()) {
                return delegate.next();
            }
            if (items.isEmpty()) {
                return null;
            }
            File f = items.poll();
            if (f.isDirectory()) {
                if (recursive) {
                    delegate = new DirIterator( f, pattern, true );
                }
            } else if (matchesPattern( f )) {
                try {
                    return f.toURI().toURL();
                } catch (MalformedURLException ex) {
                    throw new RuntimeException( ex );
                }
            }
        } while (true);
    }

    private boolean matchesPattern( File file ) {
        if (pattern == null) {
            return true;
        }
        String name = getShortName( file );
        return pattern.matcher( name ).matches();
    }

    private String getShortName( File file ) {
        return file.getName();
    }

}
