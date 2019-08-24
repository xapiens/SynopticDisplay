// (c) 2001-2010 Fermi Research Alliance
// $Id: LookupDisplay.java,v 1.3 2010/09/15 16:36:30 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.applications.syndi.property.Alignment;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.applications.syndi.property.ValueMap;
import gov.fnal.controls.applications.syndi.util.ErrorFormat;
import gov.fnal.controls.applications.syndi.util.FormatConstants;
import gov.fnal.controls.tools.timed.TimedError;
import gov.fnal.controls.tools.timed.TimedNumber;
import java.awt.Color;
import java.util.SortedMap;

/**
 * Basic numeric display.
 * 
 * @author  Andrey Petrov, Timofei Bolshakov
 * @version $Date: 2010/09/15 16:36:30 $
 */
@DisplayElement(

    name            = "Lookup Display",
    description     = "Looks up input values in the internal map and shows the result.",
    group           = "Gauges",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.VisualComponent",
    helpUrl         = "/LookupDisplay",
                    
    properties  = {
        @Property( caption="Width",            name="width",       value="80",     type=Integer.class                 ),
        @Property( caption="Height",           name="height",      value="20",     type=Integer.class                 ),
        @Property( caption="Border",           name="border",      value="",       type=Color.class,   required=false ),
        @Property( caption="Border Width",     name="borderWidth", value="1.0",    type=Double.class                  ),
        @Property( caption="Background",       name="background",  value="",       type=Color.class,   required=false ),
        @Property( caption="Text Color",       name="textColor",   value="navy",   type=Color.class                   ),
        @Property( caption="Font Size",        name="fontSize",    value="12",     type=Integer.class                 ),
        @Property( caption="Italic Font",      name="isItalicFont",value="false",  type=Boolean.class                 ),
        @Property( caption="Bold Font",        name="isBoldFont",  value="false",  type=Boolean.class                 ),
        @Property( caption="Alignment",        name="align",       value="CENTER", type=Alignment.class               ),
        @Property( caption="Value Map",        name="valMap",      value="0=false,1=true",  type=ValueMap.class       ),
        @Property( caption="Input Tolerance",  name="tolerance",   value="0.001",  type=Double.class                  ),
        @Property( caption="'Not Found' Text", name="notFound",    value="",                          required=false  ),
        @Property( caption="String Prefix",    name="prefix",      value="",                          required=false  )
    },

    minInputs = 1,
    maxInputs = 1,
    minOutputs = 0,
    maxOutputs = 64,
    
    inputs = { 
        @Pin( number=0, x=0, y=0.5 )
    }

)
        
public class LookupDisplay extends AbstractDisplay {

    private static final double DEFAULT_TOLERANCE = 0.001;
    
    protected double tolerance;
    protected SortedMap<Double,String> textMap;
    protected String notFound, prefix;
    
    private ErrorFormat errFormat;

    public LookupDisplay() {
        super( Alignment.WEST );
    }

    @Override
    public void init( PropertyCollection props ) throws Exception {

        super.init( props );

        tolerance = props.getValue( Double.class, "tolerance", DEFAULT_TOLERANCE );

        ValueMap valueMap = props.getValue( ValueMap.class, "valMap", ValueMap.EMPTY_INSTANCE );
        textMap = valueMap.createMap( tolerance );

        notFound = props.getValue( String.class, "notFound", "" );
        prefix = props.getValue( String.class, "prefix", "" );

        errFormat = new ErrorFormat( prefix, "" );

        setText( FormatConstants.PLACEHOLDER );
        
    }

    @Override
    protected void process( TimedNumber data ) {
        if (data instanceof TimedError) {
            setError( (TimedError)data );
        } else {
            setValue( data.doubleValue());
        }
    }

    public void setError( TimedError error ) {
        setMessage( error.getMessage());
        setText( errFormat.format( error ));
    }
    
    public void setValue( double value ) {
        setMessage( null );
        String str = textMap.get( value );
        if (str == null) {
            str = notFound;
        }
        setText( prefix + str );
    }

}
