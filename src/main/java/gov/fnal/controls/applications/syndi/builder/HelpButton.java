// (c) 2001-2010 Fermi Research Allaince
// $Id: HelpButton.java,v 1.4 2010/09/15 16:37:59 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:37:59 $
 */
public class HelpButton extends JButton implements ActionListener {

    private static final String HELP_ROOT = System.getProperty( "Synoptic.component-help-root" );
    
    private static final Logger log = Logger.getLogger( HelpButton.class.getName());
    
    private final URI uri;
    
    public HelpButton( String helpUrl ) {
        super( "Help" );
        addActionListener( this );
        if (helpUrl == null) {
            uri = null;
        } else if (helpUrl.startsWith( "http" )) {
            uri = createUri( helpUrl );
        } else if (HELP_ROOT != null) {
            uri = createUri( HELP_ROOT + helpUrl );
        } else {
            log.info( "Component help root is not specified" );
            uri = null;
        }
        setEnabled( uri != null );
    }

    private static URI createUri( String address ) {
        try {
            return new URI( address );
        } catch (URISyntaxException ex) {
            log.warning( "Illegal address: " + address );
            return null;
        }
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        if (uri == null) {
            return;
        }
        try {
            Desktop.getDesktop().browse( uri );
        } catch (IOException ex) {
            log.log( Level.INFO, "Cannot open " + uri, ex );
        }
    }

}
