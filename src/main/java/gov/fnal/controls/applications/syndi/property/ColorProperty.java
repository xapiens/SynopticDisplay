//  (c) 2001-2010 Fermi Research Alliance
//  $Id: ColorProperty.java,v 1.2 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

import gov.fnal.controls.tools.svg.SVGColor;
import java.awt.Color;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public class ColorProperty extends ComponentProperty<Color> {

    public ColorProperty( String name, String caption, boolean required ) {
        super( Color.class, name, caption, required );
    }

    public ColorProperty( String name, String caption, boolean required, Color value ) {
        this( name, caption, required );
        setValue( value );
    }

    @Override
    public void setValueAsString( String str ) throws PropertyException {
        if (str == null || "".equals( str ) || "none".equals( str )) {
            setValue( null );
        } else {
            try {
                setValue( SVGColor.parseColor( str ));
            } catch (IllegalArgumentException ex) {
                throw new PropertyException( "Illegal color value: " + str );
            }
        }
    }

    @Override
    public String getValueAsString() {
        return SVGColor.toString( getValue());
    }

    @Override
    public void setValue( Color value ) {
        super.setValue( value == SVGColor.NO_COLOR ? null : value );
    }

    @Override
    protected Class<ColorPropertyEditor> getEditorImpl() {
        return ColorPropertyEditor.class;
    }

    @Override
    protected Class<ColorPropertyRenderer> getRendererImpl() {
        return ColorPropertyRenderer.class;
    }

}
