//  (c) 2001-2010 Fermi Research Alliance
//  $Id: DataChannelList.java,v 1.1 2010/09/20 21:55:16 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime;

import gov.fnal.controls.applications.syndi.runtime.daq.DaqChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

/**
 *
 * @author Andrey Petrov
 */
class DataChannelList extends JList {

    DataChannelList( Set<DaqChannel> channels ) {
        super( new ModelImpl( channels ));
    }

    private static class ModelImpl implements ListModel {

        private final List<String> items = new ArrayList<String>();

        ModelImpl( Set<DaqChannel> channels ) {
            Set<String> ordered = new TreeSet<String>();
            for (DaqChannel c : channels) {
                String s = c.getDataRequest().toString();
                ordered.add( s );
            }
            items.addAll( ordered );
        }

        @Override
        public int getSize() {
            return items.size();
        }

        @Override
        public Object getElementAt( int index ) {
            return items.get( index );
        }

        @Override
        public void addListDataListener( ListDataListener l ) {}

        @Override
        public void removeListDataListener( ListDataListener l ) {}

    }

}
