//  (c) 2010 Fermi Research Alliance
//  $Id: TimedString.java,v 1.6 2010/09/09 15:54:20 apetrov Exp $
package gov.fnal.controls.tools.timed;

/**
 *
 * @author Andrey Petrov
 */
public class TimedString extends TimedNaN {

    private final String value;

    public TimedString( String value ) {
        this( value, System.currentTimeMillis());
    }

    public TimedString( String value, long instant ) {
        super( instant );
        if (value == null) {
            throw new NullPointerException();
        }
        this.value = value;
    }

    @Override
    public String stringValue() {
        return value;
    }

    @Override
    public int hashCode() {
        long time = getTime();
        return (int)(value.hashCode() ^ time ^ (time >>> 32));
    }

    @Override
    public boolean equals( Object obj ) {
        return (obj instanceof TimedString)
            && ((TimedString)obj).value.equals( value )
            && ((TimedString)obj).getTime() == getTime();
    }

}
