// (c) 2001-2010 Fermi Research Allaince
// $Id: RotateAction.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public class RotateAction extends AbstractCanvasAction {

    private final GenericSVGComponent comp;
    private final int quad;

    public RotateAction( GenericSVGComponent comp, int quad ) {
        this.comp = comp;
        this.quad = quad;
    }

    @Override
    public void unDo() {
        comp.applyRotation( -quad );
        canvas.repaint();
    }

    @Override
    public void reDo() {
        comp.applyRotation( quad );
        canvas.repaint();
    }

}
