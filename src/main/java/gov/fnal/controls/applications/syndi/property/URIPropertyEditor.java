//  (c) 2001-2010 Fermi Research Alliance
//  $Id: URIPropertyEditor.java,v 1.1 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

import gov.fnal.controls.applications.syndi.repository.DisplayAddressField;
import java.net.URI;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public class URIPropertyEditor extends AbstractPropertyEditor {

    public URIPropertyEditor() {
        this( new DisplayAddressField());
    }

    private URIPropertyEditor( DisplayAddressField editor ) {
        super( editor, editor.getTextField());
    }

    @Override
    public URI getCellEditorValue() {
        return ((DisplayAddressField)editorComponent).getURI();
    }

}
