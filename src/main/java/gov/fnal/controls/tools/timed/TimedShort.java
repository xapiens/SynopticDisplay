//  (c) 2010 Fermi Research Alliance
//  $Id: TimedShort.java,v 1.7 2010/09/09 15:54:20 apetrov Exp $
package gov.fnal.controls.tools.timed;

/**
 *
 * @author Andrey Petrov
 */
public class TimedShort extends TimedNumber {

    private final short value;

    public TimedShort( short value ) {
        this( value, System.currentTimeMillis(), null, 0 );
    }

    public TimedShort( short value, long time ) {
        this( value, time, null, 0 );
    }

    public TimedShort( short value, long time, String unit, int hint ) {
        super( time, unit, hint );
        this.value = value;
    }

    @Override
    public short shortValue() {
        return value;
    }

    @Override
    public int intValue() {
        return (int)value;
    }

    @Override
    public float floatValue() {
        return (float)value;
    }

    @Override
    public double doubleValue() {
        return (double)value;
    }

    @Override
    public long longValue() {
        return (long)value;
    }

    @Override
    public boolean booleanValue() {
        return value != 0;
    }

    @Override
    public String stringValue() {
        return String.valueOf( value );
    }

    @Override
    public int hashCode() {
        long time = getTime();
        return (int)(value ^ time ^ (time >>> 32));
    }

    @Override
    public boolean equals( Object obj ) {
        return (obj instanceof TimedShort)
	    && ((TimedShort)obj).value == value
            && ((TimedShort)obj).getTime() == getTime();
    }

}
