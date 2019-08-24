// (c) 2001-2010 Fermi Research Allaince
// $Id: MovePinAction.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import gov.fnal.controls.applications.syndi.builder.element.pin.Pin;
import java.awt.geom.Point2D;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public class MovePinAction extends AbstractCanvasAction {

    private final AbstractComponent comp;
    private final Pin pin;
    private final Point2D oldLocation, newLocation;

    protected MovePinAction( 
            AbstractComponent comp, 
            Pin pin, 
            Point2D location ) {
        this.comp = comp;
        this.pin = pin;
        this.oldLocation = (Point2D)pin.getLocation().clone(); 
        this.newLocation = (Point2D)location.clone();
    }

    @Override
    public void reDo() {
        moveTo( newLocation );
    }

    @Override
    public void unDo() {
        moveTo( oldLocation );
    }
    
    private void moveTo( Point2D location ) {
        pin.setLocation( location );
        canvas.repaint();
    }

}
