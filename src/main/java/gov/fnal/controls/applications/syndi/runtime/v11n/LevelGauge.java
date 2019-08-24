// (c) 2001-2010 Fermi Research Alliance
// $Id: LevelGauge.java,v 1.3 2010/09/15 16:36:30 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

/**
 * Level gauge. Displays the value as a level of liquid in a tank.
 * 
 * @author Andrey Petrov, Timofei Bolshakov
 * @version $Date: 2010/09/15 16:36:30 $
 */
@DisplayElement(

    name            = "Level Gauge",
    description     = "Displays the value as a level of liquid in a tank",
    group           = "Gauges",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.VisualComponent",
    helpUrl         = "/LevelGauge",

    properties = {
        @Property( caption="Width",                 name="width",       value="80",         type=Integer.class                 ),
        @Property( caption="Height",                name="height",      value="120",        type=Integer.class                 ),
        @Property( caption="Background",            name="background",  value="",           type=Color.class,   required=false ),
        @Property( caption="Text Color",            name="textColor",   value="navy",       type=Color.class                   ),
        @Property( caption="Fill Color",            name="fillColor",   value="aquamarine", type=Color.class                   ),
        @Property( caption="Alarm Color",           name="alarmColor",  value="orangered",  type=Color.class,   required=false ), 
        @Property( caption="Border Color",          name="borderColor", value="black",      type=Color.class                   ),
        @Property( caption="Font Size",             name="fontSize",    value="12",         type=Integer.class                 ),
        @Property( caption="Italic Font",           name="isItalicFont",value="false",      type=Boolean.class                 ),
        @Property( caption="Bold Font",             name="isBoldFont",  value="false",      type=Boolean.class                 ),
        @Property( caption="Decimal Format",        name="format",      value="#0.0",                           required=false ),
        @Property( caption="Minimum Value",         name="min",         value="0",          type=Double.class                  ),
        @Property( caption="Minimum Normal Value",  name="alarmMin",    value="",           type=Double.class,  required=false ),
        @Property( caption="Maximum Value",         name="max",         value="1",          type=Double.class                  ),
        @Property( caption="Maximum Normal Value",  name="alarmMax",    value="",           type=Double.class,  required=false ),
        @Property( caption="Number of Ticks",       name="tickCount",   value="0",          type=Integer.class, required=false ),
        @Property( caption="Text",                  name="text",        value="\\$",                            required=false )
    },

    minInputs = 1,
    maxInputs = 1,
    minOutputs = 0,
    maxOutputs = 64,
    
    inputs = {
        @Pin( number=1, x=0, y=0.5 )
    }

)
        
public class LevelGauge extends AbstractGauge {
    
    private static final int BORDER_SIZE = 2;
    private static final int TICK_SIZE = 8;
    
    private static final Stroke BORDER_STROKE = new BasicStroke( BORDER_SIZE );
    private static final Stroke DEFAULT_STROKE = new BasicStroke( 1 );
    
    protected Color fillColor;

    private Area border;
    private Line2D minLimitLine, maxLimitLine;
    
    public LevelGauge() {}

    @Override
    public void init( PropertyCollection props ) throws Exception {
        super.init( props );
        fillColor = props.getValue( Color.class, "fillColor" );
        border = createBorder();
        minLimitLine = createLimit( minLimit );
        maxLimitLine = createLimit( maxLimit );
    }
    
    private Area createBorder() {
        float r = Math.min( getWidth(), getHeight()) / 4f;        
        float x0 = BORDER_SIZE / 2f;
        float x1 = getWidth() - BORDER_SIZE;
        float y0 = BORDER_SIZE / 2f;
        float y1 = getHeight() - BORDER_SIZE;
        GeneralPath p = new GeneralPath();
        p.moveTo( x1 - r, y1 );
        p.quadTo( x1, y1, x1, y1 - r );
        p.lineTo( x1, y0 + r );
        p.quadTo( x1, y0, x1 - r, y0 );
        p.lineTo( x0 + r, y0 );
        p.quadTo( x0, y0, x0, y0 + r );
        p.lineTo( x0, y1 - r );
        p.quadTo( x0, y1, x0 + r, y1 );
        p.closePath();
        return new Area( p );
    }
    
    private Line2D createLimit( double value ) {
        float y = getLevel( value );
        float x0 = 0;
        float x1 = getWidth();
        GeneralPath p = new GeneralPath();
        p.moveTo( x0, y - 0.1f );
        p.lineTo( x1, y - 0.1f );
        p.lineTo( x1, y + 0.1f );
        p.lineTo( x0, y + 0.1f );
        p.closePath();
        Area res = new Area( p );
        res.intersect( border );
        Rectangle r = res.getBounds();
        return new Line2D.Float( r.x, r.y, r.x + r.width, r.y );
    }
    
