//  (c) 2001-2010 Fermi Research Alliance
//  $Id: ColorComboBox.java,v 1.1 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

import gov.fnal.controls.tools.svg.SVGColor;
import java.awt.Color;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.JComboBox;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public class ColorComboBox extends JComboBox {
    
    private static final Color[] COLORS;
    
    static {
        SortedSet<Color> set = new TreeSet<Color>( new ColorComparator());
        set.addAll( SVGColor.getStandardColors());
        Color[] cc = new Color[ set.size()];
        set.toArray( cc );
        COLORS = new Color[ cc.length + 1 ];
        System.arraycopy( cc, 0, COLORS, 1, cc.length );
        COLORS[ 0 ] = null;
    }
    
    public ColorComboBox() {
        super( COLORS );
        setRenderer( new ColorPropertyRenderer());
    }
    
    private static class ColorComparator implements Comparator<Color> {

        @Override
        public int compare( Color o1, Color o2 ) {
            return o1.toString().compareTo( o2.toString());
        }
        
    }

}
