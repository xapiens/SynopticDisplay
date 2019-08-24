//  (c) 2010 Fermi Research Alliance
//  $Id: TimedNaN.java,v 1.2 2010/06/02 21:33:43 apetrov Exp $
package gov.fnal.controls.tools.timed;

/**
 *
 * @author Andrey Petrov
 */
public abstract class TimedNaN extends TimedNumber {

    protected TimedNaN( long time ) {
        super( time, null, 0 );
    }

    @Override
    public int intValue() {
        return (int)Double.NaN;
    }

    @Override
    public float floatValue() {
        return (float)Double.NaN;
    }

    @Override
    public double doubleValue() {
        return Double.NaN;
    }

    @Override
    public long longValue() {
        return (long)Double.NaN;
    }

    @Override
    public boolean booleanValue() {
        return false;
    }

}
