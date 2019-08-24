//  (c) 2001-2010 Fermi Research Alliance
//  $Id: ColorPropertyEditor.java,v 1.1 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

import java.awt.Color;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public class ColorPropertyEditor extends AbstractPropertyEditor {

    public ColorPropertyEditor() {
        super( new ColorComboBox());
        ColorComboBox comp = (ColorComboBox)editorComponent;
        comp.setBorder( null );
    }
    
    @Override
    public Color getCellEditorValue() {
        return (Color)((ColorComboBox)editorComponent).getSelectedItem(); 
    }

}
