//  (c) 2009 Fermi Research Alliance
//  $Id: SVGFontWeight.java,v 1.1 2009/07/27 21:03:00 apetrov Exp $
package gov.fnal.controls.tools.svg;

import java.awt.Font;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2009/07/27 21:03:00 $
 */

public enum SVGFontWeight {

    normal  ( Font.PLAIN ),
    bold    ( Font.BOLD );

    private final int styleIndex;

    private SVGFontWeight( int styleIndex ) {
        this.styleIndex = styleIndex;
    }

    public int getStyleIndex() {
        return styleIndex;
    }

    public static SVGFontWeight parseFontWeight( String str ) throws IllegalArgumentException {
        return valueOf( str );
    }

    public static String toString( SVGFontWeight weight ) {
        return weight.toString();
    }

}
