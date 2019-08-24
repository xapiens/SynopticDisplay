//  (c) 2001-2010 Fermi Research Alliance
//  $Id: BooleanProperty.java,v 1.2 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public class BooleanProperty extends ComponentProperty<Boolean> {

    public BooleanProperty( String name, String caption, boolean required ) {
        super( Boolean.class, name, caption, required );
    }

    public BooleanProperty( String name, String caption, boolean required, Boolean value ) {
        this( name, caption, required );
        setValue( value );
    }

    @Override
    public void setValueAsString( String str ) throws PropertyException {
        if (str == null || "".equals( str )) {
            setValue( null );
        } else if ("true".equalsIgnoreCase( str )) {
            setValue( Boolean.TRUE );
        } else if ("false".equalsIgnoreCase( str )) {
            setValue( Boolean.FALSE );
        } else {
            throw new PropertyException( "Invalid boolean value: " + str );
        }
    }

    @Override
    protected Class<? extends TableCellEditor> getEditorImpl() {
        return BooleanPropertyEditor.class;
    }

    @Override
    protected Class<? extends TableCellRenderer> getRendererImpl() {
        return BooleanPropertyRenderer.class;
    }
    
}
