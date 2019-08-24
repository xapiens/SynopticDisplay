//  (c) 2001-2010 Fermi Research Alliance
//  $Id: DoubleProperty.java,v 1.2 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public class DoubleProperty extends ComponentProperty<Double> {

    public DoubleProperty( String name, String caption, boolean required ) {
        super( Double.class, name, caption, required );
    }

    public DoubleProperty( String name, String caption, boolean required, Double value ) {
        this( name, caption, required );
        setValue( value );
    }

    @Override
    public void setValueAsString( String str ) throws PropertyException {
        if (str == null || "".equals( str )) {
            setValue( null );
        } else {
            try {
                setValue( new Double( str ));
            } catch (NumberFormatException ex) {
                throw new PropertyException( "Illegal double value: " + str );
            }
        }
    }

    @Override
    protected Class<DoublePropertyEditor> getEditorImpl() {
        return DoublePropertyEditor.class;
    }

    @Override
    protected Class<StringPropertyRenderer> getRendererImpl() {
        return StringPropertyRenderer.class;
    }

}
