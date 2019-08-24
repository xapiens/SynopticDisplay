//  (c) 2001-2010 Fermi Research Alliance
//  $Id: SynopticFrameAction.java,v 1.2 2010/09/15 15:15:16 apetrov Exp $
package gov.fnal.controls.applications.syndi;

import java.awt.Toolkit;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:15:16 $
 */
public abstract class SynopticFrameAction extends AbstractAction {

    public static final int CTRL_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    protected SynopticFrameAction( String name, int mnemonic ) {
        super( name );
        setEnabled( true );
        putValue( MNEMONIC_KEY, mnemonic );
        putValue( SHORT_DESCRIPTION, name );
    }

    protected SynopticFrameAction( String name, int mnemonic, String iconPath ) {
        this( name, mnemonic );
        if (iconPath != null) {
            putValue( SMALL_ICON, loadIcon( iconPath, "16" ));
            putValue( LARGE_ICON_KEY, loadIcon( iconPath, "24" ));
        }
    }

    protected SynopticFrameAction( String name, char mnemonic, String iconPath, int keyCode, int modifier ) {
        this( name, mnemonic, iconPath );
        putValue( ACCELERATOR_KEY, KeyStroke.getKeyStroke( keyCode, modifier ));
    }

    private ImageIcon loadIcon( String iconPath, String suffix ) {
        if (iconPath == null) {
            return null;
        }
        String regex = null;
        if ("16".equals( suffix )) {
            regex = "24\\.";
        } else if ("24".equals( suffix )) {
            regex = "16\\.";
        }
        if (regex != null) {
            iconPath = iconPath.replaceAll( regex, suffix + "." );
        }
        URL iconUrl = getClass().getResource( iconPath );
        return iconUrl != null ? new ImageIcon( iconUrl ) : null;
    }

}
