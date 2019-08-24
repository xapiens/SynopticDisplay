// (c) 2001-2010 Fermi Research Allaince
// $Id: MapHandler.java,v 1.2 2010/09/15 15:55:51 apetrov Exp $
package gov.fnal.controls.applications.syndi.xml;

import java.util.Map;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:55:51 $
 */
public class MapHandler extends NestedHandler {

    private final Map<String,String> map;
    private final String entryTag, keyTag, valueTag;

    private MapEntryHandler entryHandler;

    public MapHandler( Map<String,String> map,
            String entryTag, String keyTag, String valueTag ) {
        if (map == null) {
            throw new NullPointerException();
        }
        if (entryTag == null || keyTag == null || valueTag == null) {
            throw new NullPointerException();
        }
        this.map = map;
        this.entryTag = entryTag;
        this.keyTag = keyTag;
        this.valueTag = valueTag;
    }

    @Override
    public void startElement( String uri, String name,  String qName, Attributes atts )
            throws SAXException {
        if (entryHandler != null) {
            entryHandler.startElement( uri, name, qName, atts );
        } else if (entryTag.equals( name )) {
            entryHandler = new MapEntryHandler( keyTag, valueTag );
        } else {
            throw new SAXException( "Illegal element: " + name );
        }
    }

    @Override
    public void endElement( String uri, String name, String qName ) throws SAXException {
        if (entryHandler.isOpen()) {
            entryHandler.endElement( uri, name, qName );
        } else {
            String key = entryHandler.getKey();
            if (key == null) {
                throw new SAXException( "Empty entry key" );
            }
            map.put( key, entryHandler.getValue());
            entryHandler = null;
        }
    }

    @Override
    public void characters( char[] str, int offset, int length )
            throws SAXException {
        if (entryHandler != null) {
            entryHandler.characters( str, offset, length );
        }
    }

    @Override
    public boolean isOpen() {
        return entryHandler != null;
    }

}
