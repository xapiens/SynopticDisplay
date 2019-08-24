//  (c) 2001-2010 Fermi Research Alliance
//  $Id: RegexPropertyEditor.java,v 1.1 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

import java.util.regex.Pattern;
import javax.swing.JTextField;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoableEdit;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public abstract class RegexPropertyEditor extends AbstractPropertyEditor {
    
    private final Pattern regex;
    
    protected RegexPropertyEditor( Pattern regex ) {
        super( new JTextField());
        this.regex = regex;
        ((JTextField)editorComponent).getDocument().addUndoableEditListener( 
                new UndoableEditListener() {
            
                    @Override
                    public void undoableEditHappened( UndoableEditEvent e ) {
                        String str = ((JTextField)editorComponent).getText();
                        if ("".equals( str )) {
                            return;
                        }
                        if (!RegexPropertyEditor.this.regex.matcher( str ).matches()) {
                            UndoableEdit edit = e.getEdit();
                            edit.undo();
                            edit.die();
                        }
                    }
                    
                }
        );
    }
    
}
