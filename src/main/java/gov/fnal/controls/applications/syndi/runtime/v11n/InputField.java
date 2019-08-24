// (c) 2001-2010 Fermi Research Alliance
// $Id: InputField.java,v 1.3 2010/09/15 16:36:30 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.applications.syndi.runtime.RuntimeComponent;
import gov.fnal.controls.applications.syndi.runtime.RuntimeComponentSupport;
import gov.fnal.controls.applications.syndi.util.DecimalFormatterFactory;
import gov.fnal.controls.tools.timed.TimedDouble;
import gov.fnal.controls.tools.timed.TimedNumber;
import gov.fnal.controls.tools.timed.TimedString;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.util.logging.Logger;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.w3c.dom.Element;

/**
 *
 * @author Timofei Bolshakov, Andrey Petrov
 * @version $Date: 2010/09/15 16:36:30 $
 */
@DisplayElement(

    name            = "Input Field",
    description     = "An interactive input field for decimal and text values.",
    group           = "Controls",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.VisualComponent",
    helpUrl         = "/InputField",

    properties = {
        @Property( caption="Width",            name="width",        value="60",      type=Integer.class                ),
        @Property( caption="Height",           name="height",       value="20",      type=Integer.class                ),
        @Property( caption="Background",       name="background",   value="thistle", type=Color.class,  required=false ),
        @Property( caption="Text Color",       name="textColor",    value="navy",    type=Color.class                  ),
        @Property( caption="Font Size",        name="fontSize",     value="12",      type=Integer.class                ),
        @Property( caption="Italic Font",      name="isItalicFont", value="false",   type=Boolean.class                ),
        @Property( caption="Bold Font",        name="isBoldFont",   value="false",   type=Boolean.class                ),
        @Property( caption="Border Width",     name="borderWidth",  value="1.0",     type=Double.class                 ),
        @Property( caption="Decimal Format",   name="format",       value="#0.000",                     required=false ),
        @Property( caption="Data Tag",         name="tag",          value="",                           required=false )
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

public class InputField extends JTextField implements AbstractInput {

    private static final Logger log = Logger.getLogger( InputField.class.getName());

    private final RuntimeComponentSupport sup = new RuntimeComponentSupport();
    
    private String dataTag;
    private TimedNumber lastValue;
    private AbstractFormatter formatter;

    public InputField() {
        addKeyListener( new KeyListener());
        setHorizontalAlignment( TEXT_HORIZONTAL_ALIGNMENT );
        addFocusListener( new FocusHandler());
    }

    @Override
    public void init( Element source ) throws Exception {

        PropertyCollection props = PropertyCollection.create( source );
        VisualComponent.handleStandartProps( props, this );

        String format = props.getValue( String.class, "format" );
        formatter = new DecimalFormatterFactory( format ).getFormatter( null );

        dataTag = props.getValue( String.class, "tag" );

        setValue( null );
        
    }

    @Override
    public void setInput( int index, RuntimeComponent comp, int reverseIndex ) {
        sup.setInput( index, comp, reverseIndex );
    }

    @Override
    public RuntimeComponent getInput( int index ) {
        return sup.getInput( index );
    }

    @Override
    public void setOutput( int index, RuntimeComponent comp, int reverseIndex ) {
        sup.setOutput( index, comp, reverseIndex );
    }

    @Override
    public RuntimeComponent getOutput( int index ) {
        return sup.getOutput( index );
    }

    @Override
    public String getDataTag( int outIndex ) {
        return dataTag;
    }

    @Override
    public boolean doesSetting() {
        return false;
    }

    @Override
    public synchronized void offer( TimedNumber data, int inputIndex ) {
        lastValue = data;
        if (!hasFocus() || !isEditable()) {
            setValue( data );
        }
    }

    public void deliver( TimedNumber data ) {
        sup.deliver( data );
    }

    public void deliver( TimedNumber data, int index ) {
        sup.deliver( data, index );
    }

    protected void setValue( TimedNumber data ) {
        if (data instanceof TimedString) {
            setText( ((TimedString)data).stringValue() );
        } else {
            try {
                String str = formatter.valueToString( data );
                setText( str );
            } catch (ParseException ex) {
                log.throwing( InputField.class.getName(), "setValue", ex );
            }
        }
    }

    protected TimedNumber getValue() {
        String str = getText().trim();
        try {
            double val = Double.parseDouble( str );
            return new TimedDouble( val );
        } catch (NumberFormatException ex) {
            return new TimedString( str );
        }
    }

    @Override
    public void setSettingEnabled( boolean settingEnabled ) {
        setEditable( !sup.doesSetting() || settingEnabled );
    }

    @Override
    public void paint( Graphics g ) {
        super.paint( g );
        if (!isEditable()) {
            g.setColor( DISABLED_CROSS_COLOR );
            g.drawLine( 0, 0, getWidth() - 1, getHeight() - 1 );
            g.drawLine( 0, getHeight() - 1, getWidth() - 1, 0 );
        }
    }

    protected synchronized void commitEdit() {
        TimedNumber value = getValue();
        log.fine( "New input value: " + value );
        setValue( value );
        deliver( value );
        lastValue = value;
    }

    protected synchronized void cancelEdit() {
        setValue( lastValue );
    }

    @Override
    protected void paintBorder( Graphics g ) {
        BORDER.paintBorder( this, g, 0, 0, getWidth(), getHeight());
    }

    private class FocusHandler implements FocusListener, Runnable {

        @Override
        public void focusGained( FocusEvent e ) {
            SwingUtilities.invokeLater( this );
        }

        @Override
        public void focusLost( FocusEvent e ) {
            cancelEdit();
        }

        @Override
        public void run() {
            selectAll();
        }

    }

    private class KeyListener extends KeyAdapter {

        @Override
        public void keyPressed( KeyEvent e ) {
            switch (e.getKeyChar()) {
                case KeyEvent.VK_ENTER :
                    commitEdit();
                    break;
                case KeyEvent.VK_ESCAPE :
                    cancelEdit();
                    break;
            }
        }

    }

}
