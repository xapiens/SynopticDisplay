//  (c) 2001-2010 Fermi Research Alliance
//  $Id: PositionPropertyEditor.java,v 1.1 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

import javax.swing.JComboBox;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public class PositionPropertyEditor extends AbstractPropertyEditor {

    public PositionPropertyEditor() {
        super( new JComboBox( Position.values()));
        JComboBox comp = (JComboBox)editorComponent;
        comp.setBorder( null );
    }
    
    @Override
    public Position getCellEditorValue() {
        return (Position)((JComboBox)editorComponent).getSelectedItem();
    }
    
}
