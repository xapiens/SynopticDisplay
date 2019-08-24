// (c) 2010 Fermi Research Alliance
// $Id: TimedBooleanArray.java,v 1.7 2010/09/09 15:54:20 apetrov Exp $
package gov.fnal.controls.tools.timed;

import java.util.Arrays;

/**
 *
 * @author Andrey Petrov
 */
public class TimedBooleanArray extends TimedNumber {

    private final boolean[] values;
    private final boolean singleValue;

    public TimedBooleanArray( boolean[] values ) {
        this( values, System.currentTimeMillis(), null, 0 );
    }

    public TimedBooleanArray( boolean[] value, long time ) {
        this( value, time, null, 0 );
    }

    public TimedBooleanArray( boolean[] values, long time, String unit, int hint ) {
        super( time, unit, hint );
        this.values = values;
        singleValue = (values.length == 0) ? false : values[ 0 ];
    }

    @Override
    public double doubleValue() {
        if (values.length == 0) {
            return Double.NaN;
        } else if (singleValue) {
            return 1.0;
        } else {
            return 0.0;
        }
    }

    @Override
    public float floatValue() {
        return singleValue ? 1.0F : 0.0F;
    }

    @Override
    public int intValue() {
        return singleValue ? 1 : 0;
    }

    @Override
    public long longValue() {
        return singleValue ? 1L : 0L;
    }

    @Override
    public boolean booleanValue() {
        return singleValue;
    }

    @Override
    public String stringValue() {
        return Arrays.toString( values );
    }

    public boolean[] getArray() {
        return values;
    }

    @Override
    public int hashCode() {
        long time = getTime();
        return (int)(Arrays.hashCode( values ) ^ time ^ (time >>> 32));
    }

    @Override
    public boolean equals( Object obj ) {
        return (obj instanceof TimedBooleanArray)
            && Arrays.equals( ((TimedBooleanArray)obj).values, values )
            && ((TimedBooleanArray)obj).getTime() == getTime();
    }

}
