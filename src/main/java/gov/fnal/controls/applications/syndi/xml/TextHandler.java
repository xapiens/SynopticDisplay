// (c) 2001-2010 Fermi Research Allaince
// $Id: TextHandler.java,v 1.2 2010/09/15 15:55:51 apetrov Exp $
package gov.fnal.controls.applications.syndi.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:55:51 $
 */
public class TextHandler extends NestedHandler {

    private final StringBuilder buf = new StringBuilder();

    public TextHandler() {}

    @Override
    public void startElement( String uri, String name,  String qName, Attributes atts )
            throws SAXException {
        throw new SAXException( "Illegal element: " + name );
    }

    @Override
    public void characters( char[] str, int offset, int length ) throws SAXException {
        buf.append( str, offset, length );
    }

    public String getText() {
        String res = buf.toString().trim();
        return res.isEmpty() ? null : res;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

}
