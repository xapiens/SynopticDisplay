//  (c) 2001-2010 Fermi Research Alliance
//  $Id: LineOrientationProperty.java,v 1.2 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public class LineOrientationProperty extends ComponentProperty<LineOrientation>{

    public LineOrientationProperty( String name, String caption, boolean required ) {
        super( LineOrientation.class, name, caption, required );
    }

    public LineOrientationProperty( String name, String caption, boolean required, LineOrientation value ) {
        this( name, caption, required );
        setValue( value );
    }

    @Override
    public void setValueAsString( String str ) throws PropertyException {
        if (str == null || "".equals( str )) {
            setValue( null );
        } else {
            try {
                setValue( LineOrientation.valueOf( str ));
            } catch (IllegalArgumentException ex) {
                throw new PropertyException( "Illegal line orientation value: " + str );
            }
        }
    }

    @Override
    protected Class<LineOrientationPropertyEditor> getEditorImpl() {
        return LineOrientationPropertyEditor.class;
    }

    @Override
    protected Class<StringPropertyRenderer> getRendererImpl() {
        return StringPropertyRenderer.class;
    }

}
