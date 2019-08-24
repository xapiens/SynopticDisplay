// (c) 2010 Fermi Research Alliance
// $Id: TimedLongArray.java,v 1.7 2010/09/09 15:54:20 apetrov Exp $
package gov.fnal.controls.tools.timed;

import java.util.Arrays;

/**
 *
 * @author Andrey Petrov
 */
public class TimedLongArray extends TimedNumber {

    private final long[] values;
    private final long singleValue;

    public TimedLongArray( long[] values ) {
        this( values, System.currentTimeMillis(), null, 0 );
    }

    public TimedLongArray( long[] value, long time ) {
        this( value, time, null, 0 );
    }

    public TimedLongArray( long[] values, long time, String unit, int hint ) {
        super( time, unit, hint );
        this.values = values;
        singleValue = (values.length == 0) ? 0 : values[ 0 ];
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
        return (values.length == 0) ? Double.NaN : (double)singleValue;
    }

    @Override
    public long longValue() {
        return singleValue;
    }

    @Override
    public boolean booleanValue() {
        return singleValue != 0;
    }

    @Override
    public String stringValue() {
        return Arrays.toString( values );
    }

    public long[] getArray() {
        return values;
    }

    @Override
    public int hashCode() {
        long time = getTime();
        return (int)(Arrays.hashCode( values ) ^ time ^ (time >>> 32));
    }

    @Override
    public boolean equals( Object obj ) {
        return (obj instanceof TimedLongArray)
            && Arrays.equals( ((TimedLongArray)obj).values, values )
            && ((TimedLongArray)obj).getTime() == getTime();
    }

}
