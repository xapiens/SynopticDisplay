//  (c) 2001-2010 Fermi Research Alliance
//  $Id: Orientation.java,v 1.2 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public enum Orientation {
    
    NORTH( 0 ), 
    WEST( -0.5 * Math.PI ), 
    SOUTH( Math.PI ), 
    EAST( 0.5 * Math.PI );
    
    private final double angle;
    
    private Orientation( double angle ) {
        this.angle = angle;
    }
    
    public double getAngle() {
        return angle;
    }

}
