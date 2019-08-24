//  (c) 2001-2010 Fermi Research Alliance
//  $Id: SynopticConfig.java,v 1.4 2010/09/23 15:04:01 apetrov Exp $
package gov.fnal.controls.applications.syndi;

import gov.fnal.controls.applications.syndi.xml.CollectionHandler;
import gov.fnal.controls.applications.syndi.xml.MapHandler;
import gov.fnal.controls.applications.syndi.xml.NestedHandler;
import gov.fnal.controls.applications.syndi.xml.RootHandlerWrapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/23 15:04:01 $
 */
public class SynopticConfig {

    public static final String DISPLAY_NS   = "http://synoptic.fnal.gov/2008/display";
    public static final String CONFIG_NS    = "http://synoptic.fnal.gov/2010/config";

    private static final String CONFIG_TAG      = "configuration";
    private static final String PROPS_TAG       = "properties";
    private static final String PROP_MAP_TAG    = "property-map";
    private static final String COMP_MAP_TAG    = "component-map";
    private static final String COMP_PATHS_TAG  = "component-paths";
    private static final String ENTRY_TAG       = "entry";
    private static final String ENTRY_KEY_TAG   = "key";
    private static final String ENTRY_VAL_TAG   = "value";
    
    private static final SynopticConfig instance = new SynopticConfig();

    private static final Logger log = Logger.getLogger( SynopticConfig.class.getName());
    
    public static SynopticConfig getInstance() {
        return instance;
    }

    private final SAXParser parser;
    private final Set<String> overloadedPropNames = new HashSet<String>();
    private final Map<String,String> propMap = new HashMap<String,String>();
    private final Map<String,String> compMap = new HashMap<String,String>();
    private final Set<String> compPaths = new HashSet<String>();

    private SynopticConfig() {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware( true );
        try {
            parser = spf.newSAXParser();
	} catch (Exception ex) {
	    throw new Error( ex );
	}
    }

    public void load( URL url ) throws IOException {
        if (url == null) {
            throw new NullPointerException();
        }
        InputStream inp = url.openStream();
        try {
            ConfigHandler handler = new ConfigHandler();
            parser.parse( inp, new RootHandlerWrapper( CONFIG_TAG, handler ));
            apply( handler );
            log.config( "Loaded configuration " + url );
        } catch (SAXException ex) {
            throw new IOException( "Cannot load " + url, ex );
        } finally {
            inp.close();
        }
    }

    public static Map<String,String> getPropertyMap() {
        return Collections.unmodifiableMap( instance.propMap );
    }

    public static Map<String,String> getComponentMap() {
        return Collections.unmodifiableMap( instance.compMap );
    }

    public static Set<String> getComponentPaths() {
        return Collections.unmodifiableSet( instance.compPaths );
    }

    private void apply( ConfigHandler handler ) {
        Properties props = System.getProperties();
        for (Entry<String,String> e : handler.props.entrySet()) {
            String name = e.getKey();
            if (props.containsKey( name ) && !overloadedPropNames.contains( name )) {
                continue;
            }
            overloadedPropNames.add( name );
            String value = e.getValue();
            if (value == null) {
                props.remove( name );
            } else {
                props.setProperty( name, value );
            }
        }
        propMap.putAll( handler.propMap );
        compMap.putAll( handler.compMap );
        compPaths.addAll( handler.compPaths );
    }

    private static class ConfigHandler extends NestedHandler {
        
        final Map<String,String> props = new HashMap<String,String>();
        final Map<String,String> compMap = new HashMap<String,String>();
        final Map<String,String> propMap = new HashMap<String,String>();
        final Set<String> compPaths = new HashSet<String>();

        private NestedHandler subHandler, propsHandler,
                propMapHandler, compMapHandler, compPathsHandler;

        ConfigHandler() {}

        @Override
        public void startElement( String uri, String name,  String qName, Attributes atts )
                throws SAXException {
            if (subHandler != null) {
                subHandler.startElement( uri, name, qName, atts );
            } else if (PROPS_TAG.equals( name ) && propsHandler == null) {
                subHandler = propsHandler =  new MapHandlerImpl( props );
            } else if (PROP_MAP_TAG.equals( name ) && propMapHandler == null) {
                subHandler = propMapHandler =  new MapHandlerImpl( propMap );
            } else if (COMP_MAP_TAG.equals( name ) && compMapHandler == null) {
                subHandler = compMapHandler =  new MapHandlerImpl( compMap );
            } else if (COMP_PATHS_TAG.equals( name ) && compPathsHandler == null) {
                subHandler = compPathsHandler =  new CollectionHandlerImpl( compPaths );
            } else {
                throw new SAXException( "Illegal element: " + name );
            }
        }

        @Override
        public void endElement( String uri, String name, String qName )
                throws SAXException {
            if (subHandler.isOpen()) {
                subHandler.endElement( uri, name, qName );
            } else {
                subHandler = null;
            }
        }

        @Override
        public void characters( char[] str, int offset, int length )
                throws SAXException {
            if (subHandler != null) {
                subHandler.characters( str, offset, length );
            }
        }

        @Override
        public boolean isOpen() {
            return subHandler != null;
        }

    }

    private static class MapHandlerImpl extends MapHandler {

        MapHandlerImpl( Map<String,String> map ) {
            super( map, ENTRY_TAG, ENTRY_KEY_TAG, ENTRY_VAL_TAG );
        }

    }

    private static class CollectionHandlerImpl extends CollectionHandler {

        CollectionHandlerImpl( Collection<String> map ) {
            super( map, ENTRY_TAG );
        }

    }

}
