// (c) 2001-2010 Fermi Research Alliance
// $Id: RecentDisplayAction.java,v 1.2 2010/09/15 15:53:52 apetrov Exp $
package gov.fnal.controls.applications.syndi.repository;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:53:52 $
 */
class RecentDisplayAction extends AbstractAction {

    final DisplayHandler handler;
    final DisplaySource<?> disp;

    RecentDisplayAction( DisplayHandler handler, DisplaySource<?> disp ) {
        super( String.valueOf( disp.getSource()));
        this.handler = handler;
        this.disp = disp;
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        handler.openDisplay( disp );
    }

}
