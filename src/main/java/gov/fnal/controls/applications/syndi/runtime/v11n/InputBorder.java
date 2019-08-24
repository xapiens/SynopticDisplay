// (c) 2001-2010 Fermi Research Alliance
// $Id: InputBorder.java,v 1.2 2010/09/15 15:48:22 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.border.AbstractBorder;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:48:22 $
 */
class InputBorder extends AbstractBorder {

    InputBorder() {}

    @Override
    public void paintBorder( Component c, Graphics g, int x, int y, int w, int h ) {
        g.translate( x, y );
        g.setColor( c.getBackground().darker() );
        g.drawRect( 0, 0, w - 1, h - 1 );
        g.setColor( c.getBackground().brighter() );
        g.drawRect( 1, 1, w - 3, h - 3 );
        g.translate( -x, -y );
    }

    @Override
    public Insets getBorderInsets( Component c )       {
        return new Insets( 2, 2, 2, 2 );
    }

    @Override
    public Insets getBorderInsets( Component c, Insets insets ) {
        insets.left = insets.top = insets.right = insets.bottom = 2;
        return insets;
    }

    @Override
    public boolean isBorderOpaque() {
        return true;
    }

}
