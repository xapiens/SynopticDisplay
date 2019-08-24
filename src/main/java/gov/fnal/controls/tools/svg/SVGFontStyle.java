//  (c) 2009 Fermi Research Alliance
//  $Id: SVGFontStyle.java,v 1.1 2009/07/27 21:03:00 apetrov Exp $
package gov.fnal.controls.tools.svg;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2009/07/27 21:03:00 $
 */

public enum SVGFontStyle {

    normal,
    italic,
    oblique;
    
    public static SVGFontStyle parseFontStyle( String str ) throws IllegalArgumentException {
        return valueOf( str );
    }

    public static String toString( SVGFontStyle style ) {
        return style.toString();
    }

}
