//  (c) 2001-2010 Fermi Research Alliance
//  $Id: EmbeddedDisplay.java,v 1.3 2010/09/15 16:36:32 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.embed;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.applications.syndi.property.Alignment;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import java.awt.Color;
import java.net.URI;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:36:32 $
 */
@DisplayElement(

    name            = "Embedded Display",
    description     = "A display nested inside another display.",
    group           = "Embedded",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.EmbeddedComponent",
    helpUrl         = "/EmbeddedDisplay",

    properties  = {
        @Property( caption="Width",                name="width",       value="100",          type=Integer.class                 ),
        @Property( caption="Height",               name="height",      value="100",          type=Integer.class                 ),
        @Property( caption="Border",               name="border",      value="",             type=Color.class,   required=false ),
        @Property( caption="Border Width",         name="borderWidth", value="1.0",          type=Double.class                  ),
        @Property( caption="Alignment",            name="align",       value="CENTER",       type=Alignment.class               ),
        @Property( caption="Target URI",           name="target",      value="/Demo/Gauges", type=URI.class                     )
    },

    minInputs = 0,
    maxInputs = 0,
    minOutputs = 0,
    maxOutputs = 0

)


public class EmbeddedDisplay extends EmbeddedComponent {

    public EmbeddedDisplay() {}

    @Override
    protected void init( PropertyCollection props ) throws Exception {}

}
