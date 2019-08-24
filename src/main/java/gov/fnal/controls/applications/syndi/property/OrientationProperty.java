//  (c) 2001-2010 Fermi Research Alliance
//  $Id: OrientationProperty.java,v 1.2 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public class OrientationProperty extends ComponentProperty<Orientation>{

    public OrientationProperty( String name, String caption, boolean required ) {
        super( Orientation.class, name, caption, required );
    }

    public OrientationProperty( String name, String caption, boolean required, Orientation value ) {
        this( name, caption, required );
        setValue( value );
    }

    @Override
    public void setValueAsString( String str ) throws PropertyException {
        if (str == null || "".equals( str )) {
            setValue( null );
        } else {
            try {
                setValue( Orientation.valueOf( str ));
            } catch (IllegalArgumentException ex) {
                throw new PropertyException( "Illegal orientation value: " + str );
            }
        }
    }

    @Override
    protected Class<OrientationPropertyEditor> getEditorImpl() {
        return OrientationPropertyEditor.class;
    }

    @Override
    protected Class<StringPropertyRenderer> getRendererImpl() {
        return StringPropertyRenderer.class;
    }

}
