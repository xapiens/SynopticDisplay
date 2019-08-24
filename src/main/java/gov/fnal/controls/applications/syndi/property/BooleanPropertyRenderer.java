//  (c) 2008 Fermi Research Alliance
//  $Id: BooleanPropertyRenderer.java,v 1.1 2010/09/13 21:16:52 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

import java.awt.Component;
import java.awt.Rectangle;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:52 $
 */
public class BooleanPropertyRenderer extends AbstractPropertyRenderer<JCheckBox> {
    
    public BooleanPropertyRenderer() {
        this( SwingConstants.LEFT );
    }

    public BooleanPropertyRenderer( int align ) {
        super( new RendererComponent( align ));
    }

    @Override
    public Component getTableCellRendererComponent( 
            JTable table, 
            Object value, 
            boolean selected, 
            boolean focused, 
            int row, int col ) {
        JCheckBox comp = getCellRendererComponent( table, selected, focused );
        comp.setSelected( value == Boolean.TRUE );
        return comp;
    }
    
    private static class RendererComponent extends JCheckBox {
        
        RendererComponent( int align ) {
            setHorizontalAlignment( align );
            setBorderPainted( true );
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
