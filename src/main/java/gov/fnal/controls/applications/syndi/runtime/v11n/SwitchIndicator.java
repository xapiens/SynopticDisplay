// (c) 2001-2010 Fermi Research Alliance
// $Id: SwitchIndicator.java,v 1.4 2010/09/15 16:36:30 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.applications.syndi.property.Orientation;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.GeneralPath;

/**
 * A "switch" state indicator.
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:36:30 $
 */
@DisplayElement(

    name            = "Switch",
    description     = "A switch state indicator.",
    group           = "State Indicators",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.VisualComponent",
    helpUrl         = "/SwitchStateIndicator",

    properties = {
        @Property( caption="Width",                         name="width",         value="40",        type=Integer.class                 ),
        @Property( caption="Height",                        name="height",        value="40",        type=Integer.class                 ),
        @Property( caption="Minimum Normal Value",          name="minValue",      value="0.0",       type=Double.class,  required=false ),
        @Property( caption="Maximum Normal Value",          name="maxValue",      value="0.0",       type=Double.class,  required=false ),
        @Property( caption="Invert State",                  name="invert",        value="false",     type=Boolean.class, required=false ),
        @Property( caption="Orientation",                   name="orient",        value="NORTH",     type=Orientation.class             ),
        @Property( caption="Stroke Color FALSE/OFF/OPEN",   name="strokeColor0",  value="black",     type=Color.class,   required=false ),
        @Property( caption="Stroke Color TRUE/ON/CLOSED",   name="strokeColor1",  value="black",     type=Color.class,   required=false ),
        @Property( caption="Stroke Width",                  name="strokeWidth",   value="1.0",       type=Double.class                  )
    },

    minInputs = 1,
    maxInputs = 1,
    minOutputs = 0,
    maxOutputs = 64,

    inputs = {
        @Pin( number=1, x=0, y=0.5 )
    }

)

public class SwitchIndicator extends AbstractShapeIndicator {

    private static Shape createOpenSwitch() {
        GeneralPath p = new GeneralPath();
        p.moveTo( 0, 0.5 );
        p.lineTo( 0.25, 0.5 );
        p.lineTo( 0.75, 0.25 );
        p.moveTo( 0.75, 0.5 );
        p.lineTo( 1, 0.5 );
        return p;
    }

    private static Shape createClosedSwitch() {
        GeneralPath p = new GeneralPath();
        p.moveTo( 0, 0.5 );
        p.lineTo( 1, 0.5 );
        return p;
    }

    public SwitchIndicator() {
        super( createOpenSwitch(), createClosedSwitch());
    }

}
