// (c) 2001-2010 Fermi Research Alliance
// $Id: DisplayURISource.java,v 1.4 2010/09/16 15:24:20 apetrov Exp $
package gov.fnal.controls.applications.syndi.repository;

import gov.fnal.controls.applications.syndi.builder.element.BuilderComponent;
import gov.fnal.controls.applications.syndi.builder.element.BuilderComponentLoader;
import gov.fnal.controls.applications.syndi.builder.element.GenericContainer;
import gov.fnal.controls.applications.syndi.runtime.DisplayFormatException;
import gov.fnal.controls.applications.syndi.runtime.RuntimeComponentLoader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import org.w3c.dom.Document;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/16 15:24:20 $
 */
public class DisplayURISource implements DisplaySource<URI> {

    private static final String REPOSITORY_URL;
    
    static {
        String repoRoot = System.getProperty( "Synoptic.repository-root" );
        REPOSITORY_URL = (repoRoot == null) ? null : repoRoot + "/repo";
    }

    private final URI uri;
    private final URL location;
    private final Set<URL> parents;
    private final DisplayAddress addr;

    private File localFile;

    public DisplayURISource( URI uri ) throws IllegalArgumentException {
        this( uri, null );
    }

    public DisplayURISource( URI uri, Set<URL> parents ) throws IllegalArgumentException {
        if (uri == null) {
            throw new NullPointerException();
        }
        this.uri = uri;
        this.addr = DisplayAddress.parse( uri );
        String schema = addr.getSchema();
        try {
            if ("repo".equals( schema )) {
                if (REPOSITORY_URL != null) {
                    location = new URL( REPOSITORY_URL + addr.getPath());
                } else {
                    location = null;
                }
            } else if ("file".equals( schema )) {
                location = new URL( "file:" + addr.getPath());
            } else {
                throw new IllegalArgumentException( "Schema not supported: " + schema );
            }
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException( "Invalid display URL", ex );
        }
        this.parents = (parents == null) ? new HashSet<URL>() : new HashSet<URL>( parents );
        if (location != null) {
            if (this.parents.contains( location )) {
                throw new IllegalArgumentException( "Cyclic reference" );
            }
            this.parents.add( location );
        }
    }

    @Override
    public URI getSource() {
        return uri;
    }

    @Override
    public String getSimpleName() {
        return addr.getSimpleName();
    }

    @Override
    public File getLocalFile() {
        if (localFile == null && "file".equals( uri.getScheme())) {
            localFile = new File( addr.getPath());
        }
        return localFile;
    }

    public URL getLocation() {
        return location;
    }

    @Override
    public Document loadDocument() throws Exception {
        if (location == null) {
            throw new Exception( "Repository root is not specified" );
        }
        InputStreamReader reader = new InputStreamReader( location.openStream(), "UTF-8" );
        try {
            Document doc = RuntimeComponentLoader.getInstance().load( reader );
            DisplayParametersApplicator.apply( addr.getParams(), doc );
            return doc;
        } finally {
            reader.close();
        }
    }

    @Override
    public GenericContainer createBuilderComponent() throws Exception {
        if (location == null) {
            throw new Exception( "Repository root is not specified" );
        }
        InputStreamReader reader = new InputStreamReader( location.openStream(), "UTF-8" );
        try {
            BuilderComponent comp = BuilderComponentLoader.getInstance().load( reader, parents );
            if (!(comp instanceof GenericContainer)) {
                throw new DisplayFormatException( "Invalid component type" );
            }
            return (GenericContainer)comp;
        } finally {
            reader.close();
        }
    }
    
    @Override
    public String toString() {
        return addr.getPath();
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }

    @Override
    public boolean equals( Object obj ) {
        return (obj instanceof DisplayURISource)
                && ((DisplayURISource)obj).uri.equals( uri );
    }

}
