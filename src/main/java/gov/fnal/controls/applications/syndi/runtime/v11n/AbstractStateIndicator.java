// (c) 2001-2010 Fermi Research Alliance
// $Id: AbstractStateIndicator.java,v 1.2 2010/09/15 15:48:22 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.tools.timed.TimedNumber;
import java.awt.Container;
import java.awt.Rectangle;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:48:22 $
 */
public abstract class AbstractStateIndicator extends VisualComponent {

    private static final double DEFAULT_MIN_VALUE = 0.0;
    private static final double DEFAULT_MAX_VALUE = 0.0;
    private static final boolean DEFAULT_INVERT = false;

    protected double minValue, maxValue;
    protected boolean invert;
    protected TimedNumber value;

    protected AbstractStateIndicator() {}

    @Override
    public void init( PropertyCollection props ) throws Exception {
        minValue = props.getValue( Double.class, "minValue", DEFAULT_MIN_VALUE );
        maxValue = props.getValue( Double.class, "maxValue", DEFAULT_MAX_VALUE );
        invert = props.getValue( Boolean.class, "invert", DEFAULT_INVERT );
    }

    public TimedNumber getValue() {
        return value;
    }

    @Override
    protected void process( TimedNumber newValue ) {
        Number oldValue = this.value;
        if (newValue.equals( oldValue )) {
            return;
        }
        this.value = newValue;
        repaintEffectiveBounds();
    }

    protected void repaintEffectiveBounds() {
        Container parent = getParent();
        if (parent == null) {
            return;
        }
        Rectangle r = getEffectiveBounds();
        parent.repaint( r.x + getX(), r.y + getY(), r.width, r.height );
    }

    protected abstract Rectangle getEffectiveBounds();

}
