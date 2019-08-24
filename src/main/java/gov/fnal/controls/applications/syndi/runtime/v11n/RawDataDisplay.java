// (c) 2001-2010 Fermi Research Alliance
// $Id: RawDataDisplay.java,v 1.3 2010/09/15 16:36:30 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.applications.syndi.property.Alignment;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.applications.syndi.util.ErrorFormat;
import gov.fnal.controls.applications.syndi.util.FormatConstants;
import gov.fnal.controls.tools.timed.TimedError;
import gov.fnal.controls.tools.timed.TimedNumber;
import java.awt.Color;
import java.util.logging.Logger;

/**
 * Component showing unsigned integer data in various numeric systems.
 * 
 * @author  Andrey Petrov
 * @version $Date: 2010/09/15 16:36:30 $
 */
@DisplayElement(

    name            = "Raw Data Display",
    description     = "Shows unsigned integer data in various numeric systems",
    group           = "Gauges",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.VisualComponent",
    helpUrl         = "/RawDataDisplay",
                    
    properties  = {
        @Property( caption="Width",           name="width",       value="80",     type=Integer.class                 ),
        @Property( caption="Height",          name="height",      value="20",     type=Integer.class                 ),
        @Property( caption="Border",          name="border",      value="",       type=Color.class,   required=false ),
        @Property( caption="Border Width",    name="borderWidth", value="1.0",    type=Double.class                  ),
        @Property( caption="Background",      name="background",  value="",       type=Color.class,   required=false ),
        @Property( caption="Text Color",      name="textColor",   value="navy",   type=Color.class                   ),
        @Property( caption="Font Size",       name="fontSize",    value="12",     type=Integer.class                 ),
        @Property( caption="Italic Font",     name="isItalicFont",value="false",  type=Boolean.class                 ),
        @Property( caption="Bold Font",       name="isBoldFont",  value="false",  type=Boolean.class                 ),
        @Property( caption="Alignment",       name="align",       value="CENTER", type=Alignment.class               ),
        @Property( caption="Number of Bytes", name="numBytes",    value="4",      type=Integer.class                 ),
        @Property( caption="Radix",           name="radix",       value="16",     type=Integer.class                 ),
        @Property( caption="Zero Padding",    name="padding",     value="true",   type=Boolean.class                 ),
        @Property( caption="Prefix",          name="prefix",      value="0x",                         required=false )
    },

    minInputs = 1,
    maxInputs = 1,
    minOutputs = 0,
    maxOutputs = 64,
    
    inputs = { 
        @Pin( number=0, x=0, y=0.5 )
    }

)
        
public class RawDataDisplay extends AbstractDisplay {

    private static final Logger log = Logger.getLogger( RawDataDisplay.class.getName());

    private static final int DEFAULT_NUM_BYTES = 4;
    private static final int DEFAULT_RADIX = 10;
    private static final boolean DEFAULT_PADDING = true;

    private static final int MAX_NUM_BYTES = 8;

    private int numBytes, numDigits, radix;
    private long mask;
    private boolean padding;
    private String prefix;
    private ErrorFormat errFormat;
    
    public RawDataDisplay() {
        super( Alignment.WEST );
    }

    @Override
    public void init( PropertyCollection props ) throws Exception {

        super.init( props );

        numBytes = props.getValue( Integer.class, "numBytes", DEFAULT_NUM_BYTES );
        if (numBytes < 1 || numBytes > MAX_NUM_BYTES) {
            log.warning( "Invalid number of bytes: " + numBytes );
            numBytes = DEFAULT_NUM_BYTES;
        }

        radix = props.getValue( Integer.class, "radix", DEFAULT_RADIX );
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) { // 2 to 36
            log.warning( "Invalid radix: " + radix );
            radix = DEFAULT_RADIX;
        }

        padding = props.getValue( Boolean.class, "padding", DEFAULT_PADDING );

        prefix = props.getValue( String.class, "prefix", "" );

        errFormat = new ErrorFormat( prefix, "" );

        mask = 0;
        for (int i = 0; i < numBytes; ++i) {
            mask = (mask << 8) | 0xff;
        }

        numDigits = (int)Math.ceil( 8 * numBytes * Math.log( 2 ) / Math.log( radix ));

        StringBuilder buf = new StringBuilder( prefix );
        if (padding) {
            for (int i = 0; i < numDigits; ++i) {
                buf.append( FormatConstants.PLACEHOLDER );
            }
        } else {
            buf.append( FormatConstants.PLACEHOLDER );
        }
        setText( buf.toString());
        
    }

    @Override
    protected void process( TimedNumber data ) {
        if (data instanceof TimedError) {
            setError( (TimedError)data );
        } else {
            setValue( data.longValue());
        }
    }

    public void setError( TimedError error ) {
        setMessage( error.getMessage());
        setText( errFormat.format( error ));
    }

    public void setValue( long value ) {
        setMessage( null );
        StringBuilder buf = new StringBuilder();
        value &= mask;
        buf.append( Long.toString( value & mask, radix ).toUpperCase());
        if (padding) {
            for (int i = numDigits - buf.length(); i > 0; --i) {
                buf.insert( 0, 0 );
            }
        }
        setText( prefix + buf.toString());
    }

}
