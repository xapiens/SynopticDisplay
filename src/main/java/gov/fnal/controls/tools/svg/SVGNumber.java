//  (c) 2009 Fermi Research Alliance
//  $Id: SVGNumber.java,v 1.1 2009/07/27 21:03:00 apetrov Exp $
package gov.fnal.controls.tools.svg;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2009/07/27 21:03:00 $
 */

public class SVGNumber extends Number {

    private static final NumberFormat FORMAT  = new DecimalFormat( "0.###" );

    static {
        FORMAT.setRoundingMode( RoundingMode.HALF_UP );
    }

    public static Number parseNumber( String str ) throws IllegalArgumentException {
        return new SVGNumber( Float.parseFloat( str ));
    }

    public static String toString( Number number ) {
        return FORMAT.format( number );
    }

    private final float value;

    private SVGNumber( float value ) {
        this.value = value;
    }

    @Override
    public int intValue() {
        return (int)value;
    }

    @Override
    public long longValue() {
        return (long)value;
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
    public int hashCode() {
        return Float.floatToIntBits( value );
    }

    @Override
    public boolean equals( Object obj ) {
        return (obj instanceof SVGNumber)
               && Float.floatToIntBits( ((SVGNumber)obj).value ) == Float.floatToIntBits( value );
    }

    @Override
    public String toString() {
        return FORMAT.format( value );
    }

}
