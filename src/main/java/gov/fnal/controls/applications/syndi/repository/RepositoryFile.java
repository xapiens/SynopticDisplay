// (c) 2001-2010 Fermi Research Alliance
// $Id: RepositoryFile.java,v 1.3 2010/09/15 15:53:52 apetrov Exp $
package gov.fnal.controls.applications.syndi.repository;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:53:52 $
 */
public class RepositoryFile extends File {

    private static final String REPOSITORY_URL;

    static {
        String repoRoot = System.getProperty( "Synoptic.repository-root" );
        REPOSITORY_URL = (repoRoot == null) ? null : repoRoot + "/display/.xml";
    }

    private static final Logger log = Logger.getLogger( RepositoryFile.class.getName());
    
    private static Element root;
    
    public synchronized static void invalidate() {
        root = null;
    }
    
    private synchronized static Element getRoot() {
        if (root == null) {
            try {
                loadRoot();
            } catch (IOException ex) {
                log.log( Level.SEVERE, "Cannot load repository tree", ex );
            }
        }
        return root;
    }

    private static void loadRoot() throws IOException {
        try {
            if (REPOSITORY_URL == null) {
                throw new Exception( "Repository root is not specified" );
            }
            DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            URLConnection con = new URL( REPOSITORY_URL ).openConnection();
            con.setConnectTimeout( 1000 );
            con.setReadTimeout( 5000 );
            con.connect();
            InputStream inp = con.getInputStream();
            try {
                Document doc = parser.parse( inp );
                root = doc.getDocumentElement();
                if (root == null) {
                    throw new Exception( "Empty repository" );
                } else {
                    cleanup( root, null );
                }
            } finally {
                inp.close();
            }
            log.info( "Repository tree loaded" );
        } catch (MalformedURLException ex) {
            throw new IOException( "Invalid repository URL: " + REPOSITORY_URL, ex );
        } catch (IOException ex) {
            throw new IOException( "Cannot read display list", ex );
        } catch (SAXException ex) {
            throw new IOException( "Cannot parse display list", ex );
        } catch (Exception ex) {
            throw new IOException( ex );
        }
    }
    
    private static void cleanup( Element e, String path ) {
        if ("repository".equals( e.getTagName())) {
            String ctx = e.getAttribute( "context" );
            if ("".equals( ctx )) {
                path = "";
            } else {
                path = "/" + ctx;
            }
        } else {
            path += "/" + e.getAttribute( "name" );
        }
        e.setUserData( "path", path, null );
        NodeList list = e.getChildNodes();
        for (int i = list.getLength() - 1; i >= 0; i--) {
            Node n = list.item( i );
            if (n instanceof Element) {
                Element e1 = (Element)n;
                cleanup( e1, path );
            } else {
                e.removeChild( n );
            }
        }
    }
    
    private final String name, path, parentPath;
    private final Element element;
    
    public RepositoryFile( URI uri ) {
        this( uri.toString());
    }

    public RepositoryFile( String path ) {
        super( "" );
        if (path == null) {
            throw new NullPointerException();
        }
        if (path.startsWith( "repo:" )) {
            path = path.substring( 5 );
        }
        String _name = "/";
        StringBuilder _path = new StringBuilder( "/" );
        int j = -1; // position of the last slash
        Element _element = getRoot();
        for (String s : path.split( "/" )) {
            if ("".equals( s )) {
                continue;
            }
            if (j != -1) {
                j = _path.length();
                _path.append( "/" );
            } else {
                j = _path.length();
            }
            _path.append( s );
            _name = s;
            if (_element != null) {
                NodeList nodes = _element.getChildNodes();
                _element = null;
                for (int i = 0, n = nodes.getLength(); i < n; i++) {
                    Node e = nodes.item( i );
                    if ((e instanceof Element) && isGoodElement( (Element)e )
                            && s.equals( ((Element)e).getAttribute( "name" ))) {
                        _element = (Element)e;
                        break;
                    }
                }
            }
        }
        this.name = _name;
        this.path = _path.toString();
        this.parentPath = (j == -1) ? null : _path.substring( 0, j );
        //log.fine( "name=" + name + ";path=" + path + ";parent=" + parentPath );
        this.element = _element;
    }

    public RepositoryFile( String parent, String child ) {
        this( parent == null ? child : parent + "/" + child );
    }

    public RepositoryFile( File parent, String child ) {
        this( parent == null ? null : parent.getAbsolutePath(), child );
    }
    
    private RepositoryFile( RepositoryFile parent, Element child ) {
        super( "" );
        this.parentPath = parent.getPath();
        this.name = child.getAttribute( "name" );
        this.path = parentPath.endsWith( "/" )
                ? parentPath + name
                : parentPath + "/" + name;
        this.element = child;
    }
    
