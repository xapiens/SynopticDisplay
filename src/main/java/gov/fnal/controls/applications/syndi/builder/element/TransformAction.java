// (c) 2001-2010 Fermi Research Allaince
// $Id: TransformAction.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import java.awt.geom.AffineTransform;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public class TransformAction extends AbstractCanvasAction {

    private final GenericSVGComponent comp;
    private final AffineTransform newXform, oldXform;

    protected TransformAction( GenericSVGComponent comp, 
            AffineTransform oldXform, AffineTransform newXform ) {
        this.comp = comp;
        this.oldXform = new AffineTransform( oldXform );
        this.newXform = new AffineTransform( newXform );
    }

    @Override
    public void unDo() {
        comp.setTransform( oldXform );
        canvas.repaint();
    }

    @Override
    public void reDo() {
        comp.setTransform( newXform );
        canvas.repaint();
    }

}
