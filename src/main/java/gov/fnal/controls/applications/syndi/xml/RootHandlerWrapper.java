// (c) 2001-2010 Fermi Research Allaince
// $Id: RootHandlerWrapper.java,v 1.2 2010/09/15 15:55:51 apetrov Exp $
package gov.fnal.controls.applications.syndi.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:55:51 $
 */
public class RootHandlerWrapper extends NestedHandler {

    private final String rootTag;
    private final NestedHandler rootHandler;

    private NestedHandler subHandler;

    public RootHandlerWrapper( String rootTag, NestedHandler rootHandler ) {
        if (rootTag == null) {
            throw new NullPointerException();
        }
        if (rootHandler == null) {
            throw new NullPointerException();
        }
        this.rootTag = rootTag;
        this.rootHandler = rootHandler;
    }

    @Override
    public void startElement( String uri, String name,  String qName, Attributes atts )
            throws SAXException {
        if (subHandler != null) {
            subHandler.startElement( uri, name, qName, atts );
        } else if (rootTag.equals( name )) {
            subHandler = rootHandler;
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
