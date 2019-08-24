// (c) 2001-2010 Fermi Research Allaince
// $Id: AbstractCanvasAction.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import gov.fnal.controls.applications.syndi.builder.CanvasAction;
import gov.fnal.controls.applications.syndi.builder.DrawingCanvas;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public abstract class AbstractCanvasAction implements CanvasAction {

    protected DrawingCanvas canvas;
    
    protected AbstractCanvasAction() {
    }

    public void setCanvas( DrawingCanvas canvas ) {
        this.canvas = canvas;
    }

    public DrawingCanvas getCanvas() {
        return canvas;
    }

}
