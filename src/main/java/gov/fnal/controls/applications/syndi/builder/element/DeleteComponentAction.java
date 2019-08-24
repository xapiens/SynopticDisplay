// (c) 2001-2010 Fermi Research Allaince
// $Id: DeleteComponentAction.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public class DeleteComponentAction extends AbstractCanvasAction {

    private final GenericContainer cont;
    private final SortedMap<Integer,BuilderComponent> components = 
            new TreeMap<Integer,BuilderComponent>();
    private final SortedMap<Integer,BuilderComponent> links = 
            new TreeMap<Integer,BuilderComponent>();

    protected DeleteComponentAction( GenericContainer cont, 
            Collection<BuilderComponent> comp ) {
        this.cont = cont;
        for (BuilderComponent c : comp) {
            if (c.getParent() != cont) {
                continue;
            }
            if (c instanceof ComponentLink) {
                links.put( cont.indexOf( c ), c );
            } else {
                components.put( cont.indexOf( c ), c );
                if (c instanceof AbstractComponent) {
                    for (ComponentLink l : ((AbstractComponent)c).links()) {
                        links.put( cont.indexOf( l ), l );
                    }
                }
            }
        }
    }

    @Override
    public void reDo() {
        for (BuilderComponent c : components.values()) {
            cont.remove( c );
        }
        for (BuilderComponent c : links.values()) {
            cont.remove( c );
        }
        canvas.repaint();
    }

    @Override
    public void unDo() {
        int n = links.size();
        for (Integer i : components.keySet()) {
            cont.add( components.get( i ), i - n );
        }
        for (Integer i : links.keySet()) {
            cont.add( links.get( i ), i );
        }
        canvas.repaint();
    }

}
