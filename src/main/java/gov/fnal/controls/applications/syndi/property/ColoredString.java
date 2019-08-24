//  (c) 2001-2010 Fermi Research Alliance
//  $Id: ColoredString.java,v 1.1 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

import java.awt.Color;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public class ColoredString {

    private final String string;
    private final Color color;
    
    public ColoredString( String string, Color color ) {
        this.string = string;
        this.color = color;
    }

    @Override
    public String toString() {
        return (string == null) ? "" : string;
    }

    public Color getColor() {
        return color;
    }

}
