//  (c) 2001-2010 Fermi Research Alliance
//  $Id: IntegerProperty.java,v 1.2 2010/09/13 21:16:52 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:52 $
 */
public class IntegerProperty extends ComponentProperty<Integer> {

    public IntegerProperty( String name, String caption, boolean required ) {
        super( Integer.class, name, caption, required );
    }

    public IntegerProperty( String name, String caption, boolean required, Integer value ) {
        this( name, caption, required );
        setValue( value );
    }

    @Override
    public void setValueAsString( String str ) throws PropertyException {
        if (str == null || "".equals( str )) {
            setValue( null );
        } else {
            try {
                setValue( new Integer( str ));
            } catch (NumberFormatException ex) {
                throw new PropertyException( "Illegal integer value: " + str );
            }
        }
    }

    @Override
    protected Class<IntegerPropertyEditor> getEditorImpl() {
        return IntegerPropertyEditor.class;
    }

    @Override
    protected Class<StringPropertyRenderer> getRendererImpl() {
        return StringPropertyRenderer.class;
    }
    
}
