//  (c) 2001-2010 Fermi Research Alliance
//  $Id: StringProperty.java,v 1.2 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public class StringProperty extends ComponentProperty<String> {

    public StringProperty( String name, String caption, boolean required ) {
        super( String.class, name, caption, required );
    }

    public StringProperty( String name, String caption, boolean required, String value ) {
        this( name, caption, required );
        setValue( value );
    }

    @Override
    public void setValueAsString( String str ) {
        if (str == null || "".equals( str )) {
            setValue( null );
        } else {
            setValue( str );
        }
    }

    @Override
    protected Class<StringPropertyEditor> getEditorImpl() {
        return StringPropertyEditor.class;
    }

    @Override
    protected Class<StringPropertyRenderer> getRendererImpl() {
        return StringPropertyRenderer.class;
    }
    
}
