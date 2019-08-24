// (c) 2001-2010 Fermi Research Alliance
// $Id: ToggleButton.java,v 1.3 2010/09/15 16:36:30 apetrov Exp $
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

    name            = "Toggle Button",
    description     = "An interactive button with two stable states producing boolean values.",
    group           = "Controls",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.VisualComponent",
    helpUrl         = "/ToggleButton",

    properties = {
        @Property( caption="Width",                name="width",        value="60",             type=Integer.class                 ),
        @Property( caption="Height",               name="height",       value="30",             type=Integer.class                 ),
        @Property( caption="Fill Color FALSE/OFF", name="fillColor0",   value="lightsteelblue", type=Color.class,   required=false ),
        @Property( caption="Fill Color TRUE/ON",   name="fillColor1",   value="lightsalmon",    type=Color.class,   required=false ),
        @Property( caption="Text FALSE/OFF",       name="offString",    value="OFF"                                                ),
        @Property( caption="Text TRUE/ON",         name="onString",     value="ON"                                                 ),
        @Property( caption="Text Color",           name="textColor",    value="navy",           type=Color.class                   ),
        @Property( caption="Font Size",            name="fontSize",     value="12",             type=Integer.class                 ),
        @Property( caption="Italic Font",          name="isItalicFont", value="false",          type=Boolean.class                 ),
        @Property( caption="Bold Font",            name="isBoldFont",   value="false",          type=Boolean.class                 ),
        @Property( caption="Data Tag",             name="tag",          value="",                                   required=false )
    },

    minInputs = 1,
    maxInputs = 1,
    minOutputs = 1,
    maxOutputs = 64,

    inputs = {
        @Pin( number=0, x=0, y=0.5, name="Adjust" )
    },

    outputs = {
        @Pin( number=1, x=1, y=0.5 )
    }

)

public class ToggleButton extends AbstractButton implements MouseListener {

    private String dataTag, textOff, textOn;
    private Color bgColorOff, bgColorOn;

    public ToggleButton() {
        addMouseListener( this );
    }

    @Override
    public void init( Element source ) throws Exception {

        PropertyCollection props = PropertyCollection.create( source );
        VisualComponent.handleStandartProps( props, this );

        String defText = props.getValue( String.class, "caption", "" );
        textOff = props.getValue( String.class, "offString", defText );
        textOn = props.getValue( String.class, "onString", defText );

        bgColorOff = props.getValue( Color.class, "fillColor0", null );
        bgColorOn = props.getValue( Color.class, "fillColor1", null );

        dataTag = props.getValue( String.class, "tag" );
        
        updateView();

    }

    @Override
    public String getDataTag( int outIndex ) {
        return dataTag;
    }
    
    @Override
    public void offer( TimedNumber data, int inputIndex ) {
        setState( data.booleanValue());
    }

    @Override
    protected void stateChanged( boolean state ) {
        Color bgColor = state ? bgColorOn : bgColorOff;
        if (bgColor != null) {
            setBackground( bgColor );
            setOpaque( true );
        } else {
            setOpaque( false );
        }
        setText( state ? textOn : textOff );
        repaint();
    }

    @Override
    public void mouseClicked( MouseEvent e ) {
        if (isSettingEnabled() && isValidButton( e )) {
            boolean newState = !getState();
            setState( newState );
            deliver( new TimedBoolean( newState ));
        }
    }

    @Override
    public void mousePressed( MouseEvent e ) {}

    @Override
    public void mouseReleased( MouseEvent e ) {}

    @Override
    public void mouseEntered( MouseEvent e ) {}

    @Override
    public void mouseExited( MouseEvent e ) {}


}