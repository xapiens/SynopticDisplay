// (c) 2001-2010 Fermi Research Allaince
// $Id: MapEntryHandler.java,v 1.2 2010/09/15 15:55:51 apetrov Exp $
package gov.fnal.controls.applications.syndi.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:55:51 $
 */
class MapEntryHandler extends NestedHandler {

    private final String keyTag, valueTag;

    private TextHandler keyHandler, valueHandler;
    private NestedHandler subHandler;

    MapEntryHandler( String keyTag, String valueTag ) {
        assert keyTag != null;
        assert valueTag != null;
        this.keyTag = keyTag;
        this.valueTag = valueTag;
    }

    @Override
    public void startElement( String uri, String name,  String qName, Attributes atts )
            throws SAXException {
        if (subHandler == null && keyTag.equals( name )) {
            if (keyHandler != null) {
                throw new SAXException( "Duplicate element: " + name );
            }
            subHandler = keyHandler = new TextHandler();
        } else if (subHandler == null && valueTag.equals( name )) {
            if (valueHandler != null) {
                throw new SAXException( "Duplicate element: " + name );
            }
            subHandler = valueHandler = new TextHandler();
        } else {
            throw new SAXException( "Illegal element: " + name );
        }
    }

    @Override
    public void endElement( String uri, String name, String qName ) throws SAXException {
        subHandler = null;
    }

    @Override
    public void characters( char[] str, int offset, int length ) throws SAXException {
        if (subHandler != null) {
            subHandler.characters( str, offset, length );
        }
    }

    @Override
    public boolean isOpen() {
        return subHandler != null;
    }

    public String getKey() {
        return (keyHandler == null) ? null : keyHandler.getText();
    }

    public String getValue() {
        return (valueHandler == null) ? null : valueHandler.getText();
    }

}
