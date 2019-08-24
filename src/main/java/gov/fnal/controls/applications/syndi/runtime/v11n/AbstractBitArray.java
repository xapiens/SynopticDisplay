// (c) 2001-2010 Fermi Research Alliance
// $Id: AbstractBitArray.java,v 1.2 2010/09/15 15:48:22 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.tools.timed.TimedNumber;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Stroke;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:48:22 $
 */
public abstract class AbstractBitArray extends VisualComponent {

    private static final int DEFAULT_NUM_BITS = 0;

    protected Color borderColor, fillColor0, fillColor1;
    protected Stroke borderStroke;
    protected double borderWidth;
    protected int numBits;
    protected boolean backwards;
    protected TimedNumber value;

    protected AbstractBitArray() {}

    @Override
    public void init( PropertyCollection props ) throws Exception {
        borderColor = props.getValue( Color.class, "border" );
        borderWidth = props.getValue( Double.class, "borderWidth", 0.0 );
        borderStroke = (borderWidth > 0) ? new BasicStroke( (float)borderWidth ) : null;
        numBits = props.getValue( Integer.class, "numBits", DEFAULT_NUM_BITS );
        if (numBits <= 0) {
            numBits = DEFAULT_NUM_BITS;
        }
        backwards = props.getValue( Boolean.class, "backwards", false );
        fillColor0 = props.getValue( Color.class, "fillColor0" );
        fillColor1 = props.getValue( Color.class, "fillColor1" );
    }

    protected boolean[] getState( Number value ) {
        boolean[] res = new boolean[ numBits ];
        long vv = Math.round( value.doubleValue());
        for (int i = 0; i < numBits; i++) {
            boolean s = ((vv & 1) != 0);
            if (backwards) {
                res[ i ] = s;
            } else {
                res[ numBits - i - 1 ] = s;
            }
            vv >>= 1;
        }
        return res;
    }

    @Override
    protected void process( TimedNumber newValue ) {
        Number oldValue = this.value;
        if (newValue.equals( oldValue )) {
            return;
        }
        this.value = newValue;
        repaint();
    }

    @Override
    public abstract void paint( Graphics g );

}
