//  (c) 2001-2010 Fermi Research Alliance
//  $Id: AlignmentProperty.java,v 1.2 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public class AlignmentProperty extends ComponentProperty<Alignment>{

    public AlignmentProperty( String name, String caption, boolean required ) {
        super( Alignment.class, name, caption, required );
    }

    public AlignmentProperty( String name, String caption, boolean required, Alignment value ) {
        this( name, caption, required );
        setValue( value );
    }

    @Override
    public void setValueAsString( String str ) throws PropertyException {
        if (str == null || "".equals( str )) {
            setValue( null );
        } else {
            try {
                setValue( Alignment.valueOf( str ));
            } catch (IllegalArgumentException ex) {
                throw new PropertyException( "Illegal alignment value: " + str );
            }
        }
    }

    @Override
    protected Class<AlignmentPropertyEditor> getEditorImpl() {
        return AlignmentPropertyEditor.class;
    }

    @Override
    protected Class<StringPropertyRenderer> getRendererImpl() {
        return StringPropertyRenderer.class;
    }

}
