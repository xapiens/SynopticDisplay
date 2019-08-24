// (c) 2010 Fermi Research Alliance
// $Id: TimedIntegerArray.java,v 1.7 2010/09/09 15:54:20 apetrov Exp $
package gov.fnal.controls.tools.timed;

import java.util.Arrays;

/**
 *
 * @author Andrey Petrov
 */
public class TimedIntegerArray extends TimedNumber {

    private final int[] values;
    private final int singleValue;

    public TimedIntegerArray( int[] values ) {
        this( values, System.currentTimeMillis(), null, 0 );
    }

    public TimedIntegerArray( int[] value, long time ) {
        this( value, time, null, 0 );
    }

    public TimedIntegerArray( int[] values, long time, String unit, int hint ) {
        super( time, unit, hint );
        this.values = values;
        singleValue = (values.length == 0) ? 0 : values[ 0 ];
    }

    @Override
    public int intValue() {
        return singleValue;
    }

    @Override
    public float floatValue() {
        return (float)singleValue;
    }

    @Override
    public double doubleValue() {
        return (values.length == 0) ? Double.NaN : (double)singleValue;
    }

    @Override
    public long longValue() {
        return (long)singleValue;
    }

    @Override
    public boolean booleanValue() {
        return singleValue != 0;
    }

    @Override
    public String stringValue() {
        return Arrays.toString( values );
    }

    public int[] getArray() {
        return values;
    }

    @Override
    public int hashCode() {
        long time = getTime();
        return (int)(Arrays.hashCode( values ) ^ time ^ (time >>> 32));
    }

    @Override
    public boolean equals( Object obj ) {
        return (obj instanceof TimedIntegerArray)
            && Arrays.equals( ((TimedIntegerArray)obj).values, values )
            && ((TimedIntegerArray)obj).getTime() == getTime();
    }

}
