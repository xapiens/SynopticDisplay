// (c) 2001-2010 Fermi Research Alliance
// $Id: AbstractBarGauge.java,v 1.2 2010/09/15 15:48:22 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:48:22 $
 */
public abstract class AbstractBarGauge extends AbstractGauge {

    protected int borderWidth;
    protected Stroke borderStroke;
    protected Color fillColor;

    protected AbstractBarGauge() {}

    @Override
    protected Rectangle computeRepaint( Number oldValue, Number newValue ) {
        return new Rectangle();
    }

    @Override
    public void init( PropertyCollection props ) throws Exception {
        super.init( props );
        double borderWidthDouble = props.getValue( Double.class, "borderWidth", 0.0 );
        borderStroke = (borderWidthDouble > 0) ? new BasicStroke( (float)borderWidthDouble ) : null;
        borderWidth = (borderWidthDouble > 0) ? (int)Math.round( borderWidthDouble ) : 0;
        fillColor = props.getValue( Color.class, "fillColor" );
    }
    
    protected abstract Shape createFilling( double value );

    @Override
    public void paint( Graphics g ) {

        Graphics2D g2 = (Graphics2D)g;

        int width = getWidth();
        int height = getHeight();

        if (isBackgroundSet()) {
            g2.setColor( getBackground());
            g2.fill( new Rectangle( 0, 0, width, height ));
        }

        if (bgImage != null) {
            g2.drawImage( bgImage, 0, 0, width, height, null, null );
        }

        boolean error = false;

        if (fillColor != null) {
            Number value = getValue();
            if (value != null) {
                double v = value.doubleValue();
                if (Double.isNaN( v )) {
                    error = true;
                } else {
                    g2.setColor( fillColor );
                    g2.fill( createFilling( v ));
                }
            }
        }

        if (borderColor != null && borderStroke != null) {
            g2.setColor( borderColor );
            g2.setStroke( borderStroke );
            g2.drawRect(
                borderWidth / 2,
                borderWidth / 2,
                width - borderWidth,
                height - borderWidth
            );
        }

        if (error) {
            g2.setColor( ERROR_CROSS_COLOR );
            g2.drawLine( 0, 0, width - 1, height - 1 );
            g2.drawLine( 0, height - 1, width - 1, 0 );
        }

    }

}
