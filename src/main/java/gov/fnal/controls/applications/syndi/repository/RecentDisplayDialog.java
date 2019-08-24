// (c) 2001-2010 Fermi Research Alliance
// $Id: RecentDisplayDialog.java,v 1.2 2010/09/15 15:53:52 apetrov Exp $
package gov.fnal.controls.applications.syndi.repository;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import static java.awt.GridBagConstraints.*;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:53:52 $
 */
class RecentDisplayDialog extends JDialog implements MouseListener, ActionListener {

    private final JButton buOpen = new JButton( "Open" );
    private final JButton buCancel = new JButton( "Cancel" );
    private final DisplayHandler handler;
    private final RecentDisplayList list;

    RecentDisplayDialog( List<DisplaySource> displays, DisplayHandler handler ) {

        super( (JFrame)handler, true );

        setTitle( "Recent Displays" );

        this.handler = handler;

        list = new RecentDisplayList( displays );
        list.addMouseListener( this );

        getRootPane().setDefaultButton( buOpen );

        buOpen.addActionListener( this );
        buCancel.addActionListener( this );

        JLabel info = new JLabel(
            "You may reopen one of the recently used displays:"
        );
        
        JScrollPane scroll = new JScrollPane(
            list,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );

        getContentPane().setLayout( new GridBagLayout());
        getContentPane().add( info,     new GridBagConstraints( 0, 0, 2, 1, 0.0, 0.0,
            WEST,   NONE, new Insets( 12, 12,  6, 11 ), 0, 0 ));
        getContentPane().add( scroll,   new GridBagConstraints( 0, 1, 2, 1, 1.0, 1.0,
            CENTER, BOTH, new Insets(  6, 12,  6, 11 ), 0, 0 ));
        getContentPane().add( buOpen,   new GridBagConstraints( 0, 2, 1, 1, 0.5, 0.0,
            EAST,   NONE, new Insets( 11, 12, 11,  3 ), 0, 0 ));
        getContentPane().add( buCancel, new GridBagConstraints( 1, 2, 1, 1, 0.5, 0.0,
            WEST,   NONE, new Insets( 11,  3, 11, 11 ), 0, 0 ));

        pack();
        setLocationRelativeTo( (JFrame)handler );

    }

    private void openSelected() {
        int index = list.getSelectedIndex();
        if (index < 0) {
            return;
        }
        this.dispose();
        DisplaySource disp = (DisplaySource)list.getModel().getElementAt( index );
        handler.openDisplay( disp );
    }

    @Override
    public void mouseClicked( MouseEvent e ) {
        if (e.getClickCount() < 2) {
            return;
        }
        openSelected();
    }

    @Override
    public void mousePressed( MouseEvent e ) {}

    @Override
    public void mouseReleased( MouseEvent e ) {}

    @Override
    public void mouseEntered( MouseEvent e ) {}

    @Override
    public void mouseExited( MouseEvent e ) {}

    @Override
    public void actionPerformed( ActionEvent e ) {
        Object src = e.getSource();
        if (src == buOpen) {
            openSelected();
        }
        dispose();
    }

}
