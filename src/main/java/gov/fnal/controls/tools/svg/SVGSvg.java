//  (c) 2009 Fermi Research Alliance
//  $Id: SVGSvg.java,v 1.3 2009/07/30 22:30:24 apetrov Exp $
package gov.fnal.controls.tools.svg;

import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;

@SVGTag( "svg" )
public class SVGSvg extends SVGContainer {
    
    private static final Logger log = Logger.getLogger( SVGSvg.class.getName());

    private Number x, y;

    public SVGSvg() {}

    @Override
    public void applyAttributes( Attributes attr ) {

        super.applyAttributes( attr );

        String x_ = attr.getValue( "x" );
        if (x_ != null) {
            try {
                setX( SVGNumber.parseNumber( x_ ));
            } catch (IllegalArgumentException ex) {
                log.warning( "Illegal 'x' attribute: " + x_ );
            }
        }

        String y_ = attr.getValue( "y" );
        if (y_ != null) {
            try {
                setY( SVGNumber.parseNumber( y_ ));
            } catch (IllegalArgumentException ex) {
                log.warning( "Illegal 'y' attribute: " + y_ );
            }
        }

    }

    public void setX( Number x ) {
        this.x = x;
    }

    public Number getX() {
        return x;
    }

    public void setY( Number y ) {
        this.y = y;
    }

    public Number getY() {
        return y;
    }

    @Override
    public Element getXML( Document doc ) {
        Element res = super.getXML( doc );
        //res.setAttribute( "xmlns", SVGSyntaxHandler.SVG_NS );
        if (x != null) {
            res.setAttribute( "x", SVGNumber.toString( x ));
        }
        if (y != null) {
            res.setAttribute( "y", SVGNumber.toString( y ));
        }
        return res;
    }

}
