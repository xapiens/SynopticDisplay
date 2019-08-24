// (c) 2001-2010 Fermi Research Allaince
// $Id: GlobalPropertyTable.java,v 1.2 2010/09/15 15:59:56 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder;

import gov.fnal.controls.applications.syndi.property.AbstractPropertyTable;
import gov.fnal.controls.applications.syndi.property.ColoredString;
import gov.fnal.controls.applications.syndi.property.GlobalNameEditor;
import gov.fnal.controls.applications.syndi.property.StringPropertyRenderer;
import gov.fnal.controls.applications.syndi.property.ComponentProperty;
import gov.fnal.controls.applications.syndi.builder.element.AbstractComponent;
import gov.fnal.controls.applications.syndi.builder.element.BuilderComponent;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:59:56 $
 */
public class GlobalPropertyTable extends AbstractPropertyTable implements TableModel {

    private static final int COLUMN_COMPONENT     = 0;
    private static final int COLUMN_PROPERTY_NAME = 1;
    private static final int COLUMN_DEFAULT_VALUE = 2;
    private static final int COLUMN_GLOBAL_NAME   = 3;

    private final TableCellRenderer compRenderer = new StringPropertyRenderer();
    private final TableCellRenderer nameRenderer = new StringPropertyRenderer();
    private final TableCellRenderer globRenderer = new StringPropertyRenderer();
    private final TableCellEditor globEditor = new GlobalNameEditor();

    private final List<ComponentProperty<?>> props;

    public GlobalPropertyTable( List<ComponentProperty<?>> props ) {
        this.props = props;
        setModel( this );
    }

    public void commitChanges() {
        if (getEditorComponent() != null) {
            editingStopped( new ChangeEvent( this ));
        }
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public int getRowCount() {
        return props.size();
    }

    @Override
    public boolean isCellEditable( int row, int col ) {
        switch (col) {
            case COLUMN_COMPONENT :
                return true;
            case COLUMN_PROPERTY_NAME :
                return false;
            case COLUMN_DEFAULT_VALUE :
                return false;
            case COLUMN_GLOBAL_NAME :
                return true;
            default :
                throw new IllegalArgumentException();
        }
    }

    @Override
    public TableCellEditor getCellEditor( int row, int col ) {
        switch (col) {
            case COLUMN_COMPONENT :
                return null;
            case COLUMN_PROPERTY_NAME :
                return null;
            case COLUMN_DEFAULT_VALUE :
                return null;
            case COLUMN_GLOBAL_NAME :
                return globEditor;
            default :
                throw new IllegalArgumentException();
        }
    }

    @Override
    public TableCellRenderer getCellRenderer( int row, int col ) {
        switch (col) {
            case COLUMN_COMPONENT :
                return compRenderer;
            case COLUMN_PROPERTY_NAME :
                return nameRenderer;
            case COLUMN_DEFAULT_VALUE :
                return getRendererFor( props.get( row ));
            case COLUMN_GLOBAL_NAME :
                return globRenderer;
            default :
                throw new IllegalArgumentException();
        }
    }

    @Override
    public String getColumnName( int col ) {
        switch (col) {
            case COLUMN_COMPONENT :
                return "Component";
            case COLUMN_PROPERTY_NAME :
                return "Property Name";
            case COLUMN_DEFAULT_VALUE :
                return "Default Value";
            case COLUMN_GLOBAL_NAME :
                return "Global Name";
            default :
                throw new IllegalArgumentException();
        }
    }

    @Override
    public Object getValueAt( int row, int col ) {
        ComponentProperty<?> p = props.get( row );
        switch (col) {
            case COLUMN_COMPONENT :
                Object comp = p.getComponent();
                if (!(comp instanceof BuilderComponent)) {
                    return "?";
                }
                String compName = ((BuilderComponent)comp).getName();
                if (comp instanceof AbstractComponent) {
                    Integer id = ((AbstractComponent)comp).getId();
                    if (id != null) {
                        compName = compName + " #" + id;
                    }
                }
                if (p.getGlobalName() == null) {
                    return new ColoredString( compName, HIGHLIGHT_COLOR );
                } else {
                    return compName;
                }
            case COLUMN_PROPERTY_NAME :
                if (p.getGlobalName() == null) {
                    return new ColoredString( p.getCaption(), HIGHLIGHT_COLOR );
                } else {
                    return p.getCaption();
                }
            case COLUMN_DEFAULT_VALUE :
                return p.getValue();
            case COLUMN_GLOBAL_NAME :
                return p.getGlobalName();
            default :
                throw new IllegalArgumentException();
        }
    }

    @Override
    public void setValueAt( Object value, int row, int col ) {
        ComponentProperty<?> p = props.get( row );
        switch (col) {
            case COLUMN_COMPONENT :
                break;
            case COLUMN_PROPERTY_NAME :
                break;
            case COLUMN_DEFAULT_VALUE :
                break;
            case COLUMN_GLOBAL_NAME :
                p.setGlobalName( value == null ? null : value.toString());
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
