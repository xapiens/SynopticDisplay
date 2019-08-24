//  (c) 2001-2010 Fermi Research Alliance
//  $Id: PositionProperty.java,v 1.2 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public class PositionProperty extends ComponentProperty<Position>{

    public PositionProperty( String name, String caption, boolean required ) {
        super( Position.class, name, caption, required );
    }

    public PositionProperty( String name, String caption, boolean required, Position value ) {
        this( name, caption, required );
        setValue( value );
    }

    @Override
    public void setValueAsString( String str ) throws PropertyException {
        if (str == null || "".equals( str )) {
            setValue( null );
        } else {
            try {
                setValue( Position.valueOf( str ));
            } catch (IllegalArgumentException ex) {
                throw new PropertyException( "Illegal position value: " + str );
            }
        }
    }

    @Override
    protected Class<PositionPropertyEditor> getEditorImpl() {
        return PositionPropertyEditor.class;
    }

    @Override
    protected Class<StringPropertyRenderer> getRendererImpl() {
        return StringPropertyRenderer.class;
    }

}
