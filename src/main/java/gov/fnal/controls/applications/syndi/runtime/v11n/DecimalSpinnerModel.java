// (c) 2001-2010 Fermi Research Alliance
// $Id: DecimalSpinnerModel.java,v 1.2 2010/09/15 15:48:22 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

import javax.swing.SpinnerNumberModel;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:48:22 $
 */
class DecimalSpinnerModel extends SpinnerNumberModel {

    DecimalSpinnerModel() {
        super( Double.NaN, null, null, 0.0 );
    }

    @Override
    @SuppressWarnings( value="unchecked" )
    public void setValue( Object value ) {
	if (!(value instanceof Double)) {
	    throw new IllegalArgumentException();
	}
        if (!((Double)value).isNaN()) {
            Comparable min = getMinimum();
            if (min != null && min.compareTo( value ) > 0) {
                throw new OutOfRangeException();
            }
            Comparable max = getMaximum();
            if (max != null && max.compareTo( value ) < 0) {
                throw new OutOfRangeException();
            }
        }
        super.setValue( value );
    }

}
