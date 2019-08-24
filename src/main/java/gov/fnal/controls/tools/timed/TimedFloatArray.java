// (c) 2010 Fermi Research Alliance
// $Id: TimedFloatArray.java,v 1.7 2010/09/09 15:54:20 apetrov Exp $
package gov.fnal.controls.tools.timed;

import java.util.Arrays;

/**
 *
 * @author Andrey Petrov
 */
public class TimedFloatArray extends TimedNumber {

    private final float[] values;
    private final float singleValue;

    public TimedFloatArray( float[] values ) {
        this( values, System.currentTimeMillis(), null, 0 );
    }

    public TimedFloatArray( float[] value, long time ) {
        this( value, time, null, 0 );
    }

    public TimedFloatArray( float[] values, long time, String unit, int hint ) {
        super( time, unit, hint );
        this.values = values;
        singleValue = (values.length == 0) ? Float.NaN : values[ 0 ];
    }

    @Override
    public int intValue() {
        return (int)singleValue;
    }

    @Override
    public float floatValue() {
        return singleValue;
    }

    @Override
    public double doubleValue() {
        return (double)singleValue;
    }

    @Override
    public long longValue() {
        return (long)singleValue;
    }

    @Override
    public boolean booleanValue() {
        long d = Float.floatToIntBits( singleValue );
        return (d != 0 && d != 0x7fc00000); // non-zero and non-NaN
    }

    @Override
    public String stringValue() {
        return Arrays.toString( values );
    }

    public float[] getArray() {
        return values;
    }

    @Override
    public int hashCode() {
        long time = getTime();
        return (int)(Arrays.hashCode( values ) ^ time ^ (time >>> 32));
    }

    @Override
    public boolean equals( Object obj ) {
        return (obj instanceof TimedFloatArray)
            && Arrays.equals( ((TimedFloatArray)obj).values, values )
            && ((TimedFloatArray)obj).getTime() == getTime();
    }

}
