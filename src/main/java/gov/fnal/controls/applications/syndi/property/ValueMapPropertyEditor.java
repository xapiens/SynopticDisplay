//  (c) 2001-2010 Fermi Research Alliance
//  $Id: ValueMapPropertyEditor.java,v 1.1 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

import javax.swing.JTextField;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public class ValueMapPropertyEditor extends AbstractPropertyEditor {

    public ValueMapPropertyEditor() {
        super( new JTextField());
    }

    @Override
    public ValueMap getCellEditorValue() {
        String str = ((JTextField)editorComponent).getText().trim();
        return "".equals( str ) ? null : new ValueMap( str );
    }


}
