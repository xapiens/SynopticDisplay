// (c) 2001-2010 Fermi Research Allaince
// $Id: PropertyTable.java,v 1.2 2010/09/15 15:59:56 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder;

import gov.fnal.controls.applications.syndi.property.AbstractPropertyTable;
import gov.fnal.controls.applications.syndi.property.ColoredString;
import gov.fnal.controls.applications.syndi.property.BooleanPropertyRenderer;
import gov.fnal.controls.applications.syndi.property.EmptyPropertyRenderer;
import gov.fnal.controls.applications.syndi.property.BooleanPropertyEditor;
import gov.fnal.controls.applications.syndi.property.StringPropertyRenderer;
import gov.fnal.controls.applications.syndi.property.ComponentProperty;
import gov.fnal.controls.applications.syndi.property.PropertyException;
import java.util.List;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:59:56 $
 */
public class PropertyTable extends AbstractPropertyTable implements TableModel {

    private static final int COLUMN_NAME    = 0;
    private static final int COLUMN_VALUE   = 1;
    private static final int COLUMN_GLOBAL  = 2;

    private final TableCellRenderer emptyRenderer = new EmptyPropertyRenderer();
    private final TableCellRenderer globRenderer = new BooleanPropertyRenderer( SwingConstants.CENTER );
    private final TableCellEditor globEditor = new BooleanPropertyEditor( SwingConstants.CENTER );
    private final TableCellRenderer nameRenderer = new StringPropertyRenderer();

    private final List<ComponentProperty<?>> props;

    public PropertyTable( List<ComponentProperty<?>> props ) {
        this.props = props;
        setModel( this );
        TableColumn col = columnModel.getColumn( COLUMN_GLOBAL );
	col.setHeaderRenderer( getTableHeader().getDefaultRenderer());
        col.sizeWidthToFit();
    }

    public void commitChanges() {
        if (getEditorComponent() != null) {
            editingStopped( new ChangeEvent( this ));
        }
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public int getRowCount() {
        return props.size();
    }

    @Override
    public boolean isCellEditable( int row, int col ) {
        switch (col) {
            case COLUMN_GLOBAL :
                ComponentProperty<?> p = props.get( row );
                return !p.isLocalOnly();
            case COLUMN_NAME :
                return false;
            case COLUMN_VALUE :
                return true;
            default :
                throw new IllegalArgumentException();
        }
    }

    @Override
    public TableCellEditor getCellEditor( int row, int col ) {
        switch (col) {
            case COLUMN_GLOBAL :
                return globEditor;
            case COLUMN_NAME :
                return null;
            case COLUMN_VALUE :
                return getEditorFor( props.get( row ));
            default :
                throw new IllegalArgumentException();
        }
    }

    @Override
    public TableCellRenderer getCellRenderer( int row, int col ) {
        switch (col) {
            case COLUMN_GLOBAL :
                ComponentProperty<?> p = props.get( row );
                return p.isLocalOnly() ? emptyRenderer : globRenderer;
            case COLUMN_NAME :
                return nameRenderer;
            case COLUMN_VALUE :
                return getRendererFor( props.get( row ));
            default :
                throw new IllegalArgumentException();
        }
    }

    @Override
    public String getColumnName( int col ) {
        switch (col) {
            case COLUMN_GLOBAL :
                return "Global";
            case COLUMN_NAME :
                return "Property Name";
            case COLUMN_VALUE :
                return "Value";
            default :
                throw new IllegalArgumentException();
        }
    }

    @Override
    public Object getValueAt( int row, int col ) {
        ComponentProperty<?> p = props.get( row );
        switch (col) {
            case COLUMN_GLOBAL :
                return p.isGlobal();
            case COLUMN_NAME :
                if (p.isRequired() && p.getValue() == null) {
                    return new ColoredString( p.getCaption(), HIGHLIGHT_COLOR );
                } else {
                    return p.getCaption();
                }
            case COLUMN_VALUE :
                return p.getValue();
            default :
                throw new IllegalArgumentException();
        }
    }

    @Override
    public void setValueAt( Object value, int row, int col ) {
        ComponentProperty<?> p = props.get( row );
        switch (col) {
            case COLUMN_GLOBAL :
                p.setGlobal( (Boolean)value );
                break;
            case COLUMN_NAME :
                break;
            case COLUMN_VALUE :
                try {
                    p.setValueAsObject( value );
                } catch (PropertyException ex) {
                    throw new RuntimeException( ex );
                }
                break;
            default :
                throw new IllegalArgumentException();
        }
        repaint();
    }

    @Override
    public void addTableModelListener( TableModelListener l ) {}

    @Override
    public void removeTableModelListener( TableModelListener l ) {}

}
