// (c) 2001-2010 Fermi Research Alliance
// $Id: SpinnerControl.java,v 1.3 2010/09/15 16:36:30 apetrov Exp $
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
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.logging.Logger;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.w3c.dom.Element;

/**
 *
 * @author Timofei Bolshakov, Andrey Petrov
 * @version $Date: 2010/09/15 16:36:30 $
 */
@DisplayElement(

    name            = "Spinner",
    description     = "An interactive input field for decimal values with increment and decrement buttons.",
    group           = "Controls",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.VisualComponent",
    helpUrl         = "/SpinnerControl",

    properties = {
        @Property( caption="Width",            name="width",        value="80",      type=Integer.class                ),
        @Property( caption="Height",           name="height",       value="20",      type=Integer.class                ),
        @Property( caption="Background",       name="background",   value="thistle", type=Color.class,  required=false ),
        @Property( caption="Text Color",       name="textColor",    value="navy",    type=Color.class                   ),
        @Property( caption="Font Size",        name="fontSize",     value="12",      type=Integer.class                ),
        @Property( caption="Italic Font",      name="isItalicFont", value="false",   type=Boolean.class                ),
        @Property( caption="Bold Font",        name="isBoldFont",   value="false",   type=Boolean.class                ),
        @Property( caption="Minimum Value",    name="min",          value="",        type=Double.class, required=false ),
        @Property( caption="Maximum Value",    name="max",          value="",        type=Double.class, required=false ),
        @Property( caption="Decimal Format",   name="format",       value="#0.000",                     required=false ),
        @Property( caption="Step",             name="step",         value="0.1",     type=Double.class                 ),
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

public class SpinnerControl extends JSpinner implements AbstractInput,
        ChangeListener {

    private static final double DEFAULT_STEP = 1.0;

    private static final Logger log = Logger.getLogger( SpinnerControl.class.getName());

    private final RuntimeComponentSupport sup = new RuntimeComponentSupport();
    private String dataTag;
    private Number lastValue;

    public SpinnerControl() {
        super( new DecimalSpinnerModel());
        addChangeListener( this );
        getEditor().setOpaque( false );
        getEditorField().setOpaque( false );
        getEditorField().setHorizontalAlignment( TEXT_HORIZONTAL_ALIGNMENT );
        getEditorField().setFocusLostBehavior( JFormattedTextField.REVERT );
        getEditorField().addFocusListener( new FocusHandler());
    }

    @Override
    public void setForeground( Color color ) {
        super.setForeground( color );
        getEditor().setForeground( color );
        getEditorField().setForeground( color );
    }

    private JFormattedTextField getEditorField() {
        return ((DefaultEditor)getEditor()).getTextField();
    }

    @Override
    public void init( Element source ) throws Exception {

        PropertyCollection props = PropertyCollection.create( source );
        VisualComponent.handleStandartProps( props, this );

        String format = props.getValue( String.class, "format" );
        getEditorField().setFormatterFactory( new DecimalFormatterFactory( format ));

        SpinnerNumberModel model = (SpinnerNumberModel)getModel();
        model.setMinimum( props.getValue( Double.class, "min" ));
        model.setMaximum( props.getValue( Double.class, "max" ));
        model.setStepSize( props.getValue( Double.class, "step", DEFAULT_STEP ));

        Color fgColor = props.getValue( Color.class, "textColor", VisualComponent.DEFAULT_FOREGROUND );
        if (fgColor != null) {
            getEditorField().setSelectedTextColor( fgColor );
        }

        dataTag = props.getValue( String.class, "tag" );

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
        Double value = new Double( data.doubleValue());
        lastValue = value;
        if (!getEditorField().hasFocus() || !getEditorField().isEditable()) {
            try {
                setValue( value );
            } catch (OutOfRangeException ex) { /* nothing */ }
        }
    }

    public void deliver( TimedNumber data ) {
        sup.deliver( data );
    }

    public void deliver( TimedNumber data, int index ) {
        sup.deliver( data, index );
    }

    @Override
    public synchronized void stateChanged( ChangeEvent e ) {
        Double value = (Double)getValue();
        if ((value == null) ? lastValue == null : value.equals( lastValue )) {
            return;
        }
        log.fine( "New spinner value: " + value );
        deliver( new TimedDouble( value ));
        lastValue = value;
    }

    @Override
    public void setSettingEnabled( boolean settingEnabled ) {
        boolean enabled = !sup.doesSetting() || settingEnabled;
        setEnabled( enabled );
        getEditorField().setEnabled( true );
        getEditorField().setEditable( enabled );
    }

    @Override
    public void paint( Graphics g ) {
        try {
            super.paint( g );
            if (!isEnabled()) {
                g.setColor( DISABLED_CROSS_COLOR );
                g.drawLine( 0, 0, getWidth() - 1, getHeight() - 1 );
                g.drawLine( 0, getHeight() - 1, getWidth() - 1, 0 );
            }
        } catch (NullPointerException ex) {} // see CTRLSYN-250
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
        public void focusLost( FocusEvent e ) {}

        @Override
        public void run() {
            getEditorField().selectAll();
        }

    }

}
