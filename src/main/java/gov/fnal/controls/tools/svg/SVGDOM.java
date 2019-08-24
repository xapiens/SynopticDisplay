//  (c) 2010 Fermi Research Alliance
//  $Id: SVGDOM.java,v 1.3 2010/02/17 21:58:01 apetrov Exp $
package gov.fnal.controls.tools.svg;

import java.awt.BasicStroke;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/02/17 21:58:01 $
 */

class SVGDOM implements SVGNamespace {

    private static final String STYLE =
        "text{font-family:'Bitstream Vera Sans Mono','DejaVu Sans Mono'," +
            "'Lucida Sans Typewriter','Andale Mono',monospace;}";

    private static final String SCRIPT =
        "function show(id){" +
            "var e = document.getElementById(id);" +
            "e.setAttribute(\"stroke\",e.getAttributeNS(\"" + XMLNS_SVGX + "\",\"stroke\"));" +
        "}" +
        "function hide(id){ " +
            "document.getElementById(id).setAttribute(\"stroke\",\"none\"); " +
        "} ";

    private static final DocumentBuilder DOC_BUILDER;

    static {
        try {
            DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
            fac.setNamespaceAware( true );
            DOC_BUILDER = fac.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException( ex );
        }
    }

    private final Document doc;
    private final Element root;

    private int id;
    private Element group, anchor, element;
    private Stroke stroke;
    private AffineTransform xform;
    private SVGAnchor link;
    private String toolTip;
    private boolean scriptSet;

    SVGDOM( Rectangle viewPort ) {

        doc = DOC_BUILDER.newDocument();

        root = doc.createElementNS( XMLNS_SVG, "svg" );
        doc.appendChild( root );

        root.setAttribute( "xmlns:xlink", XMLNS_XLINK );
        root.setAttribute( "xmlns:svgx", XMLNS_SVGX );
        //root.setAttribute( "xml:space", "preserve" );
        root.setAttribute( "width", "100%" );
        root.setAttribute( "height", "100%" );
        root.setAttribute( "stroke", "none" );
        root.setAttribute( "fill", "none" );
        //root.setAttribute( "shape-rendering", "crispEdges" );

        setStrokeAttributes( root, SVGComponent.DEFAULT_STROKE );

        if (viewPort != null) {
            String str = String.format( "%d %d %d %d",
                viewPort.x,
                viewPort.y,
                viewPort.width,
                viewPort.height
            );
            root.setAttribute( "viewBox", str );
        }

        Element defs = doc.createElement( "defs" );
        root.appendChild( defs );

        Element style = doc.createElement( "style" );
        style.setAttribute( "type", "text/css" );
        style.appendChild( doc.createCDATASection( STYLE ));
        defs.appendChild( style );

    }

    private void validateScript() {
        if (scriptSet) {
            return;
        }
        scriptSet = true;
        Element script = doc.createElement( "script" );
        script.setAttribute( "type",  "text/javascript" );
        script.appendChild( doc.createCDATASection( SCRIPT ));
        root.insertBefore( script, root.getFirstChild());
    }

    int nextId() {
        return id++;
    }

    Document getDocument() {
        return doc;
    }

    Element createElement( String tag, SVGGraphics g ) {

        boolean sameLink = (link == g.absoluteLink);

        if (!sameLink) {
            link = g.absoluteLink;
            if (link != null) {
                anchor = createElement( "a" );
                root.appendChild( anchor );
                setAnchorAttributes( anchor );
            } else {
                anchor = null;
            }
        }

        Element groupParent = (anchor == null) ? root : anchor;

        boolean sameGroup = sameLink
                         && ((stroke == null) ? g.stroke == null : stroke.equals( g.stroke ))
                         && ((xform == null) ? g.xform == null : xform.equals( g.xform ))
                         && ((toolTip == null) ? g.toolTip == null : toolTip.equals( g.toolTip ));

        if (!sameGroup) {
            stroke = g.stroke;
            xform = new AffineTransform( g.xform );
            toolTip = g.toolTip;
            group = null;
        } else if (group == null && element != null) {

            groupParent.removeChild( element );
            removeGroupAttributes( element );

            group = createElement( "g" );
            group.appendChild( element );
            groupParent.appendChild( group );
            setGroupAttributes( group );
        }

        element = createElement( tag );
        if (group == null) {
            setGroupAttributes( element );
            groupParent.appendChild( element );
        } else {
            group.appendChild( element );
        }

        return element;

    }

    void markHover() {
        if (anchor == null || element == null) {
            return;
        }
        validateScript();
        setHoverAttributes( anchor, element );
    }

