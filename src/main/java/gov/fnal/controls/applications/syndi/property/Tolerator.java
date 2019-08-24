//  (c) 2001-2010 Fermi Research Alliance
//  $Id: Tolerator.java,v 1.2 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

import java.util.Comparator;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public class Tolerator implements Comparator<Double> {

    private final double tolerance;

    public Tolerator( double tolerance ) {
        this.tolerance = Math.abs( tolerance );
    }

    public double getTolerance() {
        return tolerance;
    }

    @Override
    public int compare( Double val1, Double val2 ) {
        double dif = val1 - val2;
        if (Math.abs( dif ) <= tolerance) {
            return 0;
        } else if (dif < 0) {
            return -1;
        } else {
            return 1;
        }
    }

}
