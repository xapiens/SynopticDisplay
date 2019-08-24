//  (c) 2001-2010 Fermi Research Alliance
//  $Id: DoublePropertyEditor.java,v 1.1 2010/09/13 21:16:52 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

import java.util.regex.Pattern;
import javax.swing.JTextField;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:52 $
 */
public class DoublePropertyEditor extends RegexPropertyEditor {

    public DoublePropertyEditor() {
        super( Pattern.compile( "-?\\d*(\\.\\d*)?([Ee][+-]?\\d*)?" ));
    }

    @Override
    public Double getCellEditorValue() {
        try {
            String str = ((JTextField)editorComponent).getText().trim();
            return "".equals( str ) ? null : new Double( str );
        } catch (NumberFormatException ex) {
            return null;
        }
    }

}
