// (c) 2001-2010 Fermi Research Allaince
// $Id: GenericShape.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import gov.fnal.controls.applications.syndi.property.ColorProperty;
import gov.fnal.controls.applications.syndi.property.ComponentProperty;
import gov.fnal.controls.applications.syndi.property.DoubleProperty;
import gov.fnal.controls.applications.syndi.property.IntegerProperty;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.applications.syndi.property.PropertyException;
import gov.fnal.controls.tools.svg.SVGColor;
import gov.fnal.controls.tools.svg.SVGComponent;
import java.awt.Color;
import java.util.Collection;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public class GenericShape extends GenericSVGComponent<SVGComponent> {
    
    public GenericShape( SVGComponent svg ) {
        super( svg );

        props.add( new IntegerProperty( "x", "X", true, getX()));
        props.add( new IntegerProperty( "y", "Y", true, getY()));
        props.add( new IntegerProperty( "width", "Width", true, getWidth()));
        props.add( new IntegerProperty( "height", "Height", true, getHeight()));
        props.add( new ColorProperty( "fill", "Fill Color", false, getFillColor()));
        props.add( new ColorProperty( "stroke", "Stroke Color", false, getStrokeColor()));
        props.add( new DoubleProperty( "stroke-width", "Stroke Width", true, getStrokeWidth()));

        props.get( "x" ).setLocalOnly( true );
        props.get( "y" ).setLocalOnly( true );
        props.get( "width" ).setLocalOnly( true );
        props.get( "height" ).setLocalOnly( true );
        
        if (svg.getFillColor() == null) {
            svg.setFillColor( SVGColor.NO_COLOR );
        }
    }
    
    public void setFillColor( Color val ) {
        svg.setFillColor( val );
    }
    
    public Color getFillColor() {
        return svg.getFillColor();
    }

    public void setStrokeColor( Color val ) {
        svg.setStrokeColor( val );
    }

    public Color getStrokeColor() {
        return svg.getStrokeColor();
    }

    public void setStrokeWidth( double width ) {
        svg.setStrokeWidth( width );
    }
    
    public Double getStrokeWidth() {
        Number width = svg.getStrokeWidth();
        return (width == null) ? null : new Double( width.doubleValue());
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
        setStrokeColor( props.getValue( Color.class, "stroke", SVGColor.NO_COLOR ));
        setFillColor( props.getValue( Color.class, "fill", SVGColor.NO_COLOR ));
        setStrokeWidth( props.getValue( Double.class, "stroke-width", getStrokeWidth()));
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
