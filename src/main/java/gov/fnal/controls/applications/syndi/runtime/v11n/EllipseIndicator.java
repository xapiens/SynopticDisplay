// (c) 2001-2010 Fermi Research Alliance
// $Id: EllipseIndicator.java,v 1.3 2010/09/15 16:36:30 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.applications.syndi.markup.Pin;
import java.awt.Color;
import java.awt.geom.Ellipse2D;

/**
 * A round state state indicator.
 * 
 * @author Andrey Petrov, Timofei Bolshakov
 * @version $Date: 2010/09/15 16:36:30 $
 */
@DisplayElement(

    name            = "Ellipse",
    description     = "A round state indicator",
    group           = "State Indicators",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.VisualComponent",
    helpUrl         = "/EllipseStateIndicator",

    properties = {
        @Property( caption="Width",                         name="width",         value="20",        type=Integer.class                 ),
        @Property( caption="Height",                        name="height",        value="20",        type=Integer.class                 ),
        @Property( caption="Minimum Normal Value",          name="minValue",      value="0.0",       type=Double.class,  required=false ),
        @Property( caption="Maximum Normal Value",          name="maxValue",      value="0.0",       type=Double.class,  required=false ),
        @Property( caption="Invert State",                  name="invert",        value="false",     type=Boolean.class, required=false ),
        @Property( caption="Fill Color FALSE/OFF/NORMAL",   name="fillColor0",    value="limegreen", type=Color.class,   required=false ),
        @Property( caption="Fill Color TRUE/ON/ALARM",      name="fillColor1",    value="crimson",   type=Color.class,   required=false ),
        @Property( caption="Stroke Color FALSE/OFF/NORMAL", name="strokeColor0",  value="black",     type=Color.class,   required=false ),
        @Property( caption="Stroke Color TRUE/ON/ALARM",    name="strokeColor1",  value="black",     type=Color.class,   required=false ),
        @Property( caption="Stroke Width",                  name="strokeWidth",   value="2.0",       type=Double.class                  )
    },

    minInputs = 1,
    maxInputs = 1,
    minOutputs = 0,
    maxOutputs = 64,

    inputs = { 
        @Pin( number=1, x=0, y=0.5 ) 
    }

)
        
public class EllipseIndicator extends AbstractShapeIndicator {
    
    public EllipseIndicator() {
        super( new Ellipse2D.Double( 0, 0, 1, 1 ));
    }

}
