// (c) 2001-2010 Fermi Research Alliance
// $Id: DisplayHandler.java,v 1.2 2010/09/15 15:53:52 apetrov Exp $
package gov.fnal.controls.applications.syndi.repository;

import javax.swing.JMenu;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:53:52 $
 */
public interface DisplayHandler {

    void openDisplay( DisplaySource<?> disp );

    JMenu getRecentDisplaysMenu();

}
