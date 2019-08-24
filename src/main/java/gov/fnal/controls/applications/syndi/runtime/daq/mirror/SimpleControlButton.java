//  (c) 2001-2010 Fermi Research Alliance
//  $Id: SimpleControlButton.java,v 1.2 2010/09/15 15:30:45 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.daq.mirror;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author  Andrey Petrov
 * @version $Date: 2010/09/15 15:30:45 $
 */
public class SimpleControlButton extends JLabel {

    private static final String CONNECTED_ICON =
            "/gov/fnal/controls/applications/syndi/runtime/daq/mirror/connect.gif";
    private static final String SETTING_ICON =
            "/gov/fnal/controls/applications/syndi/runtime/daq/mirror/settings.gif";

    private static final Logger log = Logger.getLogger( SimpleControlButton.class.getName());

    private final ImageIcon connectedIcon = createIcon( CONNECTED_ICON );
    private final ImageIcon settingIcon = createIcon( SETTING_ICON );
    private final Popup popup = new Popup();

    private final MirrorInterface daq;

    SimpleControlButton( MirrorInterface daq ) {
        assert (daq != null);
        this.daq = daq;
        setOpaque( false );
        setHorizontalAlignment( SwingConstants.CENTER );
        setVerticalAlignment( SwingConstants.CENTER );
        addMouseListener( new MouseListenerImpl());
        updateView();
    }

    private boolean isSettingEnabled() {
        return daq.isSettingEnabled();
    }

    private void setSettingEnabled( boolean settingEnabled ) {
        daq.setSettingEnabled( settingEnabled );
        updateView();
    }

    private void updateView() {
        if (!isSettingEnabled()) {
            setIcon( connectedIcon );
            setToolTipText( "Connected" );
        } else {
            setIcon( settingIcon );
            setToolTipText( "Setting is Enabled" );
        }
    }

    public void showPopup() {
        popup.settingEnabled.setSelected( isSettingEnabled());
        popup.show( this, getWidth() / 2, getHeight() / 2 );
    }

    private ImageIcon createIcon( String iconPath ) {
        URL url = getClass().getResource( iconPath );
        if (url == null) {
            return null;
        }
        try {
            return new ImageIcon( url );
        } catch (Exception ex) {
            log.throwing( SimpleControlButton.class.getName(), "createIcon", ex );
            return null;
        }
    }

    private class MouseListenerImpl extends MouseAdapter {

        @Override
        public void mouseClicked( MouseEvent e ) {
            showPopup();
        }

    }

    private class Popup extends JPopupMenu {

        final JCheckBox settingEnabled = new JCheckBox( "Setting Enabled" );

        Popup() {
            settingEnabled.addChangeListener( new ChangeListenerImpl());
            setLayout( new GridBagLayout());
            add( settingEnabled,
                    new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.WEST, GridBagConstraints.NONE,
                    new Insets( 12, 12, 12, 12 ), 0, 0 ));
        }

    }

    private class ChangeListenerImpl implements ChangeListener {

        @Override
        public void stateChanged( ChangeEvent e ) {
            JCheckBox checkBox = (JCheckBox)e.getSource();
            setSettingEnabled( checkBox.isSelected());
        }

    }

}
