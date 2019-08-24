//  (c) 2009 Fermi Research Alliance
//  $Id: JarIterator.java,v 1.4 2010/08/30 16:05:08 apetrov Exp $
package gov.fnal.controls.tools.resource;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/08/30 16:05:08 $
 */

class JarIterator extends AbstractResourceIterator<URL> {

    private final Pattern pattern;
    private final boolean recursive;
    private final String urlPrefix, namePrefix;
    private final int namePrefixLength;
    private final Enumeration<JarEntry> entries;

    JarIterator( URL url, Pattern pattern, boolean recursive ) throws IOException {
        this.pattern = pattern;
        this.recursive = recursive;
        JarURLConnection con = (JarURLConnection)url.openConnection();
        JarFile file = con.getJarFile();
        urlPrefix = "jar:file:" + file.getName() + "!/";
        namePrefix = con.getJarEntry().getName() + "/";
        namePrefixLength = namePrefix.length();
        entries = file.entries();
        prefetch();
    }

    @Override
    protected URL fetch() {
        while (entries.hasMoreElements()) {
            JarEntry e = entries.nextElement();
            if (isDirectory( e )) {
                continue;
            }
            String fullName = e.getName();
            if (fullName.startsWith( namePrefix )
                    && (recursive || !inSubdirectory( e ))
                    && matchesPattern( e )) {
                try {
                    return new URL( urlPrefix + fullName );
                } catch (MalformedURLException ex) {
                    throw new RuntimeException( ex );
                }
            }
        }
        return null;
    }

    private boolean isDirectory( JarEntry entry ) {
        String s = entry.getName();
        return s.endsWith( "/" );
    }

    private boolean inSubdirectory( JarEntry entry ) {
        String s = entry.getName();
        return s.indexOf( '/', namePrefixLength + 1 ) != -1;
    }

    private boolean matchesPattern( JarEntry entry ) {
        if (pattern == null) {
            return true;
        }
        String name = getShortName( entry );
        return pattern.matcher( name ).matches();
    }

    private String getShortName( JarEntry entry ) {
        String s = entry.getName();
        int i = s.lastIndexOf( "/", s.length() - 2 );
        return (i == -1) ? s : s.substring( i + 1 );
    }

}
