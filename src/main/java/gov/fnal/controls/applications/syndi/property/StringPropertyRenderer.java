//  (c) 2001-2010 Fermi Research Alliance
//  $Id: StringPropertyRenderer.java,v 1.1 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

import java.awt.Component;
import java.awt.Rectangle;
import javax.swing.JLabel;
import javax.swing.JTable;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public class StringPropertyRenderer extends AbstractPropertyRenderer<JLabel> {
    
    public StringPropertyRenderer() {
        super( new RendererComponent());
    }

    @Override
    public Component getTableCellRendererComponent( 
            JTable table, 
            Object value, 
            boolean selected, 
            boolean focused, 
            int row, int col ) {
        JLabel comp = getCellRendererComponent( table, selected, focused );
        comp.setText( value == null ? "" : value.toString());
        if (value instanceof ColoredString) {
            comp.setForeground( ((ColoredString)value).getColor() );
        }
        return comp;
    }
    
    private static class RendererComponent extends JLabel {
        
        RendererComponent() {}
        
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
        protected void firePropertyChange( String propertyName, Object oldValue, Object newValue ) {
        }

        @Override
        public void firePropertyChange( String propertyName, boolean oldValue, boolean newValue ) {}

    }

}
