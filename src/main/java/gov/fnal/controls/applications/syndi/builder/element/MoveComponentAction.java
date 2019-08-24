// (c) 2001-2010 Fermi Research Allaince
// $Id: MoveComponentAction.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import java.awt.Point;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public class MoveComponentAction extends AbstractCanvasAction {

    private final GenericContainer cont;
    private final Set<BuilderComponent> comp = new HashSet<BuilderComponent>();
    private final int dx, dy;

    protected MoveComponentAction( GenericContainer cont, 
            Collection<BuilderComponent> comp, Point dp ) {
        this.cont = cont;
        for (BuilderComponent c : comp) {
            if (c.getParent() == cont) {
                this.comp.add( c );
            }
        }
        this.dx = dp.x;
        this.dy = dp.y;
    }

    @Override
    public void reDo() {
        moveBy( dx, dy );
    }

    @Override
    public void unDo() {
        moveBy( -dx, -dy );
    }
    
    private void moveBy( int dx, int dy ) {
        for (BuilderComponent c : comp) {
            c.setLocation( c.getX() + dx, c.getY() + dy );
        }
        for (BuilderComponent c : comp) {
            if (c instanceof ComponentLink) {
                ((ComponentLink)c).adjust();
            }
        }
        cont.repaintComponent();
    }

}
