// (c) 2001-2010 Fermi Research Allaince
// $Id: ResizeComponentAction.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public class ResizeComponentAction extends AbstractCanvasAction {

    private final AbstractComponent comp;
    private final int dx, dy, dw, dh;

    protected ResizeComponentAction( 
            AbstractComponent comp, 
            int dx, 
            int dy, 
            int dw, 
            int dh ) {
        this.comp = comp;
        this.dx = dx;
        this.dy = dy;
        this.dw = dw;
        this.dh = dh;
    }

    @Override
    public void reDo() {
        resize( dx, dy, dw, dh );
    }

    @Override
    public void unDo() {
        resize( -dx, -dy, -dw, -dh );
    }
    
    private void resize( int dx, int dy, int dw, int dh ) {
        comp.setLocation( comp.getX() + dx, comp.getY() + dy );
        comp.setSize( comp.getWidth() + dw, comp.getHeight() + dh );
        canvas.repaint();
    }

}
