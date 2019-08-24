// (c) 2001-2010 Fermi Research Allaince
// $Id: LinkInfoPanel.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import gov.fnal.controls.applications.syndi.builder.element.pin.PinInfoPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JPanel;
import static java.awt.GridBagConstraints.*;
import static gov.fnal.controls.applications.syndi.builder.element.pin.PinRole.*;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public class LinkInfoPanel extends JPanel {

    private final PinInfoPanel srcPanel, tgtPanel;

    public LinkInfoPanel( ComponentLink link ) {
        super( new GridBagLayout());
        srcPanel = new PinInfoPanel( SOURCE, link.getPin( SOURCE ));
        tgtPanel = new PinInfoPanel( TARGET, link.getPin( TARGET ));
        add( srcPanel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 0.5,
                CENTER, BOTH, new Insets( 0, 0, 3, 0 ), 0, 0 ));
        add( tgtPanel, new GridBagConstraints( 0, 1, 1, 1, 1.0, 0.5,
                CENTER, BOTH, new Insets( 3, 0, 0, 0 ), 0, 0 ));
    }

}
