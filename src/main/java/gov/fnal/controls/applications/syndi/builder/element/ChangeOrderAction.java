// (c) 2001-2010 Fermi Research Allaince
// $Id: ChangeOrderAction.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public class ChangeOrderAction extends AbstractCanvasAction {

    private final BuilderComponent comp;
    private final GenericContainer cont;
    private final int oldIndex, newIndex;
    
    protected ChangeOrderAction( BuilderComponent comp, int dir ) {
        this.comp = comp;
        BuilderContainer parent = comp.getParent();
        if (parent instanceof GenericContainer) {
            this.cont = (GenericContainer)parent;
            oldIndex = cont.indexOf( comp );
            if (oldIndex != -1) {
                if (dir < 0) {
                    newIndex = 0;
                } else if (dir > 0) {
                    newIndex = cont.getComponentCount() - 1;
                } else {
                    newIndex = oldIndex;
                }
            } else {
                newIndex = -1;
            }
        } else {
            this.cont = null;
            oldIndex = -1;
            newIndex = -1;
        } 
    }

    @Override
    public void reDo() {
        moveTo( oldIndex, newIndex );
    }

    @Override
    public void unDo() {
        moveTo( newIndex, oldIndex );
    }
    
    private void moveTo( int fromIndex, int toIndex ) {
        if (fromIndex < 0 || toIndex < 0)  {
            return;
        }
        cont.move( fromIndex, toIndex );
        comp.repaintComponent();
    }

}
