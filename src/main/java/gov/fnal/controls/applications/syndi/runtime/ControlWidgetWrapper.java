// (c) 2001-2010 Fermi Research Allaince
// $Id: ControlWidgetWrapper.java,v 1.2 2010/09/15 15:25:14 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JPanel;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:25:14 $
 */
public class ControlWidgetWrapper extends JPanel {

    private static final Dimension SIZE = new Dimension( 32, 16 );

    public ControlWidgetWrapper() {
        setMinimumSize( SIZE );
        setMaximumSize( SIZE );
        setPreferredSize( SIZE );
        setLayout( new BorderLayout());
    }

    public void setWidget( Component comp ) {
        add( comp, BorderLayout.CENTER );
    }

    public void clearWidget() {
        removeAll();
    }

}
