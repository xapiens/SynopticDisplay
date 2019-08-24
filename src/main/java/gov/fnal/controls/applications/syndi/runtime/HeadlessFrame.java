// (c) 2001-2010 Fermi Research Allaince
// $Id: HeadlessFrame.java,v 1.2 2010/09/15 15:25:14 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime;

import gov.fnal.controls.applications.syndi.repository.DisplayParametersApplicator;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.RepaintManager;
import org.w3c.dom.Document;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:25:14 $
 */
class HeadlessFrame extends JFrame {
    
    private final RootComponent canvas = new RootComponent();
    
    HeadlessFrame( Document doc, Map<String,String> params ) throws Exception {
        RepaintManager.currentManager( this ).setDoubleBufferingEnabled( false );
        setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
        canvas.setDoubleBuffered( false );
        if (params != null) {
            DisplayParametersApplicator.apply( params, doc );
        }
        canvas.init( doc.getDocumentElement());
        getContentPane().add( canvas );
        pack();
    }

    RootComponent getCanvas() {
        return canvas;
    }
    
    @Override
    public void dispose() {
        canvas.dispose();
        super.dispose();
    }

}
