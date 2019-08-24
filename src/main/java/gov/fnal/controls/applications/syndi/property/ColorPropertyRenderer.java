//  (c) 2001-2010 Fermi Research Alliance
//  $Id: ColorPropertyRenderer.java,v 1.1 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

import gov.fnal.controls.tools.svg.SVGColor;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public class ColorPropertyRenderer extends AbstractPropertyRenderer<JLabel>
        implements ListCellRenderer {

    private static final int ICON_WIDTH     = 25;
    private static final int ICON_HEIGHT    = 10;

    private static final int ICON_TO_TEXT_GAP = 4;

    public ColorPropertyRenderer() {
        super( new RendererComponent());
    }
    
    private static Icon createIcon( Color color ) {
        BufferedImage x = new BufferedImage( ICON_WIDTH, ICON_HEIGHT, BufferedImage.TYPE_INT_ARGB );
        Graphics g = x.getGraphics();
        g.setColor( color );
        g.fillRect( 1, 1, ICON_WIDTH - 2, ICON_HEIGHT - 2 );
        g.setColor( Color.BLACK );
        g.drawRect( 0, 0, ICON_WIDTH - 1, ICON_HEIGHT - 1 );
        return new ImageIcon( x );
    }
    
    @Override
    public JLabel getTableCellRendererComponent(
            JTable table, 
            Object value, 
            boolean selected, 
            boolean focused, 
            int row, int col ) {
        JLabel comp = getCellRendererComponent( table, selected, focused );
        return init( comp, (Color)value );
    }
    
    @Override
    public JLabel getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean selected,
            boolean focused ) {
        JLabel comp = getCellRendererComponent( list, selected, focused );
        return init( comp, (Color)value );
    }

    private JLabel init( JLabel comp, Color color ) {
        if (color == null || color == SVGColor.NO_COLOR) {
            comp.setIcon( null );
            comp.setText( " " );
        } else {
            comp.setIcon( createIcon( color ));
            comp.setText( SVGColor.toString( color ));
        }
        return comp;
    }

    private static class RendererComponent extends JLabel {
        
        RendererComponent() {
            setIconTextGap( ICON_TO_TEXT_GAP );
        }
        
        @Override
        public void invalidate() {}

        @Override
        public void validate() {}

        @Override
        public void revalidate() {}

        @Override
        public void repaint( long tm, int x, int y, int width, int height ) {}

        @Override
        public void repaint( Rectangle r ) {}

        @Override
        public void repaint() {}

        @Override
        protected void firePropertyChange( String propertyName, Object oldValue, Object newValue ) {}

        @Override
        public void firePropertyChange( String propertyName, boolean oldValue, boolean newValue ) {}

    }

}
