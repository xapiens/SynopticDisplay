//  (c) 2001-2010 Fermi Research Alliance
//  $Id: EmptyPropertyRenderer.java,v 1.1 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

import java.awt.Component;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.JTable;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public class EmptyPropertyRenderer extends AbstractPropertyRenderer<JPanel> {

    public EmptyPropertyRenderer() {
        super( new RendererComponent());
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean selected,
            boolean focused,
            int row, int col ) {
        return getCellRendererComponent( table, selected, focused );
    }

    private static class RendererComponent extends JPanel {

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
        protected void firePropertyChange( String propertyName, Object oldValue, Object newValue ) {}

        @Override
        public void firePropertyChange( String propertyName, boolean oldValue, boolean newValue ) {}

    }

}
