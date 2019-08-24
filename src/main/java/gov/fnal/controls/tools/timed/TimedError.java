// (c) 2010 Fermi Research Alliance
// $Id: TimedError.java,v 1.7 2010/09/09 15:54:20 apetrov Exp $
package gov.fnal.controls.tools.timed;

/**
 * 
 * @author  Andrey Petrov
 */
public class TimedError extends TimedNaN {

    private final int facilityCode, errorNumber;
    private final String message;

    public TimedError( int facilityCode, int errorNumber ) {
        this( facilityCode, errorNumber, null, System.currentTimeMillis());
    }

    public TimedError( int facilityCode, int errorNumber, String message, long time ) {
        super( time );
        this.facilityCode = facilityCode;
        this.errorNumber = errorNumber;
        this.message = message;
    }

    public int getFacilityCode() {
        return facilityCode;
    }

    public int getErrorNumber() {
        return errorNumber;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String stringValue() {
        return "\u00ab" + facilityCode + " " + errorNumber + "\u00bb"
                + (message == null ? "" : " " + message);
    }

    @Override
    public int hashCode() {
        long time = getTime();
        return (int)(facilityCode ^ errorNumber ^ time ^ (time >>> 32));
    }

    @Override
    public boolean equals( Object obj ) {
        return (obj instanceof TimedError)
            && ((TimedError)obj).facilityCode == facilityCode
            && ((TimedError)obj).errorNumber == errorNumber
            && ((TimedError)obj).getTime() == getTime();
    }

}
