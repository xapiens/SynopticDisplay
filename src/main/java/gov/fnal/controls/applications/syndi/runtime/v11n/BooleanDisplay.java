// (c) 2001-2010 Fermi Research Alliance
// $Id: BooleanDisplay.java,v 1.3 2010/09/15 16:36:30 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.property.Alignment;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.applications.syndi.util.ErrorFormat;
import gov.fnal.controls.applications.syndi.util.FormatConstants;
import gov.fnal.controls.tools.timed.TimedError;
import gov.fnal.controls.tools.timed.TimedNumber;
import java.awt.Color;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:36:30 $
 */
@DisplayElement(

    name            = "Boolean Display",
    description     = "A simple display showing boolean values",
    group           = "State Indicators",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.VisualComponent",
    helpUrl         = "/BooleanDisplay",
    
    properties = {
        @Property( caption="Width",                       name="width",         value="80",     type=Integer.class                 ),
        @Property( caption="Height",                      name="height",        value="20",     type=Integer.class                 ),
        @Property( caption="Border",                      name="border",        value="",       type=Color.class,   required=false ),
        @Property( caption="Border Width",                name="borderWidth",   value="1.0",    type=Double.class                  ),
        @Property( caption="Font Size",                   name="fontSize",      value="12",     type=Integer.class                 ),
        @Property( caption="Italic Font",                 name="isItalicFont",  value="false",  type=Boolean.class                 ),
        @Property( caption="Bold Font",                   name="isBoldFont",    value="false",  type=Boolean.class                 ),
        @Property( caption="Alignment",                   name="align",         value="CENTER", type=Alignment.class               ),
        @Property( caption="Minimum Normal Value",        name="minValue",      value="0.0",    type=Double.class,  required=false ),
        @Property( caption="Maximum Normal Value",        name="maxValue",      value="0.0",    type=Double.class,  required=false ),
        @Property( caption="Invert State",                name="invert",        value="false",  type=Boolean.class, required=false ),
        @Property( caption="Background FALSE/OFF/NORMAL", name="offBackground", value="",       type=Color.class,   required=false ),
        @Property( caption="Background TRUE/ON/ALARM",    name="onBackground",  value="",       type=Color.class,   required=false ),
        @Property( caption="Color FALSE/OFF/NORMAL",      name="offColor",      value="navy",   type=Color.class                   ),
        @Property( caption="Color TRUE/ON/ALARM",         name="onColor",       value="navy",   type=Color.class                   ),
        @Property( caption="Text FALSE/OFF/NORMAL",       name="offString",     value="false",                      required=false ),
        @Property( caption="Text TRUE/ON/ALARM",          name="onString",      value="true",                       required=false )
    },

    minInputs = 1,
    maxInputs = 1,
    minOutputs = 0,
    maxOutputs = 64,
    
    inputs = {
        @Pin( number=1, x=0, y=0.5 )
    }
        
)
        
public class BooleanDisplay extends AbstractDisplay {

    private static final double DEFAULT_MIN_VALUE = 0.0;
    private static final double DEFAULT_MAX_VALUE = 0.0;
    private static final boolean DEFAULT_INVERT = false;

    protected Color bgColor0, bgColor1, fgColor0, fgColor1;
    protected String text0, text1;
    protected double minValue, maxValue;
    protected boolean invert;

    private ErrorFormat errFormat;
    
    public BooleanDisplay() {
        super( Alignment.WEST );
    }

    @Override
    public void init( PropertyCollection props ) throws Exception {

        super.init( props );

        minValue = props.getValue( Double.class, "minValue", DEFAULT_MIN_VALUE );
        maxValue = props.getValue( Double.class, "maxValue", DEFAULT_MAX_VALUE );

        invert = props.getValue( Boolean.class, "invert", DEFAULT_INVERT );

        bgColor0 = props.findValue( Color.class, "offBackground", "background" );
        bgColor1 = props.findValue( Color.class, "onBackground", "background" );

        fgColor0 = props.getValue( Color.class, "offColor", DEFAULT_FOREGROUND );
        fgColor1 = props.getValue( Color.class, "onColor", DEFAULT_FOREGROUND );

        text0 = props.getValue( String.class, "offString", "" );
        text1 = props.getValue( String.class, "onString", "" );

        errFormat = new ErrorFormat( "", "" );
        
        setText( FormatConstants.PLACEHOLDER );
        
    }

    @Override
    protected void process( TimedNumber data ) {
        if (data instanceof TimedError) {
            setError( (TimedError)data );
        } else {
            double val = data.doubleValue();
            boolean state = (val < minValue || val > maxValue);
            setValue( invert ? !state : state );
        }
    }

    public void setError( TimedError error ) {
        setMessage( error.getMessage());
        setText( errFormat.format( error ));
    }

    public void setValue( boolean value ) {
        setMessage( null );
        setText( value ? text1 : text0 );
        setForeground( value ? fgColor1 : fgColor0 );
        Color bgColor = value ? bgColor1 : bgColor0;
        setOpaque( bgColor != null );
        setBackground( bgColor );
    }

}
