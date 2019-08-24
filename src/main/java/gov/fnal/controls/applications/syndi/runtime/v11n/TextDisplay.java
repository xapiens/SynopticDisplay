// (c) 2001-2010 Fermi Research Alliance
// $Id: TextDisplay.java,v 1.3 2010/09/15 16:36:30 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.applications.syndi.property.Alignment;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.applications.syndi.util.DecimalFormatFactory;
import gov.fnal.controls.applications.syndi.util.ErrorFormat;
import gov.fnal.controls.tools.timed.TimedError;
import gov.fnal.controls.tools.timed.TimedNumber;
import gov.fnal.controls.tools.timed.TimedString;
import java.awt.Color;
import java.text.DecimalFormat;

/**
 * Basic numeric display.
 * 
 * @author  Andrey Petrov, Timofei Bolshakov
 * @version $Date: 2010/09/15 16:36:30 $
 */
@DisplayElement(

    name            = "Text Display",
    description     = "Shows formatted decimal numbers and plain text data.",
    group           = "Gauges",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.VisualComponent",
    helpUrl         = "/TextDisplay",
                    
    properties  = {
        @Property( caption="Width",          name="width",       value="80",     type=Integer.class                 ),
        @Property( caption="Height",         name="height",      value="20",     type=Integer.class                 ),
        @Property( caption="Border",         name="border",      value="",       type=Color.class,   required=false ),
        @Property( caption="Border Width",   name="borderWidth", value="1.0",    type=Double.class                  ),
        @Property( caption="Background",     name="background",  value="",       type=Color.class,   required=false ),
        @Property( caption="Text Color",     name="textColor",   value="navy",   type=Color.class                   ),
        @Property( caption="Font Size",      name="fontSize",    value="12",     type=Integer.class                 ),
        @Property( caption="Italic Font",    name="isItalicFont",value="false",  type=Boolean.class                 ),
        @Property( caption="Bold Font",      name="isBoldFont",  value="false",  type=Boolean.class                 ),
        @Property( caption="Alignment",      name="align",       value="CENTER", type=Alignment.class               ),
        @Property( caption="Decimal Format", name="format",      value="\\$ #0.000 \\@"                                 )
    },

    minInputs = 1,
    maxInputs = 1,
    minOutputs = 0,
    maxOutputs = 64,
    
    inputs = { 
        @Pin( number=0, x=0, y=0.5 )
    }

)
        
public class TextDisplay extends AbstractDisplay {

    protected String format;
    
    private DecimalFormat decFormat;
    private ErrorFormat errFormat;

    public TextDisplay() {
        super( Alignment.WEST );
    }

    @Override
    public void init( PropertyCollection props ) throws Exception {
        
        super.init( props );

        format = props.findValue( String.class, "format", "vFormat" );
        decFormat = DecimalFormatFactory.createFormat( format );
        errFormat = new ErrorFormat( decFormat );

        String str = DecimalFormatFactory.createPlaceholder( decFormat );
        str = str.replace( "\\@", "" );

        setText( str );

    }

    @Override
    protected void process( TimedNumber data ) {
        if (data instanceof TimedError) {
            setError( (TimedError)data );
        } else if (data instanceof TimedString) {
            setValue( ((TimedString)data).stringValue());
        } else {
            setValue( data.doubleValue(), data.getUnit());
        }
    }

    public void setError( TimedError error ) {
        setMessage( error.getMessage());
        setText( errFormat.format( error ));
    }

    public void setValue( String value ) {
        setMessage( null );
        String str = DecimalFormatFactory.formatText( decFormat, value );
        str = str.replace( "\\@", "" );
        setText( str );
    }

    public void setValue( double value, String unit ) {
        setMessage( null );
        String str = decFormat.format( value );
        str = str.replace( "\\@", (unit == null) ? "" : unit );
        setText( str );
    }

}
