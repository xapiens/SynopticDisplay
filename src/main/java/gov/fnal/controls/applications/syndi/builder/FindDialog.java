// (c) 2001-2010 Fermi Research Allaince
// $Id: FindDialog.java,v 1.2 2010/09/15 15:59:55 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:59:55 $
 */
public class FindDialog extends JPanel implements ActionListener {

    private final JTextField componentIdE = new JTextField();

    public FindDialog( Integer componentId ) {
        super( new GridBagLayout());
        componentIdE.addActionListener( this );
        if (componentId != null) {
            componentIdE.setText( componentId.toString());
        } else {
            componentIdE.setText( "" );
        }
        JLabel label = new JLabel( "Component #" );
        label.setDisplayedMnemonic( 'C' );
        label.setLabelFor( componentIdE );
        this.add( label,        new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.NONE,
            new Insets( 0, 0, 0, 6 ), 0, 0 ));
        this.add( componentIdE, new GridBagConstraints( 1, 0, 1, 1, 1.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
            new Insets( 0, 6, 0, 0 ), 0, 0 ));
    }

    public Integer getComponentId() {
        try {
            String str = componentIdE.getText();
            return new Integer( str );
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        SwingUtilities.getWindowAncestor( this ).setVisible( false );
    }

}
