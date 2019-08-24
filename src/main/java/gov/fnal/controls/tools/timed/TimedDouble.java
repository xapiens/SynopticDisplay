// (c) 2010 Fermi Research Alliance
// $Id: TimedDouble.java,v 1.7 2010/09/09 15:54:20 apetrov Exp $
package gov.fnal.controls.tools.timed;

/**
 *
 * @author Andrey Petrov
 */
public class TimedDouble extends TimedNumber {

    private final double value;

    public TimedDouble( double value ) {
        this( value, System.currentTimeMillis(), null, 0 );
    }

    public TimedDouble( double value, long time ) {
        this( value, time, null, 0 );
    }

    public TimedDouble( double value, long time, String unit, int hint ) {
        super( time, unit, 0 );
        this.value = value;
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
        return value;
    }

    @Override
    public long longValue() {
        return (long)value;
    }

    @Override
    public boolean booleanValue() {
        long bits = getBits();
        return (bits != 0 && bits != 0x7ff8000000000000L); // non-zero and non-NaN
    }

    @Override
    public String stringValue() {
        return String.valueOf( value );
    }

    private long getBits() {
        return Double.doubleToLongBits( value );
    }

    @Override
    public int hashCode() {
        long bits = getBits();
        long time = getTime();
        return (int)(bits ^ (bits >>> 32) ^ time ^ (time >>> 32));
    }

    @Override
    public boolean equals( Object obj ) {
        return (obj instanceof TimedDouble)
	    && ((TimedDouble)obj).getBits() == getBits()
            && ((TimedDouble)obj).getTime() == getTime();
    }

}
