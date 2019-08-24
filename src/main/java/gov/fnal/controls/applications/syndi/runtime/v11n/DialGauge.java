// (c) 2001-2010 Fermi Research Alliance
// $Id: DialGauge.java,v 1.3 2010/09/15 16:36:30 apetrov Exp $
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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

/**
 * Dial gauge. Displays the value with a handle on a dial.
 * 
 * @author Andrey Petrov, Timofei Bolshakov
 * @version $Date: 2010/09/15 16:36:30 $
 */
@DisplayElement(

    name            = "Dial Gauge",
    description     = "Displays the value with a handle on a dial",
    group           = "Gauges",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.VisualComponent",
    helpUrl         = "/DialGauge",

    properties = {
        @Property( caption="Width",                 name="width",       value="100",        type=Integer.class                 ),
        @Property( caption="Height",                name="height",      value="100",        type=Integer.class                 ),
        @Property( caption="Background",            name="background",  value="",           type=Color.class,   required=false ),
        @Property( caption="Text Color",            name="textColor",   value="navy",       type=Color.class                   ),
        @Property( caption="Handle Color",          name="handleColor", value="slategray",  type=Color.class                   ),
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
        @Property( caption="Number of Ticks",       name="tickCount",   value="5",          type=Integer.class, required=false ),
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

public class DialGauge extends AbstractGauge {
    
    private static final int BORDER_SIZE = 2;
    private static final int SCALE_SIZE = 3;
    
    private static final double SCALE_POSITION = 0.8;
    private static final double HANDLE_BALANCE = 0.15;
    
    private static final Stroke BORDER_STROKE = new BasicStroke( BORDER_SIZE );
    private static final Stroke SCALE_STROKE = new BasicStroke( SCALE_SIZE );
    private static final Stroke DEFAULT_STROKE = new BasicStroke( 1 );
    
    protected Color handleColor;

    private Area border;
    private Shape ticks, minLimitLine, maxLimitLine;
    private double minAngle, maxAngle;
    private Point p0, minLimitPoint, maxLimitPoint;
    private Rectangle textArea;
    
    public DialGauge() {}

    @Override
    public void init( PropertyCollection props ) throws Exception {
        super.init( props );
        handleColor = props.getValue( Color.class, "handleColor" );
        if (handleColor == null) {
            handleColor = borderColor;
        }
        int th = (int)(getFont().getSize() * 1.25);
        textArea = new Rectangle( 0, getHeight() - th, getWidth(), th );
        p0 = new Point( getWidth() / 2, getHeight() / 2 );
        border = createBorder();
        ticks = createTicks();
        minLimitPoint = getScalePoint( minLimit );
        maxLimitPoint = getScalePoint( maxLimit );
        minLimitLine = createLimit( minValue, minLimit );
        maxLimitLine = createLimit( maxLimit, maxValue );
    }
    
    private Area createBorder() {
        double q = textArea.getHeight() / getHeight();
        double a = Math.asin( 1 - 2 * q );
        minAngle = -a * 0.9;
        maxAngle = Math.PI + a * 0.9;
        return new Area( new Arc2D.Double(
            BORDER_SIZE / 2,
            BORDER_SIZE / 2,
            getWidth() - BORDER_SIZE,
            getHeight() - BORDER_SIZE,
            180 * (-a) / Math.PI,
            180 * (Math.PI + 2 * a) / Math.PI,
            Arc2D.CHORD
        ));
    }
    
    private Shape createTicks() {
        GeneralPath res = new GeneralPath();
        res.append( createTick( minValue ), false );
        res.append( createTick( maxValue ), false );
        if (tickCount > 0) {
            double step = (maxValue - minValue) / (tickCount + 1);
            double val = minValue + step;
            for (int i = 0; i < tickCount; i++) {
                res.append( createTick( val ), false );
                val += step;
            }
        }
        res.append( new Ellipse2D.Float( p0.x - 2, p0.x - 2, 5, 5 ), false );
        return res;
    }
    
    private Shape createTick( double value ) {
        Point p = getScalePoint( value );
        int r = SCALE_SIZE / 2;
        return new Rectangle( p.x - r, p.y - r, SCALE_SIZE, SCALE_SIZE );
    }
    
    private Shape createHandle( double value ) {
        Point p1 = getScalePoint( value );
        int x2 = p0.x - (int)((p1.x - p0.x) * HANDLE_BALANCE);
        int y2 = p0.y - (int)((p1.y - p0.y) * HANDLE_BALANCE);
        int x3 = p0.x - p0.y + y2;
        int y3 = p0.x + p0.y - x2;
        int x4 = p0.y + p0.x - y2;
        int y4 = p0.y - p0.x + x2;
        int x5 = 2 * x2 - p0.x;
        int y5 = 2 * y2 - p0.y;
        GeneralPath res = new GeneralPath();
        res.moveTo( p1.x, p1.y );
        res.lineTo( x3, y3 );
        res.quadTo( x5, y5, x4, y4 );
        res.closePath();
        return res;
    }
    
    private Shape createLimit( double val1, double val2 ) {
        double a1 = getAngle( val1 );
        double a2 = getAngle( val2 );
        return new Arc2D.Double( 
            p0.x * ( 1 - SCALE_POSITION ),
            p0.y * ( 1 - SCALE_POSITION ),
            2 * p0.x * SCALE_POSITION,
            2 * p0.y * SCALE_POSITION,
            180 * a2 / Math.PI,
            180 * (a1 - a2) / Math.PI,
            Arc2D.OPEN
        );
    }
    
    private double getAngle( double value ) {
        if (value < minValue) {
            value = minValue;
        } else if (value > maxValue) {
            value = maxValue;
        }
        double c = (value - minValue) / (maxValue - minValue);
        return maxAngle - (maxAngle - minAngle) * c;
    }
    
    private Point getScalePoint( double value ) {
        double a = getAngle( value );
        double x = p0.x * ( 1 + SCALE_POSITION * Math.cos( a ));
        double y = p0.y * ( 1 - SCALE_POSITION * Math.sin( a ));
        return new Point( (int)x, (int)y );
    }
    
    @Override
    protected Rectangle computeRepaint( Number oldValue, Number newValue ) {
        return new Rectangle( getSize());
    }
    
    @Override
    public void paint( Graphics g ) {

        Graphics2D g2 = (Graphics2D)g;
        g2.setFont( getFont());
        FontMetrics fm = g2.getFontMetrics();
        
        if (isBackgroundSet()) {
            g2.setColor( getBackground());
            g2.fill( border );
        }

        if (bgImage != null) {
            Rectangle r = border.getBounds();
            g2.drawImage( bgImage, 0, 0, r.width, r.height, null, null );
        }
        
        if (minLimit > minValue) {
            g2.setColor( alarmColor );
            g2.setStroke( SCALE_STROKE );
            g2.draw( minLimitLine );
        } else {
            g2.setColor( getForeground());
        }
        
        g2.setStroke( DEFAULT_STROKE );
        
        if (minLimitStr != null) {
            Rectangle2D r = fm.getStringBounds( minLimitStr, g );
            int x = minLimitPoint.x + SCALE_SIZE + 1;
            int y = minLimitPoint.y + (int)r.getHeight() / 2;
            g2.drawString( minLimitStr, x, y );
        }
        
        if (maxLimit < maxValue) {
            g2.setStroke( SCALE_STROKE );
            g2.setColor( alarmColor );
            g2.draw( maxLimitLine );
        } else {
            g2.setColor( getForeground());
        }

        g2.setStroke( DEFAULT_STROKE );

        if (maxLimitStr != null) {
            Rectangle2D r = fm.getStringBounds( maxLimitStr, g );
            int x = maxLimitPoint.x - (int)r.getWidth() - SCALE_SIZE - 1;
            int y = maxLimitPoint.y + (int)r.getHeight() / 2;
            g2.drawString( maxLimitStr, x, y );
        }
        
        boolean error = false;
        
        if (handleColor != null) {
            Number value = getValue();
            if (value != null) {
                double v = value.doubleValue();
                if (Double.isNaN( v )) {
                    error = true;
                } else {
                    g2.setColor( handleColor );
                    g2.fill( createHandle( v ));
                }
            }
        }
        
        if (borderColor != null) {
            g2.setColor( borderColor );
            g2.fill( ticks );
        }

        if (borderColor != null && BORDER_SIZE > 0) {
            g2.setColor( borderColor );
            g2.setStroke( BORDER_STROKE );
            g2.draw( border );
        }
        
        g2.setStroke( DEFAULT_STROKE );

        if (displayText != null) {
            g2.setColor( getForeground());
            Rectangle2D r = fm.getStringBounds( displayText, g );
            int x = (getWidth() - (int)r.getWidth()) / 2;
            int y = textArea.y + (textArea.height - (int)r.getHeight()) / 2 - (int)r.getY();
            g2.drawString( displayText, x, y );
        }
        
        if (error) {
            g2.setColor( ERROR_CROSS_COLOR );
            g2.drawLine( 0, 0, getWidth() - 1, getHeight() - 1 );
            g2.drawLine( 0, getHeight() - 1, getWidth() - 1, 0 );
        }
        
    }

}
