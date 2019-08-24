//  (c) 2001-2010 Fermi Research Alliance
//  $Id: AbstractPropertyEditor.java,v 1.1 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

import java.awt.Component;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
    
/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public abstract class AbstractPropertyEditor extends DefaultCellEditor {
    
    protected AbstractPropertyEditor( JCheckBox editor ) {
        super( editor );
        init();
    }

    protected AbstractPropertyEditor( JComboBox editor ) {
        super( editor );
        init();
    }
    
    protected AbstractPropertyEditor( JTextField editor ) {
        super( editor );
        init();
    }

    protected AbstractPropertyEditor( JComponent editorComponent, JTextField editor ) {
        super( editor );
        this.editorComponent = editorComponent;
    }

    private void init() {
        editorComponent.setBorder( AbstractPropertyRenderer.FOCUS_BORDER );
        setClickCountToStart( 1 );
    }

    @Override
    public Component getTableCellEditorComponent(
            JTable table, 
            Object value,
            boolean selected, 
            int row, int col ) {
        Component res = super.getTableCellEditorComponent( table, value, selected, row, col );
        if (res instanceof JTextField) {
            ((JTextField)res).setText( value == null ? "" : String.valueOf( value ));
        } else if (editorComponent instanceof JComboBox) {
            ((JComboBox)res).setSelectedItem( value );
        } else if (editorComponent instanceof JCheckBox) {
            ((JCheckBox)res).setSelected( value == Boolean.TRUE );
        }
        return editorComponent;
    }

}