    private Element createElement( String tag ) {
        Element res = doc.createElement( tag );
        res.setAttribute( "id", String.valueOf( nextId()));
        return res;
    }

    private void setGroupAttributes( Element e ) {
        setTransformAttributes( e, xform );
        setStrokeAttributes( e, stroke );
        setToolTipAttributes( e, toolTip );
    }

    private void removeGroupAttributes( Element e ) {
        removeTransformAttributes( e  );
        removeStrokeAttributes( e );
        removeToolTipAttributes( e );
    }

    private void setAnchorAttributes( Element e ) {
        setLinkAttributes( e, link );
    }

    private static void setTransformAttributes( Element e, AffineTransform xform ) {
        if (xform == null || xform.isIdentity()) {
            return;
        }
        e.setAttribute( "transform", SVGTransform.toString( xform ));
    }

    private static void removeTransformAttributes( Element e ) {
        e.removeAttribute( "transform" );
    }

    private static void setStrokeAttributes( Element e, Stroke stroke ) {

        if (!(stroke instanceof BasicStroke)) {
            return;
        }
        BasicStroke bs = (BasicStroke)stroke;

        if ("svg".equals( e.getTagName())
                || bs.getLineWidth() != SVGComponent.DEFAULT_STROKE.getLineWidth()) {
            e.setAttribute( "stroke-width", SVGNumber.toString( bs.getLineWidth()));
        }

        if ("svg".equals( e.getTagName())
                || bs.getEndCap() != SVGComponent.DEFAULT_STROKE.getEndCap()) {
            switch (bs.getEndCap()) {
                case BasicStroke.CAP_BUTT:
                    e.setAttribute( "stroke-linecap", "butt" );
                    break;
                case BasicStroke.CAP_ROUND:
                    e.setAttribute( "stroke-linecap", "round" );
                    break;
                case BasicStroke.CAP_SQUARE:
                    e.setAttribute( "stroke-linecap", "square" );
                    break;
            }
        }

        if ("svg".equals( e.getTagName())
                || bs.getLineJoin() != SVGComponent.DEFAULT_STROKE.getLineJoin()) {
            switch (bs.getLineJoin()) {
                case BasicStroke.JOIN_BEVEL:
                    e.setAttribute( "stroke-linejoin", "bevel" );
                    break;
                case BasicStroke.JOIN_MITER:
                    e.setAttribute( "stroke-linejoin", "miter" );
                    break;
                case BasicStroke.JOIN_ROUND:
                    e.setAttribute( "stroke-linejoin", "bevel" );
                    break;
            }
        }

        float[] dash = bs.getDashArray();
        if (dash != null && dash.length != 0) {
            StringBuilder buf = new StringBuilder();
            boolean first = false;
            for (double d : dash) {
                if (first) {
                    first = false;
                } else {
                    buf.append( ',' );
                }
                buf.append( SVGNumber.toString( d ));
            }
            e.setAttribute( "stroke-dasharray", buf.toString());
        }

    }

    private static void removeStrokeAttributes( Element e ) {
        e.removeAttribute( "stroke-width" );
        e.removeAttribute( "stroke-linecap" );
        e.removeAttribute( "stroke-linejoin" );
        e.removeAttribute( "stroke-dasharray" );
    }

    private static void setToolTipAttributes( Element e, String toolTip ) {
        if (toolTip == null) {
            return;
        }
        e.setAttribute( "title", toolTip );
    }

    private static void removeToolTipAttributes( Element e ) {
        e.removeAttribute( "title" );
    }

    private static void setLinkAttributes( Element e, SVGAnchor link ) {
        if (link == null) {
            return;
        }
        e.setAttributeNS( XMLNS_XLINK, "xlink:href", link.getURI().toString());
        e.setAttribute( "target", link.getTarget().toString()); // XLINK ?
        if (link.getTitle() != null) {
            e.setAttributeNS( XMLNS_XLINK, "xlink:title", link.getTitle());
        }
    }

    private static void setHoverAttributes( Element anchor, Element hover ) {
        String id = hover.getAttribute( "id" );
        if ("".equals( id )) {
            return;
        }
        anchor.setAttribute( "onmouseover", "show(" + id + ")" );
        anchor.setAttribute( "onmouseout", "hide(" + id + ")" );
        String stroke = hover.getAttribute( "stroke" );
        if ("".equals( stroke )) {
            stroke = "black";
        }
        hover.setAttribute( "stroke", "none" );
        hover.setAttributeNS( XMLNS_SVGX, "svgx:stroke", stroke );
    }

}
