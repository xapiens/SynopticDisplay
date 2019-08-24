// (c) 2001-2010 Fermi Research Allaince
// $Id: SelectComponentAction.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public class SelectComponentAction extends AbstractCanvasAction {

    private final GenericContainer cont;
    private final List<BuilderComponent> sel0 = new ArrayList<BuilderComponent>();
    private final List<BuilderComponent> sel1 = new ArrayList<BuilderComponent>();

    protected SelectComponentAction( GenericContainer cont, List<BuilderComponent> selection ) {
        this.cont = cont;
        sel0.addAll( cont.getSelection());
        if (selection != null) {
            sel1.addAll( selection );
        }
    }

    @Override
    public void reDo() {
        changeSelection( sel1 );
    }

    @Override
    public void unDo() {
        changeSelection( sel0 );
    }

    private void changeSelection( List<BuilderComponent> selection ) {
        cont.select( selection );
        canvas.repaint();
    }
}
