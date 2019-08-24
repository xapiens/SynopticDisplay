// (c) 2001-2010 Fermi Research Alliance
// $Id: TriangleIndicator.java,v 1.3 2010/09/15 16:36:30 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.property.Orientation;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.GeneralPath;

/**
 * A triangular state state indicator.
 * 
 * @author Andrey Petrov, Timofei Bolshakov
 * @version $Date: 2010/09/15 16:36:30 $
 */
@DisplayElement(

    name            = "Triangle",
    description     = "A triangular state indicator.",
    group           = "State Indicators",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.VisualComponent",
    helpUrl         = "/TriangleStateIndicator",

    properties = {
        @Property( caption="Width",                         name="width",         value="40",        type=Integer.class                 ),
        @Property( caption="Height",                        name="height",        value="40",        type=Integer.class                 ),
        @Property( caption="Minimum Normal Value",          name="minValue",      value="0.0",       type=Double.class,  required=false ),
        @Property( caption="Maximum Normal Value",          name="maxValue",      value="0.0",       type=Double.class,  required=false ),
        @Property( caption="Invert State",                  name="invert",        value="false",     type=Boolean.class, required=false ),
        @Property( caption="Orientation",                   name="orient",        value="NORTH",     type=Orientation.class             ),
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
        
public class TriangleIndicator extends AbstractShapeIndicator {
    
    private static Shape createOutline() {
        GeneralPath p = new GeneralPath();
        p.moveTo( 0, 1 );
        p.lineTo( 1, 1 );
        p.lineTo( 0.5, 0 );
        p.closePath();
        return p;
    }
    
    public TriangleIndicator() {
        super( createOutline(), true );
    }
    
}
