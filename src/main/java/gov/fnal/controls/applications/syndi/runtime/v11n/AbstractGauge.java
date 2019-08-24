// (c) 2001-2010 Fermi Research Alliance
// $Id: AbstractGauge.java,v 1.2 2010/09/15 15:48:22 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.applications.syndi.property.PropertyException;
import gov.fnal.controls.applications.syndi.util.DecimalFormatFactory;
import gov.fnal.controls.tools.timed.TimedNumber;
import java.awt.Color;
import java.awt.Rectangle;
import java.text.DecimalFormat;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:48:22 $
 */
public abstract class AbstractGauge extends VisualComponent {
    
    private static final double DEFAULT_MIN_VALUE = 0.0;
    private static final double DEFAULT_MAX_VALUE = 1.0;
    
    private TimedNumber value;

    protected DecimalFormat format;
    protected Color borderColor, alarmColor;
    protected double minValue, maxValue, minLimit, maxLimit;
    protected String text, displayText, minLimitStr, maxLimitStr;
    protected int tickCount;
    
    protected AbstractGauge() {}
    
    @Override
    public void init( PropertyCollection props ) throws Exception {
        
        String str = props.findValue( String.class, "format", "vformat", "vFormat" );
        format = (str == null) ? null : DecimalFormatFactory.createFormat( str );
        
        borderColor = getBorderColor( props );
        alarmColor = props.getValue( Color.class, "alarmColor" );
        
        minValue = props.getValue( Double.class, "min", DEFAULT_MIN_VALUE );
        maxValue = props.getValue( Double.class, "max", DEFAULT_MAX_VALUE );
        
        if (minValue >= maxValue) {
            // reset to defaults
            minValue = DEFAULT_MIN_VALUE;
            maxValue = DEFAULT_MAX_VALUE;
        }
        
        minLimit = props.getValue( Double.class, "alarmMin", minValue );
        if (minLimit < minValue || minLimit >= maxValue) {
            // disable min limit
            minLimit = minValue; 
        }
        
        maxLimit = props.getValue( Double.class, "alarmMax", maxValue );
        if (maxLimit > maxValue || maxLimit <= minValue) {
            // disable max limit
            maxLimit = maxValue; 
        }
        
        if (minLimit >= maxLimit || alarmColor == null) {
            // disable both limits
            minLimit = minValue;
            maxLimit = maxValue;
        }
        
        if (format != null) {
            minLimitStr = format.format( minLimit );
            maxLimitStr = format.format( maxLimit );
        } else {
            minLimitStr = null;
            maxLimitStr = null;
        }
        
        Integer tickCount_ = props.getValue( Integer.class, "tickCount" );
        if (tickCount_ == null) {
            Double step_ = props.getValue( Double.class, "step" );
            if (step_ != null) {
                tickCount = (int)Math.round((maxValue - minValue) / step_.doubleValue()) - 1;
            } else {
                tickCount = 0;
            }
        } else {
            tickCount = tickCount_.intValue();
        }
        
        text = props.getValue( String.class, "text", "" );
        
    }
    
    private Color getBorderColor( PropertyCollection props ) {
        try {
            return props.findValue( Color.class, "borderColor", "fcolor", "fColor" );
        } catch (PropertyException ex) {
            return null;
        }
    }

    public TimedNumber getValue() {
        return value;
    }
    
    @Override
    protected void process( TimedNumber newValue ) {
        if (displayText == null) {
            String dataTag = getDataTag( 0 );
            displayText = text.replace( "\\$", (dataTag == null) ? "" : dataTag );
        }
        Number oldValue = this.value;
        if (newValue.equals( oldValue )) {
            return;
        }
        this.value = newValue;
        repaint( computeRepaint( oldValue, newValue ));
    }
    
    protected abstract Rectangle computeRepaint( Number oldValue, Number newValue );

}
