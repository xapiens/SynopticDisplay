// (c) 2001-2010 Fermi Research Allaince
// $Id: PlaceComponentAction.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public class PlaceComponentAction extends AbstractCanvasAction {

    private static final Logger log = Logger.getLogger( PlaceComponentAction.class.getName());

    private final GenericContainer cont;
    private final List<BuilderComponent> comp;

    protected PlaceComponentAction( GenericContainer cont, List<BuilderComponent> comp ) {
        this.cont = cont;
        this.comp = new ArrayList<BuilderComponent>( comp );
    }

    @Override
    public void reDo() {
        for (BuilderComponent c : comp) {
            try {
                cont.add( c );
            } catch (RuntimeException ex) {
                log.log( Level.INFO, "Cannot place component", ex );
            }
        }
        canvas.repaint();
    }

    @Override
    public void unDo() {
        for (BuilderComponent c : comp) {
            cont.remove( c );
        }
        canvas.repaint();
    }

}
