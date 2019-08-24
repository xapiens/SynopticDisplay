//  (c) 2009 Fermi Research Alliance
//  $Id: SVGSyntaxHandler.java,v 1.2 2009/07/30 22:30:24 apetrov Exp $
package gov.fnal.controls.tools.svg;

import java.util.Deque;
import java.util.LinkedList;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class SVGSyntaxHandler extends DefaultHandler implements SVGNamespace {

    private static final SVGElementFactory factory = new SVGElementFactory();

    private final Deque<SVGElement> elements = new LinkedList<SVGElement>();
    private final Deque<StringBuilder> texts = new LinkedList<StringBuilder>();
    private final boolean namespaceAware;

    private SVGComponent root;

    public SVGSyntaxHandler() {
        this( true );
    }

    public SVGSyntaxHandler( boolean namespaceAware ) {
        this.namespaceAware = namespaceAware;
    }

    public void reset() {
        elements.clear();
        texts.clear();
        root = null;
    }

    public SVGComponent getResult() {
        return root;
    }

    @Override
    public void startElement( String uri, String localName, String qName, Attributes attr ) {
        SVGElement e = null;
        if ((!namespaceAware || XMLNS_SVG.equals( uri ))
                && (elements.isEmpty() || elements.peekLast() != null)) {
            e = factory.createComponent( localName );
        }
        if (e != null) {
            e.applyAttributes( attr );
        }
        elements.addLast( e );
        texts.addLast( null );
    }

    @Override
    public void endElement( String uri, String localName, String qName ) {
        SVGElement e = elements.removeLast();
        StringBuilder buf = texts.removeLast();
        if (e != null && buf != null) {
            String str = buf.toString().trim();
            if (str.length() > 0) {
                e.setText( str );
            }
        }
        if (e != null) {
            SVGElement parent = elements.peekLast();
            if (parent != null) {
                parent.add( e );
            }
        }
        if (elements.isEmpty() && (e instanceof SVGComponent)) {
            root = (SVGComponent)e;
        }
    }

    @Override
    public void characters( char ch[], int start, int length ) {
        String str = new String( ch ).substring( start, start + length );
        StringBuilder buf = texts.peekLast();
        if (buf == null) {
            buf = new StringBuilder();
            texts.removeLast();
            texts.addLast( buf );
        }
        buf.append( str );
    }

}
