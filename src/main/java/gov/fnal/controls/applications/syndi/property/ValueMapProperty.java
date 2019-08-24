//  (c) 2001-2010 Fermi Research Alliance
//  $Id: ValueMapProperty.java,v 1.2 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public class ValueMapProperty extends ComponentProperty<ValueMap> {

    public ValueMapProperty( String name, String caption, boolean required ) {
        super( ValueMap.class, name, caption, required );
    }

    public ValueMapProperty( String name, String caption, boolean required, ValueMap value ) {
        this( name, caption, required );
        setValue( value );
    }

    @Override
    public void setValueAsString( String str ) throws PropertyException {
        if (str == null || "".equals( str )) {
            setValue( null );
        } else {
            try {
                setValue( new ValueMap( str ));
            } catch (IllegalArgumentException ex) {
                throw new PropertyException( "Illegal value map: " + str );
            }
        }
    }

    @Override
    protected Class<ValueMapPropertyEditor> getEditorImpl() {
        return ValueMapPropertyEditor.class;
    }

    @Override
    protected Class<StringPropertyRenderer> getRendererImpl() {
        return StringPropertyRenderer.class;
    }

}
