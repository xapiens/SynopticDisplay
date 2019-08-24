// (c) 2001-2010 Fermi Research Alliance
// $Id: FileRepositoryAccess.java,v 1.3 2010/09/15 15:53:52 apetrov Exp $
package gov.fnal.controls.applications.syndi.repository;

import gov.fnal.controls.applications.syndi.runtime.TimedDocument;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.SimpleTimeZone;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Implementation of the display repository in a file system.
 *  
 * @author  Andrey Petrov
 * @version $Revision: 1.3 $
 */
public class FileRepositoryAccess implements RepositoryAccess {
    
    private static final String REPO_ROOT = System.getProperty( "Synoptic.file-repository-root" );

    static {
        if (REPO_ROOT == null) {
            throw new Error( "Repository root is not specified" );
        }
    }

    private static final int maxDirDepth = 16;

    private static final boolean includeHidden = false;

    private static final boolean includeEmptyDirs = false;

    private static boolean isHidden( String name ) {
        return name.startsWith( "_" );
    }

    private static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    static {
        dbf.setNamespaceAware( true );
        dbf.setValidating( false );
    }
    
    private final DateFormat dformat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSSZ" );
    private final FileFilter filter = new FileFilter();
    private final FileComparator comp = new FileComparator();
    private final DocumentBuilder builder;
    private final File root;
    
    public FileRepositoryAccess() {
        dformat.setTimeZone( new SimpleTimeZone( 0, "Z" ));
        try {
            builder = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new Error( ex );
        }
        root = new File( REPO_ROOT.replace( "/", File.separator ));
    }
    
    @Override
    public boolean isDisplay( String path ) throws IOException {
        File f = new File( root, toFilePath( path + ".xml" ));
        return filter.accept( f ) && f.isFile();
    }
    
    @Override
    public boolean isDirectory( String path ) throws IOException {
        File f = new File( root, toFilePath( path ));
        return filter.accept( f ) && f.isDirectory();
    }
    
    @Override
    public TimedDocument load( String path, long since ) throws IOException {
        File file = new File( root, toFilePath( path ));
        if (file.isDirectory()) {
            if (!filter.accept( file )) {
                throw new FileNotFoundException( "Directory Not Found: " + path );
            }
            Document doc = builder.newDocument();
            append( file, doc, doc, 0 );
            return new TimedDocument( doc, System.currentTimeMillis());
        } else {
            file = new File( root, toFilePath( path + ".xml" ));
            if (!filter.accept( file )) {
                throw new FileNotFoundException( "Display Not Found: " + path );
            }
            long modTime = file.lastModified();
            if (modTime <= since) {
                return null;
            }
            try {
                Document doc = builder.parse( file );
                return new TimedDocument( doc, file.lastModified());
            } catch (SAXException ex) {
                throw new IOException( "Display Display Format", ex );
            }
        }
    }

    @Override
    public TimedDocument load( String path ) throws IOException {
        TimedDocument doc = load( path, -1 );
        if (doc == null) {
            throw new IOException( "Cannot Load Display" );
        }
        return doc;
    }
    
    private boolean append( File file, Document doc, Node node, int level ) {
        if (level >= maxDirDepth) {
            return false;
        } else if (file.isDirectory()) {
            return appendDirectory( file, doc, node, level );
        } else {
            return appendDisplay( file, doc, node );
        }
    }

    private boolean appendDirectory( File dir, Document doc, Node node, int level ) {

        String name = dir.getName();

        if (level == 0) {
            Element e = doc.createElement( "repository" );
            String p0 = root.getPath();
            String p1 = dir.getPath();
            if (p1.startsWith( p0 )) {
                int i = p0.length();
                String context = p1.substring( i ).replace( File.separator, "/" );
                if (context.startsWith( "/" )) {
                    context = context.substring( 1 );
                }
                e.setAttribute( "context", context );
            }
            appendDirectoryContents( dir, doc, e, level + 1 );
            node.appendChild( e );
            return true;
        }

        if (includeHidden || !isHidden( name )) {
            Element e = doc.createElement( "directory" );
            e.setAttribute( "name", name );
            boolean hasChildren = appendDirectoryContents( dir, doc, e, level + 1 );
            if (hasChildren || includeEmptyDirs) {
                node.appendChild( e );
            }
            return hasChildren;
        }

        return false;

    }

    private boolean appendDirectoryContents( File dir, Document doc, Node node, int level ) {
        File[] children = dir.listFiles( filter );
        Arrays.sort( children, comp );
        boolean res = false;
        for (File f : children) {
            res |= append( f, doc, node, level );
        }
        return res;
    }

    private boolean appendDisplay( File file, Document doc, Node node ) {

        String name = file.getName();
        if (!includeHidden && isHidden( name )) {
            return false;
        }

        int i = name.lastIndexOf( "." );
        if (i != -1) {
            name = name.substring( 0, i );
        }

        Element e = doc.createElement( "display" );
        e.setAttribute( "name", name );
        e.setAttribute( "modified", dformat.format( new Date( file.lastModified())));
        node.appendChild( e );
        return true;
        
    }
    
    private static String toFilePath( String path ) throws IOException {
        if (!path.startsWith( "/" )) {
            throw new IOException( "Illegal path" );
        }
        return path.replace( "/", File.separator );
    } 
    
    private class FileFilter implements java.io.FileFilter {
        
        @Override
        public boolean accept( File f ) {
            if (!f.canRead() || f.isHidden()) {
                return false;
            }
            String name = f.getName();
            if (f.isDirectory()) {
                return !"WEB-INF".equals( name ) && !"CVS".equals( name );
            } else {
                return name.endsWith( ".xml" );
            }
        }
        
    }
    
    private class FileComparator implements Comparator<File> {

        @Override
        public int compare( File f1, File f2 ) {
            boolean d1 = f1.isDirectory();
            boolean d2 = f2.isDirectory();
            if (d1 && !d2) {
                return Integer.MIN_VALUE;
            } else if (!d1 && d2) {
                return Integer.MAX_VALUE;
            } else {
                return f1.getName().compareToIgnoreCase( f2.getName());
            }
        }
        
    }

}