    private static boolean isGoodElement( Element e ) {
        String tag = e.getTagName();
        if (!"repository".equals( tag ) && !"directory".equals( tag ) 
                && !"display".equals( tag )) {
            return false;
        }
        String name = e.getAttribute( "name" );
        if (name == null || "".equals( name ) || name.indexOf( "/" ) != -1) {
            return false;
        }
        return true;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getParent() {
        return parentPath;
    }
    
    @Override
    public File getParentFile() {
        return (parentPath == null) ? null : new RepositoryFile( parentPath );
    }
    
    @Override
    public String getPath() {
        return path;
    }
    
    @Override
    public boolean isAbsolute() {
        return parentPath == null;
        //return true;
    }
    
    @Override
    public String getAbsolutePath() {
        return path;
    }
    
    @Override
    public File getAbsoluteFile() {
        return this;
    }

    @Override
    public String getCanonicalPath() {
        return path;
    }
    
    @Override
    public File getCanonicalFile() {
        return this;
    }
    
    @Deprecated
    @Override
    public URL toURL() throws MalformedURLException {
        return toURI().toURL();
    }
    
    @Override
    public URI toURI() {
        try {
            return new URI( "repo", null, path, null );
        } catch (URISyntaxException ex) {
            throw new RuntimeException( ex );
        }
    }
    
    @Override
    public boolean canRead() {
        return exists();
    }

    @Override
    public boolean canWrite() {
        return false;
    }

    @Override
    public boolean exists() {
        return element != null;
    }

    @Override
    public boolean isDirectory() {
        if (element == null) {
            return false;
        }
        String tag = element.getTagName();
        return "repository".equals( tag ) || "directory".equals( tag );    
    }

    @Override
    public boolean isFile() {
        if (element == null) {
            return false;
        }
        String tag = element.getTagName();
        return "display".equals( tag );    
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public long lastModified() {
        return -1;
    }

    @Override
    public long length() {
        return -1;
    }

    @Override
    public boolean createNewFile() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean delete() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void deleteOnExit() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] list() {
        return list( null );
    }

    @Override
    public String[] list( FilenameFilter filter ) {
        if (!isDirectory()) {
            return null;
        }
        List<String> res = new ArrayList<String>();
        NodeList nodes = element.getChildNodes();
        for (int i = 0, n = nodes.getLength(); i < n; i++) {
            Node e = nodes.item( i );
            if (e instanceof Element) {
                String _name = ((Element)e).getAttribute( "name" );
                if (_name != null && 
                        (filter == null || filter.accept( this, _name ))) {
                    res.add( _name );
                }
            }
        }
        return res.toArray( new String[ res.size()]);
    }
    
    @Override
    public File[] listFiles() {
        return listFiles( (FilenameFilter)null );
    }

    @Override
    public File[] listFiles( FilenameFilter filter ) {
        if (!isDirectory()) {
            return null;
        }
        List<File> res = new ArrayList<File>();
        NodeList nodes = element.getChildNodes();
        for (int i = 0, n = nodes.getLength(); i < n; i++) {
            Node e = nodes.item( i );
            if (e instanceof Element && isGoodElement( (Element)e )) {
                String _name = ((Element)e).getAttribute( "name" );
                if (filter == null || filter.accept( this, _name )) {
                    res.add( new RepositoryFile( this, (Element)e ));
                }
            }
        }
        return res.toArray( new File[ res.size()]);
    }
    
    @Override
    public File[] listFiles( FileFilter filter ) {
        if (!isDirectory()) {
            return null;
        }
        List<File> res = new ArrayList<File>();
        NodeList nodes = element.getChildNodes();
        for (int i = 0, n = nodes.getLength(); i < n; i++) {
            Node e = nodes.item( i );
            if (e instanceof Element && isGoodElement( (Element)e )) {
                File f = new RepositoryFile( this, (Element)e );
                if (filter == null || filter.accept( f )) {
                    res.add( f );
                }
            }
        }
        return res.toArray( new File[ res.size()]);
    }

    @Override
    public boolean mkdir() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean renameTo( File dest ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean setLastModified( long time ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean setReadOnly() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean setWritable( boolean writable, boolean ownerOnly ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean setReadable( boolean readable, boolean ownerOnly ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean setExecutable( boolean executable, boolean ownerOnly ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canExecute() {
        return true;
    }

    public static File[] listRoots() {
        return new File[]{ new RepositoryFile( "" ) };
    }

    @Override
    public long getTotalSpace() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getFreeSpace() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getUsableSpace() {
        return Long.MAX_VALUE;
    }

    public static File createTempFile( String prefix, String suffix, File directory ) {
        throw new UnsupportedOperationException();
    }
    
    public static File createTempFile( String prefix, String suffix ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo( File f ) {
        return path.compareTo( f.getAbsolutePath());
    }

    @Override
    public boolean equals( Object o ) {
        return (o instanceof RepositoryFile) 
                && ((RepositoryFile)o).path.equals( path );
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public String toString() {
        return path;
    }
    
}
