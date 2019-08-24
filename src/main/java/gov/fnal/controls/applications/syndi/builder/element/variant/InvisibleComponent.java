// (c) 2001-2010 Fermi Research Alliance
// $Id: InvisibleComponent.java,v 1.2 2010/09/15 16:10:15 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element.variant;

import gov.fnal.controls.applications.syndi.builder.element.GenericComponent;
import gov.fnal.controls.applications.syndi.property.ComponentProperty;
import gov.fnal.controls.applications.syndi.property.PropertyException;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.Collection;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:10:15 $
 */
public class InvisibleComponent extends GenericComponent {
    
    private static final Stroke BORDER_STROKE = new BasicStroke(
        (float)BORDER_SIZE, 
        BasicStroke.CAP_ROUND, 
        BasicStroke.JOIN_MITER, 
        10.0f,
        new float[]{ 2.0f, 2.0f },
        0.0f
    );

    private final Color bgColor;
    
    protected InvisibleComponent( Color bgColor ) {
        this.bgColor = bgColor;
    }
    
    @Override
    public Color getBackgroundColor() {
        return bgColor;
    }

    @Override
    public void setProperties( Collection<ComponentProperty<?>> value ) throws PropertyException {
        super.setProperties( value );
        props.removeByName( "background" );
    }
    
    @Override
    protected void paintBorder( Graphics2D g ) {
        g.setColor( BORDER_COLOR );
        g.setStroke( BORDER_STROKE );
        g.drawRect( 
            BORDER_SIZE / 2, 
            BORDER_SIZE / 2, 
            getWidth() - BORDER_SIZE, 
            getHeight() - BORDER_SIZE 
        );
    }

}
