//  (c) 2001-2010 Fermi Research Alliance
//  $Id: SynopticFrame.java,v 1.3 2010/09/15 15:15:16 apetrov Exp $
package gov.fnal.controls.applications.syndi;

import gov.fnal.controls.applications.syndi.repository.DisplayHandler;
import gov.fnal.controls.applications.syndi.util.AboutDialog;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:15:16 $
 */
public abstract class SynopticFrame extends JFrame implements DisplayHandler {

    private static final String ICON_URL = "icon.gif";

    private static final Logger log = Logger.getLogger( SynopticFrame.class.getName());
    
    private static PageFormat pageFormat = new PageFormat();

    private PrinterJob printerJob;

    protected SynopticFrame() {
        URL url = SynopticFrame.class.getResource( ICON_URL );
        if (url != null) {
            setIconImage( new ImageIcon( url ).getImage());
        }
    }
    
    protected synchronized PrinterJob getPrinterJob() {
        if (printerJob == null) {
            printerJob = PrinterJob.getPrinterJob();
            printerJob.setJobName( "Synoptic" );
        }
        return printerJob;
    }

    protected PageFormat getPageFormat() {
        return (PageFormat)pageFormat.clone();
    }
    
    protected static class FrameMenu extends JMenu {

        public FrameMenu( String name, char mnemonic ) {
            super( name );
            setMnemonic( mnemonic );
        }

    }

    protected static class FrameMenuBar extends JMenuBar {

        public FrameMenuBar() {}

    }

    protected static class FrameToolBar extends JToolBar {

        public FrameToolBar() {
            setFloatable( false );
        }

        public JButton insert( Action a, int index ) {
            JButton b = createActionComponent( a );
            b.setAction( a );
            add( b, index );
            return b;
        }

        public void insertSeparator( int index ) {
            insertSeparator( null, index );
        }

        public void insertSeparator( Dimension size, int index ) {
            JToolBar.Separator s = new JToolBar.Separator( size );
            add( s, index );
        }

    }

    protected class PageSetupAction extends SynopticFrameAction {

        public PageSetupAction() {
            super( "Page Setup...", 'G' );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            PrinterJob job = getPrinterJob();
            pageFormat = job.validatePage( pageFormat );
            pageFormat = job.pageDialog( pageFormat );
        }

    }

    protected class HelpAction extends SynopticFrameAction {

        public HelpAction() {
            super( "Help Topics", 'E',
                    "/toolbarButtonGraphics/general/Help16.gif",
                    KeyEvent.VK_F1, 0 );
            setEnabled( System.getProperty( "Synoptic.help-url" ) != null );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            try {
                String urlStr = System.getProperty( "Synoptic.help-url" );
                if (urlStr == null) {
                    return;
                }
                Desktop.getDesktop().browse( new URI( urlStr ));
            } catch (Exception ex) {
                log.log( Level.SEVERE, "Cannot open browser", ex );
            }
        }

    }

    protected class AboutAction extends SynopticFrameAction {

        public AboutAction() {
            super( "About...", 'A', null );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            new AboutDialog( SynopticFrame.this );
        }

    }

}
