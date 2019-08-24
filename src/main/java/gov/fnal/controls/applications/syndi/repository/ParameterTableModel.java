// (c) 2001-2010 Fermi Research Alliance
// $Id: ParameterTableModel.java,v 1.2 2010/09/15 15:53:52 apetrov Exp $
package gov.fnal.controls.applications.syndi.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:53:52 $
 */
class ParameterTableModel implements TableModel {

    private final List<Entry<String,String>> items = new ArrayList<Entry<String,String>>();
    private final Set<TableModelListener> listeners = new CopyOnWriteArraySet<TableModelListener>();

    ParameterTableModel() {
    }

    ParameterTableModel( Map<String,String> params ) {
        setParameters( params );
    }
    
    public Map<String,String> getParameters() {
        Map<String,String> map = new LinkedHashMap<String,String>( items.size());
        for (Entry<String,String> e : items) {
            map.put( e.getKey(), e.getValue());
        }
        return map;
    }

    public void setParameters( Map<String,String> params ) {
        if (params == null) {
            throw new NullPointerException();
        }
        items.clear();
        items.addAll( params.entrySet());
        TableModelEvent evt = new TableModelEvent( this );
        fireTableChanged( evt );
    }
    
    public void clear() {
        items.clear();
        TableModelEvent evt = new TableModelEvent( this );
        fireTableChanged( evt );
    }

    public void addRow( int index ) {
        Entry<String,String> e = new StringEntryImpl( "" );
        items.add( index, e );
        TableModelEvent evt = new TableModelEvent(
            this,
            index,
            index,
            TableModelEvent.ALL_COLUMNS,
            TableModelEvent.INSERT
        );
        fireTableChanged( evt );
    }

    public void removeRow( int index ) {
        items.remove( index );
        TableModelEvent evt = new TableModelEvent(
            this,
            index,
            index,
            TableModelEvent.ALL_COLUMNS,
            TableModelEvent.DELETE
        );
        fireTableChanged( evt );
    }

    @Override
    public int getRowCount() {
        return items.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName( int col ) {
        switch (col) {
            case 0 :
                return "Name";
            case 1:
                return "Value";
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public Class<?> getColumnClass( int col ) {
        return String.class;
    }

    @Override
    public boolean isCellEditable( int row, int col ) {
        return true;
    }

    @Override
    public String getValueAt( int row, int col ) {
        Entry<String,String> e = items.get( row );
        switch (col) {
            case 0 :
                return e.getKey();
            case 1 :
                return e.getValue();
            default :
                throw new IllegalArgumentException();
        }
    }

    @Override
    public void setValueAt( Object val, int row, int col ) {
        String str = (val == null) ? null : val.toString();
        switch (col) {
            case 0 :
                Entry<String,String> e0 = items.get( row );
                Entry<String,String> e1 = new StringEntryImpl( str, e0.getValue());
                items.set( row, e1 );
                break;
            case 1 :
                items.get( row ).setValue( str );
                break;
            default :
                throw new IllegalArgumentException();
        }
        TableModelEvent evt = new TableModelEvent( 
            this,
            row,
            row,
            col,
            TableModelEvent.UPDATE
        );
        fireTableChanged( evt );
    }

    private void fireTableChanged( TableModelEvent evt ) {
        for (TableModelListener l : listeners) {
            l.tableChanged( evt );
        }
    }

    @Override
    public void addTableModelListener( TableModelListener l ) {
        listeners.add( l );
    }

    @Override
    public void removeTableModelListener( TableModelListener l ) {
        listeners.remove( l );
    }

    private static class StringEntryImpl implements Entry<String,String> {

        private final String key;
        private String value = "";

        StringEntryImpl( String key ) {
            if (key == null) {
                throw new NullPointerException();
            }
            this.key = key;
        }

        StringEntryImpl( String key, String value ) {
            this( key );
            setValue( value );
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public String setValue( String value ) {
            String res = this.value;
            this.value = value;
            return res;
        }

    }

}
