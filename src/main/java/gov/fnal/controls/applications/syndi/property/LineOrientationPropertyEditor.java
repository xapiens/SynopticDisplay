//  (c) 2001-2010 Fermi Research Alliance
//  $Id: LineOrientationPropertyEditor.java,v 1.1 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

import javax.swing.JComboBox;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public class LineOrientationPropertyEditor extends AbstractPropertyEditor {

    public LineOrientationPropertyEditor() {
        super( new JComboBox( LineOrientation.values()));
        JComboBox comp = (JComboBox)editorComponent;
        comp.setBorder( null );
    }

    @Override
    public LineOrientation getCellEditorValue() {
        return (LineOrientation)((JComboBox)editorComponent).getSelectedItem();
    }

}
