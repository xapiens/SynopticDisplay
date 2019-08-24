//  (c) 2001-2010 Fermi Research Alliance
//  $Id: URIProperty.java,v 1.2 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public class URIProperty extends ComponentProperty<URI> {

    public URIProperty( String name, String caption, boolean required ) {
        super( URI.class, name, caption, required );
    }

    public URIProperty( String name, String caption, boolean required, URI value ) {
        this( name, caption, required );
        setValue( value );
    }

    @Override
    public void setValueAsString( String str ) throws PropertyException {
        if (str == null || "".equals( str )) {
            setValue( null );
        } else {
            try {
                setValue( new URI( str ));
            } catch (URISyntaxException ex) {
                throw new PropertyException( "Illegal URI value: " + str );
            }
        }
    }

    @Override
    protected Class<URIPropertyEditor> getEditorImpl() {
        return URIPropertyEditor.class;
    }

    @Override
    protected Class<StringPropertyRenderer> getRendererImpl() {
        return StringPropertyRenderer.class;
    }

}
