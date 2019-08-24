// (c) 2001-2010 Fermi Research Alliance
// $Id: VerticalSlider.java,v 1.3 2010/09/15 16:36:30 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.markup.Property;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JSlider;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:36:30 $
 */
@DisplayElement(

    name            = "Slider - Vertical",
    description     = "An interactive vertical slider",
    group           = "Controls",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.VisualComponent",
    helpUrl         = "/VerticalSlider",

    properties = {
        @Property( caption="Width",            name="width",        value="70",    type=Integer.class                ),
        @Property( caption="Height",           name="height",       value="250",   type=Integer.class                ),
        @Property( caption="Background",       name="background",   value="",      type=Color.class,  required=false ),
        @Property( caption="Text Color",       name="textColor",    value="navy",  type=Color.class                  ),
        @Property( caption="Font Size",        name="fontSize",     value="12",    type=Integer.class                ),
        @Property( caption="Italic Font",      name="isItalicFont", value="false", type=Boolean.class                ),
        @Property( caption="Bold Font",        name="isBoldFont",   value="false", type=Boolean.class                ),
        @Property( caption="Decimal Format",   name="format",       value="#0.0##",                   required=false ),
        @Property( caption="Minimum Value",    name="min",          value="0.0",   type=Double.class                 ),
        @Property( caption="Maximum Value",    name="max",          value="1.0",   type=Double.class                 ),
        @Property( caption="Number of Labels", name="numLabels",    value="3",     type=Integer.class                ),
        @Property( caption="Data Tag",         name="tag",          value="",                         required=false )
    },

    minInputs = 1,
    maxInputs = 1,
    minOutputs = 1,
    maxOutputs = 64,

    inputs = {
        @Pin( number=0, x=0, y=0.67, name="Adjust" )
    },

    outputs = {
        @Pin( number=1, x=1, y=0.5 )
    }

)

public class VerticalSlider extends AbstractSlider {

    public VerticalSlider() {
        slider.setOrientation( JSlider.VERTICAL );
        display.setHorizontalAlignment( JLabel.LEFT );
        add( slider,  new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
            GridBagConstraints.CENTER,   GridBagConstraints.BOTH,
            new Insets( 0, 0, 0, 0 ), 0, 0 ));
        add( display, new GridBagConstraints( 0, 1, 1, 1, 1.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
            new Insets( 6, 0, 0, 0 ), 0, 0 ));
    }

}
