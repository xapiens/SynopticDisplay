// (c) 2001-2010 Fermi Research Allaince
// $Id: SimpleReadingChannel.java,v 1.4 2010/09/15 19:10:01 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.daq;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.applications.syndi.property.DeviceName;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.tools.timed.TimedNumber;

/**
 * Single-value readable data acquisition channel.
 * 
 * @author  Andrey Petrov
 * @version $Date: 2010/09/15 19:10:01 $
 */
@DisplayElement(

    name            = "Reading",
    description     = "Acquires reading, setting, or basic status property (as a number) from a device",
    group           = "Data Acquisition",
    icon            = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAALElEQVR42mNgGDbgP5EYtwGEABU" +
                      "N+P+fCgZgGoJHIz5MDxfQIAxolA5GMgAAk0cR/QaVogEAAAAASUVORK5CYII=",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.DaqComponent",
    helpUrl         = "/Reading",

    properties = {
        @Property( caption="Width",        name="width",   value="60",          type=Integer.class    ),
        @Property( caption="Height",       name="height",  value="20",          type=Integer.class    ),
        @Property( caption="Data Request", name="devName", value="M:OUTTMP",    type=DeviceName.class ),
        @Property( caption="Data Tag",     name="tag",     value="",            required=false        )
    },

    minInputs = 0,
    maxInputs = 0,
    minOutputs = 1,
    maxOutputs = 64,

    outputs = {
        @Pin( number=1, x=1, y=0.5 )
    }

)

public class SimpleReadingChannel extends SimpleChannel implements DaqReadingChannel {

    protected String dataTag;
    
    public SimpleReadingChannel() {}

    @Override
    public void init( PropertyCollection props ) throws Exception {
        super.init( props );
        dataTag = props.getValue( String.class, "tag", getDefaultDataTag());
    }

    private String getDefaultDataTag() {
        String res = getDataRequest();
        if (res == null) {
            return null;
        }
        int i = res.indexOf( '.' );
        if (i <= 0) {
            return res;
        }
        return res.substring( 0, i );
    }
    
    @Override
    public void newData( TimedNumber data ) {
        deliver( data );
    }

    @Override
    public String getDataTag( int outIndex ) {
        return dataTag;
    }
    
    @Override
    public String toString() {
        return "SimpleReadingChannel[dataRequest=" + getDataRequest() + "]";
    }

    @Override
    public boolean doesSetting() {
        return false;
    }

    @Override
    public void offer( TimedNumber data, int inputIndex ) {}

}
