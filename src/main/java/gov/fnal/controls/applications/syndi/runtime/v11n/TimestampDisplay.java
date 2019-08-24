// (c) 2001-2010 Fermi Research Alliance
// $Id: TimestampDisplay.java,v 1.3 2010/09/15 16:36:30 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.applications.syndi.property.Alignment;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.applications.syndi.util.DateFormatFactory;
import gov.fnal.controls.tools.timed.TimedNumber;
import java.awt.Color;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;


/**
 * Displays timestamps of data samples.
 * 
 * @author  Andrey Petrov
 * @version $Date: 2010/09/15 16:36:30 $
 */
@DisplayElement(

    name            = "Timestamp",
    description     = "Displays timestamps of data samples",
    group           = "Gauges",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.VisualComponent",
    helpUrl         = "/Timestamp",

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
        @Property( caption="Date Format",    name="format",      value="HH:mm:ss.SSS"                               ),
        @Property( caption="Use UTC",        name="utc",         value="false",  type=Boolean.class                 )
    },

    minInputs = 1,
    maxInputs = 1,
    minOutputs = 0,
    maxOutputs = 64,

    inputs = {
        @Pin( number=0, x=0, y=0.5 )
    }

)
        
public class TimestampDisplay extends AbstractDisplay {

    private static final boolean DEFAULT_USE_UTC = false;
    
    private DateFormat format;
    
    public TimestampDisplay() {
        super( Alignment.WEST );
    }

    @Override
    public void init( PropertyCollection props ) throws Exception {
        super.init( props );
        String str = props.findValue( String.class, "format", "dFormat" );
        format = DateFormatFactory.createFormat( str );
        if (props.getValue( Boolean.class, "utc", DEFAULT_USE_UTC )) {
            format.setTimeZone( TimeZone.getTimeZone( "Z" ));
        }
        setText( DateFormatFactory.createPlaceholder( format ));
    }

    @Override
    protected void process( TimedNumber data ) {
        setText( format.format( new Date( data.getTime())));
    }
    
}
