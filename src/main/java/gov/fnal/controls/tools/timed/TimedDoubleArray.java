// (c) 2010 Fermi Research Alliance
// $Id: TimedDoubleArray.java,v 1.7 2010/09/09 15:54:20 apetrov Exp $
package gov.fnal.controls.tools.timed;

import java.util.Arrays;

/**
 *
 * @author Andrey Petrov
 */
public class TimedDoubleArray extends TimedNumber {

    private final double[] values;
    private final double singleValue;

    public TimedDoubleArray( double[] values ) {
        this( values, System.currentTimeMillis(), null, 0 );
    }

    public TimedDoubleArray( double[] value, long time ) {
        this( value, time, null, 0 );
    }

    public TimedDoubleArray( double[] values, long time, String unit, int hint ) {
        super( time, unit, hint );
        this.values = values;
        singleValue = (values.length == 0) ? Double.NaN : values[ 0 ];
    }

    @Override
    public int intValue() {
        return (int)singleValue;
    }

    @Override
    public float floatValue() {
        return (float)singleValue;
    }

    @Override
    public double doubleValue() {
        return singleValue;
    }

    @Override
    public long longValue() {
        return (long)singleValue;
    }

    @Override
    public boolean booleanValue() {
        long d = Double.doubleToLongBits( singleValue );
        return (d != 0 && d != 0x7ff8000000000000L); // non-zero and non-NaN
    }

    @Override
    public String stringValue() {
        return Arrays.toString( values );
    }

    public double[] getArray() {
        return values;
    }

    @Override
    public int hashCode() {
        long time = getTime();
        return (int)(Arrays.hashCode( values ) ^ time ^ (time >>> 32));
    }

    @Override
    public boolean equals( Object obj ) {
        return (obj instanceof TimedDoubleArray)
            && Arrays.equals( ((TimedDoubleArray)obj).values, values )
            && ((TimedDoubleArray)obj).getTime() == getTime();
    }

}
