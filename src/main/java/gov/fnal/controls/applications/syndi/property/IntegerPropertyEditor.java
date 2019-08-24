//  (c) 2001-2010 Fermi Research Alliance
//  $Id: IntegerPropertyEditor.java,v 1.1 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

import java.util.regex.Pattern;
import javax.swing.JTextField;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public class IntegerPropertyEditor extends RegexPropertyEditor {

    public IntegerPropertyEditor() {
        super( Pattern.compile( "-?\\d*" ));
    }

    @Override
    public Integer getCellEditorValue() {
        try {
            String str = ((JTextField)editorComponent).getText().trim();
            return "".equals( str ) ? null : new Integer( str );
        } catch (NumberFormatException ex) {
            return null;
        }
    }

}
