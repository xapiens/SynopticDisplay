// (c) 2001-2010 Fermi Research Allaince
// $Id: PinInfoPanel.java,v 1.2 2010/09/15 16:11:48 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element.pin;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import static java.awt.GridBagConstraints.*;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:11:48 $
 */
public class PinInfoPanel extends JPanel {

    private static final Font LABEL_FONT = new Font( Font.MONOSPACED, Font.PLAIN, 12 );
    private static final Border BORDER = new EtchedBorder( EtchedBorder.LOWERED );

    private final Label laComp = new Label( "Component: " );
    private final Label laPin  = new Label( "Pin: " );

    private final Label fdComp = new Label();
    private final Label fdPin  = new Label();

    public PinInfoPanel( PinRole role, Pin pin ) {

        super( new GridBagLayout());

        setBorder( new TitledBorder( BORDER, role.toString()));

        fdComp.setText( String.valueOf( pin.getComponent()));

        String pinName = pin.getType() + " #" + pin.getIndex();
        if (pin.getName() != null) {
            pinName += ", \"" + pin.getName() + "\"";
        }
        fdPin.setText( pinName );

        add( laComp, new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0,
                EAST, NONE,       new Insets( 6, 6, 3, 3 ), 0, 0 ));
        add( fdComp, new GridBagConstraints( 1, 0, 1, 1, 1.0, 0.0,
                WEST, HORIZONTAL, new Insets( 6, 3, 3, 6 ), 0, 0 ));
        add( laPin,  new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0,
                EAST, NONE,       new Insets( 3, 6, 6, 3 ), 0, 0 ));
        add( fdPin,  new GridBagConstraints( 1, 1, 1, 1, 1.0, 0.0,
                WEST, HORIZONTAL, new Insets( 3, 3, 6, 6 ), 0, 0 ));

    }

    private static class Label extends JLabel {

        Label() {
            super();
            setFont( LABEL_FONT );
        }

        Label( String text ) {
            this();
            setText( text );
        }

    }

}
