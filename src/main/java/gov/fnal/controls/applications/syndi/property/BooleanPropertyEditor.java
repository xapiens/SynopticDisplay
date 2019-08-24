//  (c) 2001-2010 Fermi Research Alliance
//  $Id: BooleanPropertyEditor.java,v 1.1 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public class BooleanPropertyEditor extends AbstractPropertyEditor {

    public BooleanPropertyEditor() {
        this( SwingConstants.LEFT );
    }

    public BooleanPropertyEditor( int align ) {
        super( new JCheckBox());
        ((JCheckBox)getComponent()).setHorizontalAlignment( align );
    }

    @Override
    public Boolean getCellEditorValue() {
        return ((JCheckBox)editorComponent).isSelected();
    }

}
