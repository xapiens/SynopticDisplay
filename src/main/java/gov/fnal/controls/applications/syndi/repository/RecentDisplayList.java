// (c) 2001-2010 Fermi Research Alliance
// $Id: RecentDisplayList.java,v 1.2 2010/09/15 15:53:51 apetrov Exp $
package gov.fnal.controls.applications.syndi.repository;

import java.awt.Component;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:53:51 $
 */
class RecentDisplayList extends JList {

    RecentDisplayList( List<DisplaySource> displays ) {
        super( new Model( displays ));
        setCellRenderer( new Renderer());
        if (getModel().getSize() > 0) {
            setSelectedIndex( 0 );
        }
    }

    private static class Model implements ListModel {
        
        final List<DisplaySource> displays;
        
        Model( List<DisplaySource> displays ) {
            this.displays = displays;
        }

        @Override
        public int getSize() {
            return displays.size();
        }

        @Override
        public DisplaySource getElementAt( int index ) {
            return displays.get( index );
        }

        @Override
        public void addListDataListener( ListDataListener l ) {}

        @Override
        public void removeListDataListener( ListDataListener l ) {}
        
    }

    private static class Renderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus ) {
            return super.getListCellRendererComponent(
                    list,
                    String.valueOf( ((DisplaySource)value).getSource() ),
                    index,
                    isSelected,
                    cellHasFocus
            );
        }

    }

}