    private Area createFilling( double value ) {
        int y = getLevel( value );
        int w = getWidth() - 2 * BORDER_SIZE;
        int h = getHeight() - y - BORDER_SIZE;
        Area res = new Area( new Rectangle( BORDER_SIZE, y, w, h ));
        res.intersect( border );
        return res;
    }
    
    private int getLevel( double value ) {
        if (value < minValue) {
            value = minValue;
        } else if (value > maxValue) {
            value = maxValue;
        }
        int h = getHeight() - 2 * BORDER_SIZE;
        double c = 1 - (value - minValue) / (maxValue - minValue);
        return (int)Math.round( c * h + BORDER_SIZE );
    }

    @Override
    protected Rectangle computeRepaint( Number oldValue, Number newValue ) {
        if (oldValue == null || newValue == null) {
            return new Rectangle( getSize());
        }
        double v0 = oldValue.doubleValue();
        double v1 = newValue.doubleValue();
        if (Double.isNaN( v0 ) || Double.isNaN( v1 )) {
            return new Rectangle( getSize());
        }
        int y1 = getLevel( v0 );
        int y2 = getLevel( v1 );
        int w = getWidth() - 2 * BORDER_SIZE;
        int h = Math.abs( y1 - y2 ) + 1;
        int y = Math.min( y1, y2 );
        return new Rectangle( BORDER_SIZE, y, w, h );
    }
    
    @Override
    public void paint( Graphics g ) {
        
        Graphics2D g2 = (Graphics2D)g;
        g2.setFont( getFont());
        FontMetrics fm = g2.getFontMetrics();
        
        g2.setStroke( DEFAULT_STROKE );
        
        if (isBackgroundSet()) {
            g2.setColor( getBackground());
            g2.fill( border );
        }

        if (bgImage != null) {
            Rectangle r = border.getBounds();
            g2.drawImage( bgImage, 0, 0, r.width, r.height, null, null );
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
        
        if (minLimit > minValue) {
            g2.setColor( alarmColor );
            g2.draw( minLimitLine );
        } else {
            g2.setColor( getForeground());
        }
        
        if (minLimitStr != null) {
            int x = (int)minLimitLine.getX1() + 2 * BORDER_SIZE; 
            int y = (int)minLimitLine.getY1() - fm.getMaxDescent();
            g2.drawString( minLimitStr, x, y );
        }
        
        if (maxLimit < maxValue) {
            g2.setColor( alarmColor );
            g2.draw( maxLimitLine );
        } else {
            g2.setColor( getForeground());
        }
        
        if (maxLimitStr != null) {
            Rectangle2D r = fm.getStringBounds( maxLimitStr, g );
            int x = (int)maxLimitLine.getX2() - (int)r.getWidth() - 2 * BORDER_SIZE; 
            int y = (int)maxLimitLine.getY1() + (int)r.getHeight();
            g2.drawString( maxLimitStr, x, y );
        }
        
        if (borderColor != null && BORDER_SIZE > 0) {
            g2.setColor( borderColor );
            g2.setStroke( BORDER_STROKE );
            g2.draw( border );
        }
        
        g2.setStroke( DEFAULT_STROKE );

        if (borderColor != null && tickCount > 0) {
            g2.setColor( borderColor );
            int h = getHeight() - 2 * BORDER_SIZE;
            float step = (float)h / (tickCount + 1);
            float y = getHeight() - BORDER_SIZE - step;
            int x0 = (getWidth() - TICK_SIZE) / 2;
            int x1 = (getWidth() + TICK_SIZE) / 2;
            for (int i = 0; i < tickCount; i++) {
                g2.drawLine( x0, (int)y, x1, (int)y );
                y -= step;
            }
        }

        if (displayText != null) {
            g2.setColor( getForeground());
            Rectangle2D r = fm.getStringBounds( displayText, g );
            int x = (getWidth() - (int)r.getWidth()) / 2;
            int y = (getHeight() - (int)r.getHeight()) / 2 - (int)r.getY();
            g2.drawString( displayText, x, y );
        }
        
        if (error) {
            g2.setColor( ERROR_CROSS_COLOR );
            g2.drawLine( 0, 0, getWidth() - 1, getHeight() - 1 );
            g2.drawLine( 0, getHeight() - 1, getWidth() - 1, 0 );
        }

    }
    
}
