// (c) 2001-2010 Fermi Research Allaince
// $Id: ReshapeAction.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import java.awt.geom.Path2D;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public class ReshapeAction extends AbstractCanvasAction {

    private final GenericLine figure;
    private final Path2D oldPath, newPath;

    protected ReshapeAction( GenericLine figure, Path2D oldPath, Path2D newPath ) {
        this.figure = figure;
        this.oldPath = oldPath;
        this.newPath = newPath;
    }

    @Override
    public void reDo() {
        figure.setPath( newPath );
        canvas.repaint();
    }

    @Override
    public void unDo() {
        figure.setPath( oldPath );
        canvas.repaint();
    }

}
