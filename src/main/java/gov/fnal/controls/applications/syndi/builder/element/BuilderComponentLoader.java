// (c) 2001-2010 Fermi Research Allaince
// $Id: BuilderComponentLoader.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import java.io.Reader;
import java.net.URL;
import java.util.Set;
import java.util.logging.Logger;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public class BuilderComponentLoader {

    private static final Logger log = Logger.getLogger( BuilderComponentLoader.class.getName());

    private static final BuilderComponentLoader instance = new BuilderComponentLoader();
    
    public static BuilderComponentLoader getInstance() {
        return instance;
    }

    private final SAXParserFactory fac;

    private BuilderComponentLoader() {
        fac = SAXParserFactory.newInstance();
        fac.setNamespaceAware( true );
        fac.setValidating( false );
    }

    public BuilderComponent load( Reader reader ) throws Exception {
        return load( reader, null );
    }

    public synchronized BuilderComponent load( Reader reader, Set<URL> parents ) throws Exception {

        ComponentParser syntax = new ComponentParser( parents );
        SAXParser parser = fac.newSAXParser();
        parser.parse( new InputSource( reader ), syntax );

        int originAdjustmentCount = syntax.getOriginAdjustmentCount();
        if (originAdjustmentCount > 0) {
            log.info( "Adjusted origin of " + originAdjustmentCount + " SVG component(s)" );
        }
        int removedInvisibleCount = syntax.getRemovedInvisibleCount();
        if (removedInvisibleCount > 0) {
            log.info( "Removed " + removedInvisibleCount + " invisible SVG component(s)" );
        }

        return syntax.getResult();
        
    }

}
