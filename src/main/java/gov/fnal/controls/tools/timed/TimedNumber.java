//  (c) 2010 Fermi Research Alliance
//  $Id: TimedNumber.java,v 1.6 2010/09/09 15:54:20 apetrov Exp $
package gov.fnal.controls.tools.timed;

//import gov.fnal.controls.service.proto.DAQData.Reply;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Andrey Petrov
 */
public abstract class TimedNumber extends Number {

    private static DateFormat DATE_FORMAT = new SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
    );
    
    private final long time;
    private final String unit;
    private final int hint;

    protected TimedNumber( long time, String unit, int hint ) {
        this.time = time;
        this.unit = unit;
        this.hint = hint;
    }

    public long getTime() {
        return time;
    }

    public String getTimeAsString() {
        return DATE_FORMAT.format( new Date( time ));
    }

    public String getUnit() {
        return unit;
    }

    public int getFormatHint() {
        return hint;
    }

    public abstract boolean booleanValue();

    public abstract String stringValue();

    @Override
    public String toString() {
        return stringValue()
                + ((unit == null) ? "" : " " + unit)
                + " " + getTimeAsString();
    }

}
