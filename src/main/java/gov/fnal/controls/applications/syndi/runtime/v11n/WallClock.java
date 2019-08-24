// (c) 2001-2010 Fermi Research Alliance
// $Id: WallClock.java,v 1.3 2010/09/15 16:36:30 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
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
 * Wall clock.
 * 
 * @author  Andrey Petrov, Timofei Bolshakov
 * @version $Date: 2010/09/15 16:36:30 $
 */
@DisplayElement(

    name            = "Wall Clock",
    description     = "Displays current date and time",
    group           = "Gauges",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.VisualComponent",
    helpUrl         = "/WallClock",

    properties  = {
        @Property( caption="Width",          name="width",       value="140",    type=Integer.class                 ),
        @Property( caption="Height",         name="height",      value="20",     type=Integer.class                 ),
        @Property( caption="Border",         name="border",      value="",       type=Color.class,   required=false ),
        @Property( caption="Border Width",   name="borderWidth", value="1.0",    type=Double.class                  ),
        @Property( caption="Background",     name="background",  value="",       type=Color.class,   required=false ),
        @Property( caption="Text Color",     name="textColor",   value="navy",   type=Color.class                   ),
        @Property( caption="Font Size",      name="fontSize",    value="12",     type=Integer.class                 ),
        @Property( caption="Italic Font",    name="isItalicFont",value="false",  type=Boolean.class                 ),
        @Property( caption="Bold Font",      name="isBoldFont",  value="false",  type=Boolean.class                 ),
        @Property( caption="Alignment",      name="align",       value="CENTER", type=Alignment.class               ),
        @Property( caption="Date Format",    name="format",      value="yyyy-MM-dd HH:mm:ss"                        ),
        @Property( caption="Use UTC",        name="utc",         value="false",  type=Boolean.class                 )
    },

    minInputs = 0,
    maxInputs = 0,
    minOutputs = 0,
    maxOutputs = 0

)
        
public class WallClock extends AbstractDisplay implements Runnable {

    private static final boolean DEFAULT_USE_UTC = false;
    
    private DateFormat format;
    
    public WallClock() {
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
    public void run() {
        setText( format.format( new Date()));
    }

    @Override
    protected void process( TimedNumber data ) {}
    
}
