// (c) 2001-2010 Fermi Research Allaince
// $Id: ResizeSVGAction.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import java.awt.Point;
import java.awt.Rectangle;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public class ResizeSVGAction extends AbstractCanvasAction {

    private final GenericSVGComponent<?> comp;
    private final Rectangle oldBounds, newBounds;
    private final Point oldLocation, newLocation;

    protected ResizeSVGAction(
            GenericSVGComponent<?> comp,
            Rectangle newBounds,
            Point newLocation ) {
        this.comp = comp;
        this.oldBounds = comp.getBounds();
        this.newBounds = newBounds;
        this.oldLocation = new Point( comp.getX(), comp.getY());
        this.newLocation = newLocation;
    }

    @Override
    public void reDo() {
        comp.setBounds( newBounds );
        comp.setLocation( newLocation.x, newLocation.y );
        canvas.repaint();
    }

    @Override
    public void unDo() {
        comp.setLocation( oldLocation.x, oldLocation.y );
        comp.setBounds( oldBounds );
        canvas.repaint();
    }

}
