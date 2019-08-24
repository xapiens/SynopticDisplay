//  (c) 2010 Fermi Research Alliance
//  $Id: TimedBoolean.java,v 1.7 2010/09/09 15:54:20 apetrov Exp $
package gov.fnal.controls.tools.timed;

/**
 *
 * @author Andrey Petrov
 */
public class TimedBoolean extends TimedNumber {

    private final boolean value;

    public TimedBoolean( boolean value ) {
        this( value, System.currentTimeMillis(), null, 0 );
    }

    public TimedBoolean( boolean value, long time ) {
        this( value, time, null, 0 );
    }

    public TimedBoolean( boolean value, long time, String unit, int hint ) {
        super( time, unit, hint );
        this.value = value;
    }

    @Override
    public double doubleValue() {
        return value ? 1.0 : 0.0;
    }

    @Override
    public float floatValue() {
        return value ? 1.0F : 0.0F;
    }

    @Override
    public int intValue() {
        return value ? 1 : 0;
    }

    @Override
    public long longValue() {
        return value ? 1L : 0L;
    }

    @Override
    public boolean booleanValue() {
        return value;
    }

    @Override
    public String stringValue() {
        return String.valueOf( value );
    }

    @Override
    public int hashCode() {
        long time = getTime();
        return (int)(intValue() ^ time ^ (time >>> 32));
    }

    @Override
    public boolean equals( Object obj ) {
        return (obj instanceof TimedBoolean)
            && ((TimedBoolean)obj).value == value
            && ((TimedBoolean)obj).getTime() == getTime();
    }

}
