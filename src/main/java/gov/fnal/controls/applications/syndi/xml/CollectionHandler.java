// (c) 2001-2010 Fermi Research Allaince
// $Id: CollectionHandler.java,v 1.2 2010/09/15 15:55:51 apetrov Exp $
package gov.fnal.controls.applications.syndi.xml;

import java.util.Collection;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:55:51 $
 */
public class CollectionHandler extends NestedHandler {

    private final Collection<String> col;
    private final String entryTag;

    private TextHandler entryHandler;

    public CollectionHandler( Collection<String> col, String entryTag ) {
        if (col == null) {
            throw new NullPointerException();
        }
        if (entryTag == null) {
            throw new NullPointerException();
        }
        this.col = col;
        this.entryTag = entryTag;
    }

    @Override
    public void startElement( String uri, String name,  String qName, Attributes atts )
            throws SAXException {
        if (entryHandler == null && entryTag.equals( name )) {
            entryHandler = new TextHandler();
        } else {
            throw new SAXException( "Illegal element: " + name );
        }
    }

    @Override
    public void endElement( String uri, String name, String qName ) throws SAXException {
        String value = entryHandler.getText();
        if (value != null) {
            col.add( value );
        }
        entryHandler = null;
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
