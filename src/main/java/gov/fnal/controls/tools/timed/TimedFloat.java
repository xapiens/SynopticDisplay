// (c) 2010 Fermi Research Alliance
// $Id: TimedFloat.java,v 1.7 2010/09/09 15:54:20 apetrov Exp $
package gov.fnal.controls.tools.timed;

/**
 *
 * @author Andrey Petrov
 */
public class TimedFloat extends TimedNumber {

    private final float value;

    public TimedFloat( float value ) {
        this( value, System.currentTimeMillis(), null, 0 );
    }

    public TimedFloat( float value, long time ) {
        this( value, time, null, 0 );
    }

    public TimedFloat( float value, long time, String unit, int hint ) {
        super( time, unit, 0 );
        this.value = value;
    }

    @Override
    public int intValue() {
        return (int)value;
    }

    @Override
    public float floatValue() {
        return value;
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
        long bits = getBits();
        return (bits != 0 && bits != 0x7fc00000); // non-zero and non-NaN
    }

    @Override
    public String stringValue() {
        return String.valueOf( value );
    }

    private int getBits() {
        return Float.floatToIntBits( value );
    }

    @Override
    public int hashCode() {
        int bits = getBits();
        long time = getTime();
        return (int)(bits ^ time ^ (time >>> 32));
    }

    @Override
    public boolean equals( Object obj ) {
        return (obj instanceof TimedFloat)
	    && ((TimedFloat)obj).getBits() == getBits()
            && ((TimedFloat)obj).getTime() == getTime();
    }

}
