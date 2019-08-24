// (c) 2001-2010 Fermi Research Alliance
// $Id: PushButton.java,v 1.3 2010/09/15 16:36:30 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.tools.timed.TimedBoolean;
import gov.fnal.controls.tools.timed.TimedNumber;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import org.w3c.dom.Element;

/**
 *
 * @author Timofei Bolshakov, Andrey Petrov
 * @version $Date: 2010/09/15 16:36:30 $
 */
@DisplayElement(

    name            = "Push Button",
    description     = "A simple button with one stable state.",
    group           = "Controls",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.VisualComponent",
    helpUrl         = "/PushButton",

    properties = {
        @Property( caption="Width",                name="width",        value="60",             type=Integer.class                 ),
        @Property( caption="Height",               name="height",       value="30",             type=Integer.class                 ),
        @Property( caption="Background",           name="background",   value="lightsteelblue", type=Color.class,   required=false ),
        @Property( caption="Text",                 name="text",         value="PUSH"                                               ),
        @Property( caption="Text Color",           name="textColor",    value="navy",           type=Color.class                   ),
        @Property( caption="Font Size",            name="fontSize",     value="12",             type=Integer.class                 ),
        @Property( caption="Italic Font",          name="isItalicFont", value="false",          type=Boolean.class                 ),
        @Property( caption="Bold Font",            name="isBoldFont",   value="false",          type=Boolean.class                 ),
        @Property( caption="Data Tag",             name="tag",          value="",                                   required=false )
    },

    minInputs = 0,
    maxInputs = 0,
    minOutputs = 1,
    maxOutputs = 64,

    outputs = {
        @Pin( number=1, x=1, y=0.5 )
    }

)

public class PushButton extends AbstractButton implements MouseListener {

    private String dataTag;

    public PushButton() {
        addMouseListener( this );
    }

    @Override
    public void init( Element source ) throws Exception {

        PropertyCollection props = PropertyCollection.create( source );
        VisualComponent.handleStandartProps( props, this );

        String defText = props.getValue( String.class, "caption", "" );
        String text = props.getValue( String.class, "text", defText );
        setText( text );

        dataTag = props.getValue( String.class, "tag" );
        
    }

    @Override
    public String getDataTag( int outIndex ) {
        return dataTag;
    }

    @Override
    public void offer( TimedNumber data, int inputIndex ) {}

    @Override
    public void mousePressed( MouseEvent e ) {
        if (isSettingEnabled() && isValidButton( e )) {
            setState( true );
            deliver( new TimedBoolean( true ));
        }
    }

    @Override
    public void mouseReleased( MouseEvent e ) {
        if (isValidButton( e )) {
            setState( false );
        }
    }

    @Override
    public void mouseClicked( MouseEvent e ) {}

    @Override
    public void mouseEntered( MouseEvent e ) {}

    @Override
    public void mouseExited( MouseEvent e ) {}

}