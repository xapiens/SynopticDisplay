// (c) 2001-2010 Fermi Research Allaince
// $Id: SimpleSettingChannel.java,v 1.4 2010/09/15 19:10:01 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.daq;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.applications.syndi.property.DeviceName;
import gov.fnal.controls.tools.timed.TimedNumber;

/**
 * Single-value settable data acquisition channel.
 * 
 * @author  Andrey Petrov
 * @version $Date: 2010/09/15 19:10:01 $
 */
@DisplayElement(

    name            = "Setting",
    description     = "Writes data to a device",
    group           = "Data Acquisition",
    icon            = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAMElEQVR42mNgGDbgP5EYtwGEANkGwKTIMgA" +
                      "kTLYBMM34MS1dQJUwICkWKE0HIxkAADlk9gpk8D1qAAAAAElFTkSuQmCC",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.DaqComponent",
    helpUrl         = "/Setting",

    properties = {
        @Property( caption="Width",        name="width",    value="60",       type=Integer.class    ),
        @Property( caption="Height",       name="height",   value="20",       type=Integer.class    ),
        @Property( caption="Data Request", name="devName",  value="Z:CACHE",  type=DeviceName.class )
    },

    minInputs = 1,
    maxInputs = 1,
    minOutputs = 0,
    maxOutputs = 0,

    inputs = {
        @Pin( number=1, x=0, y=0.5 )
    }

)

public class SimpleSettingChannel extends SimpleChannel implements DaqSettingChannel {

    private SettingListener listener;

    public SimpleSettingChannel() {}

    @Override
    public boolean doesSetting() {
        return true;
    }
    
    @Override
    public void offer( TimedNumber data, int inputIndex ) {
        SettingListener l = this.listener;
        if (l != null) {
            l.newData( data );
        }
    }

    @Override
    public String toString() {
        return "SimpleSettingChannel[dataRequest=" + getDataRequest() + "]";
    }

    @Override
    public void setSettingListener( SettingListener listener ) {
        this.listener = listener;
    }

}
