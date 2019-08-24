//  (c) 2009 Fermi Research Alliance
//  $Id: SVGCircle.java,v 1.1 2009/07/27 21:03:01 apetrov Exp $
package gov.fnal.controls.tools.svg;

import java.util.logging.Logger;
import org.xml.sax.Attributes;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2009/07/27 21:03:01 $
 */

@SVGTag( "circle" )
public class SVGCircle extends SVGEllipse {

    private static final Logger log = Logger.getLogger( SVGCircle.class.getName());

    public SVGCircle() {}

    @Override
    public void applyAttributes( Attributes attr ) {
        super.applyAttributes( attr );
        String r_ = attr.getValue( "r" );
        if (r_ != null) {
            try {
                Number r = SVGNumber.parseNumber( r_ );
                setRadiusX( r );
                setRadiusY( r );
            } catch (IllegalArgumentException ex) {
                log.warning( "Illegal 'r' attribute: " + r_ );
            }
        }

    }

}
