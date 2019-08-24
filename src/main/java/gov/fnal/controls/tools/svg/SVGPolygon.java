//  (c) 2009 Fermi Research Alliance
//  $Id: SVGPolygon.java,v 1.1 2009/07/27 21:03:01 apetrov Exp $
package gov.fnal.controls.tools.svg;

import java.awt.geom.GeneralPath;
import org.xml.sax.Attributes;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2009/07/27 21:03:01 $
 */

@SVGTag( "polygon" )
public class SVGPolygon extends SVGPolyline {

    public SVGPolygon() {}

    @Override
    public void applyAttributes( Attributes attr ) {
        super.applyAttributes( attr );
        ((GeneralPath)super.getPath()).closePath();
    }

}
