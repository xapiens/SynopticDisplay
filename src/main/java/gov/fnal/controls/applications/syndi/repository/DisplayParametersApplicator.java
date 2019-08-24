// (c) 2001-2010 Fermi Research Alliance
// $Id: DisplayParametersApplicator.java,v 1.2 2010/09/15 15:53:51 apetrov Exp $
package gov.fnal.controls.applications.syndi.repository;

import gov.fnal.controls.applications.syndi.SynopticConfig;
import gov.fnal.controls.tools.svg.SVGNamespace;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:53:51 $
 */
public class DisplayParametersApplicator {

    private static final Logger log = Logger.getLogger( DisplayParametersApplicator.class.getName());

    public static void apply( Map<String,String> params, Document doc ) {
        if (params == null || doc == null) {
            throw new NullPointerException();
        }
        DisplayParametersApplicator a = new DisplayParametersApplicator( params );
        a.applyToDocument( doc );
    }
    
    private final Map<String,String> params;
    private final Set<String> missing, redundant;

    private DisplayParametersApplicator( Map<String,String> params ) {
        this.params = params;
        redundant = new HashSet<String>( params.keySet());
        missing = new HashSet<String>();
    }

    private String getParameter( String name ) {
        String res = params.get( name );
        if (res == null) {
            missing.add( name );
        } else {
            redundant.remove( name );
        }
        return res;
    }

    private boolean isElement( Element e ) {
        return SynopticConfig.DISPLAY_NS.equals( e.getNamespaceURI())
                && "element".equals( e.getLocalName());
    }

    private boolean isProperty( Element e ) {
        return SynopticConfig.DISPLAY_NS.equals( e.getNamespaceURI())
                && "property".equals( e.getLocalName())
                && "true".equals( e.getAttribute( "global" ));
    }

    private boolean isURI( Element e ) {
        return SynopticConfig.DISPLAY_NS.equals( e.getNamespaceURI())
                && "property".equals( e.getLocalName())
                && "uri".equals( e.getAttribute( "type" ))
                && !"true".equals( e.getAttribute( "global" ));
    }

    private boolean isSVG( Element e ) {
        return SVGNamespace.XMLNS_SVG.equals( e.getNamespaceURI())
                && "svg".equals( e.getLocalName());
    }

    private void applyTo( Element e ) {
        if (isElement( e )) {
            applyToElement( e );
        } else if (isProperty( e )) {
            applyToProperty( e );
        } else if (isURI( e )) {
            applyToURI( e );
        } else if (isSVG( e )) {
            applyToSVG( e );
        }
    }

    private void applyToDocument( Document doc ) {
        applyTo( doc.getDocumentElement());
        if (!redundant.isEmpty()) {
            log.info( "Redundant global properties: " + toString( redundant ) + "." );
        }
        if (!missing.isEmpty()) {
            log.info( "Missing global properties: " + toString( missing ) + "." );
        }
    }

    private void applyToElement( Element e_element ) {
        for (Node n = e_element.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                applyTo( (Element)n );
            }
        }
    }

    private void applyToProperty( Element e_property ) {
        String globalName = e_property.getAttribute( "global-name" );
        if ("".equals( globalName )) {
            return;
        }
        String value = getParameter( globalName );
        if (value != null) {
            e_property.setAttribute( "value", value );
        }
    }

    private void applyToURI( Element e_uri ) {
        String value0 = e_uri.getAttribute( "value" );
        StringBuffer buf = new StringBuffer();
        Matcher m = DisplayAddressSyntax.URL_PARAM_PATTERN.matcher( value0 );
        while (m.find()) {
            String globalName = m.group( 1 );
            String value = getParameter( globalName );
            if (value != null) {
                m.appendReplacement( buf, value );
            } else {
                m.appendReplacement( buf, "" );
            }
        }
        m.appendTail( buf );
        String value1 = buf.toString();
        e_uri.setAttribute( "value", value1 );
    }

    private void applyToSVG( Element e_svg ) {
        NodeList props = e_svg.getElementsByTagNameNS( SynopticConfig.DISPLAY_NS, "property" );
        if (props.getLength() == 0) {
            return;
        }
        Element e_graph = getGraphicalElement( e_svg );
        if (e_graph == null) {
            log.severe( "SVG graphical element not found" );
            return;
        }
        for (int i = 0; i < props.getLength(); ++i) {
            Element e = (Element)props.item( i );
            String globalName = e.getAttribute( "global-name" );
            if ("".equals( globalName )) {
                continue;
            }
            String value = getParameter( globalName );
            if (value == null) {
                continue;
            }
            String name = e.getAttribute( "name" );
            if ("text".equals( name )) {
                NodeList list = e_graph.getChildNodes();
                for (int j = 0; j < list.getLength(); ++j) {
                    Node t = list.item( j );
                    if (t.getNodeType() == Node.TEXT_NODE) {
                        e_graph.removeChild( t );
                    }
                }
                Node t = e_graph.getOwnerDocument().createTextNode( value );
                e_graph.appendChild( t );
            } else if (!"".equals( name )) {
                e_graph.setAttribute( name, value );
            }
        }
    }

    private Element getGraphicalElement( Element e_svg ) {
        for (Node n = e_svg.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() != Node.ELEMENT_NODE
                    || !SVGNamespace.XMLNS_SVG.equals( n.getNamespaceURI())) {
                continue;
            }
            String tag = ((Element)n).getLocalName();
            if ("g".equals( tag ) || "text".equals( tag ) || "path".equals( tag )) {
                return (Element)n;
            }
        }
        return null;
    }

    private static String toString( Set<String> set ) {
        StringBuilder buf = new StringBuilder();
        for (String s : set) {
            if (buf.length() > 0) {
                buf.append( ", " );
            }
            buf.append( s );
        }
        return buf.toString();
    }

}
