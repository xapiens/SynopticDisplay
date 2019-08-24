// (c) 2001-2010 Fermi Research Allaince
// $Id: GenericText.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import gov.fnal.controls.applications.syndi.property.BooleanProperty;
import gov.fnal.controls.applications.syndi.property.ColorProperty;
import gov.fnal.controls.applications.syndi.property.ComponentProperty;
import gov.fnal.controls.applications.syndi.property.IntegerProperty;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.applications.syndi.property.PropertyException;
import gov.fnal.controls.applications.syndi.property.StringProperty;
import gov.fnal.controls.tools.svg.SVGColor;
import gov.fnal.controls.tools.svg.SVGFontStyle;
import gov.fnal.controls.tools.svg.SVGFontWeight;
import gov.fnal.controls.tools.svg.SVGText;
import java.awt.Color;
import java.awt.Shape;
import java.util.Collection;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public class GenericText extends GenericSVGComponent<SVGText> {
    
    public GenericText( SVGText svg ) {
        super( svg );

        props.add( new IntegerProperty( "x", "X", true, getX()));
        props.add( new IntegerProperty( "y", "Y", true, getY()));
        props.add( new IntegerProperty( "width", "Width", true, getWidth()));
        props.add( new IntegerProperty( "height", "Height", true, getHeight()));
        props.add( new ColorProperty( "fill", "Text Color", true, getTextColor()));
        props.add( new IntegerProperty( "font-size", "Font Size", true, getFontSize()));
        props.add( new BooleanProperty( "isItalic", "Italic Font", true, isItalic()));
        props.add( new BooleanProperty( "isBold", "Bold Font", true, isBold()));
        props.add( new StringProperty( "text", "Text", false, getText()));

        props.get( "x" ).setLocalOnly( true );
        props.get( "y" ).setLocalOnly( true );
        props.get( "width" ).setLocalOnly( true );
        props.get( "height" ).setLocalOnly( true );
        props.get( "isItalic" ).setLocalOnly( true );
        props.get( "isBold" ).setLocalOnly( true );

        if (svg.getStrokeColor() == null) {
            svg.setStrokeColor( SVGColor.NO_COLOR );
        }
    }
    
    public void setTextColor( Color val ) {
        svg.setFillColor( val );
    }

    public Color getTextColor() {
        return svg.getFillColor();
    }

    public void setFontSize( int val ) {
        svg.setFontSize( val );
    }

    public Integer getFontSize() {
        Number res = svg.getFoneSize();
        return (res != null) ? new Integer( res.intValue()) : null;
    }
    
    public void setItalic( boolean val ) {
        svg.setFontStyle( val ? SVGFontStyle.italic : SVGFontStyle.normal );
    }
    
    public boolean isItalic() {
        return svg.getFontStyle() == SVGFontStyle.italic;
    }

    public void setBold( boolean val ) {
        svg.setFontWeight( val ? SVGFontWeight.bold : SVGFontWeight.normal );
    }
    
    public boolean isBold() {
        return svg.getFontWeight() == SVGFontWeight.bold;
    }
    
    public void setText( String text ) {
        if (text == null || "".equals( text )) {
            text = " ";
        }
        svg.setText( text );
        recalculateBounds();
    }
    
    public String getText() {
        return svg.getText();
    }
    
    @Override
    public void setProperties( Collection<ComponentProperty<?>> val ) throws PropertyException {
        super.setProperties( val );
        setLocation( 
            props.getValue( Integer.class, "x", getX()),
            props.getValue( Integer.class, "y", getY()) 
        );
        setSize(
            props.getValue( Integer.class, "width", getWidth()),
            props.getValue( Integer.class, "height", getHeight())
        );
        Shape s0 = svg.getOutline();
        setTextColor( props.getValue( Color.class, "fill", getTextColor()));
        setFontSize( props.getValue( Integer.class, "font-size", getFontSize()));
        setItalic( props.getValue( Boolean.class, "isItalic", isItalic()));
        setBold( props.getValue( Boolean.class, "isBold", isBold()));
        setText( props.getValue( String.class, "text", getText()));
        Shape s1 = svg.getOutline();
        firePropertyChange( "shape", s0, s1 );
        repaintComponent();
    }
    
    @Override
    public Collection<ComponentProperty<?>> getProperties() {
        try {
            props.get( IntegerProperty.class, "x" ).setValue( getX());
            props.get( IntegerProperty.class, "y" ).setValue( getY());
            props.get( IntegerProperty.class, "width" ).setValue( getWidth());
            props.get( IntegerProperty.class, "height" ).setValue( getHeight());
        } catch (PropertyException ex) {
            throw new RuntimeException( ex ); // should not happen
        }
        PropertyCollection res = (PropertyCollection)props.clone();
        res.setComponent( this );
        return res;
    }

}