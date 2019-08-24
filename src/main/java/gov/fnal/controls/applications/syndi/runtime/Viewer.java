// (c) 2001-2010 Fermi Research Allaince
// $Id: Viewer.java,v 1.6 2010/09/20 21:55:16 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime;

import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import gov.fnal.controls.applications.syndi.SynopticFrameAction;
import gov.fnal.controls.applications.syndi.SynopticFrame;
import gov.fnal.controls.applications.syndi.repository.DisplaySource;
import gov.fnal.controls.applications.syndi.repository.DisplayURISource;
import gov.fnal.controls.applications.syndi.repository.DisplayXMLSource;
import gov.fnal.controls.applications.syndi.repository.RecentDisplays;
import gov.fnal.controls.applications.syndi.repository.RepositoryFile;
import gov.fnal.controls.applications.syndi.repository.RepositoryFileView;
import gov.fnal.controls.applications.syndi.repository.RepositoryView;
import gov.fnal.controls.applications.syndi.runtime.daq.DaqInterface;
import gov.fnal.controls.applications.syndi.runtime.daq.DaqInterfaceFactory;
import gov.fnal.controls.applications.syndi.util.DisplayFilter;
import gov.fnal.controls.applications.syndi.util.FrameSaver;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.DefaultFocusTraversalPolicy;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import static java.awt.event.KeyEvent.ALT_MASK;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static java.awt.GridBagConstraints.*;

/**
 *
 * @author Andrey Petrov, Timofei Bolshakov
 * @version $Date: 2010/09/20 21:55:16 $
 */
public class Viewer extends SynopticFrame {

    private static final String TITLE = "Synoptic Viewer";

    private static final int NEW_FRAME_SHIFT = 24;

    private static final Logger log = Logger.getLogger( Viewer.class.getName());
    
    private static final AtomicInteger frameCount = new AtomicInteger();

    private static final Map<String,Viewer> pool = new HashMap<String,Viewer>();

    private static final RecentDisplays recentCache = new RecentDisplays( "viewer" );

    private final ViewerStatusBar statusBar = new ViewerStatusBar();

    protected final FrameMenuBar menuBar = new FrameMenuBar();
    protected final FrameToolBar toolBar = new FrameToolBar();

    protected final JMenu mnuFile = new FrameMenu( "File", 'f' );
    protected final JMenu mnuEdit = new FrameMenu( "Edit", 'e' );
    protected final JMenu mnuTasks = new FrameMenu( "Tasks", 't' );
    protected final JMenu mnuHelp = new FrameMenu( "Help", 'h' );
    protected final JMenu mnuRecent = new FrameMenu( "Recent Displays", 'r' );

    private final Action newAction = new NewAction();
    private final Action openAction = new OpenAction();
    private final Action downloadAction = new DownloadAction();
    private final Action closeAction = new CloseAction();
    private final Action printAction = new PrintAction();
    private final Action pageSetupAction = new PageSetupAction();
    private final Action exitAction = new ExitAction();
    private final Action cutAction = new CutAction();
    private final Action copyAction = new CopyAction();
    private final Action pasteAction = new PasteAction();
    private final Action backAction = new BackAction();
    private final Action forwardAction = new ForwardAction();
    private final Action reloadAction = new ReloadAction();
    private final Action helpAction = new HelpAction();
    private final Action aboutAction = new AboutAction();
    private final Action listDataChannels = new ListDataChannelsAction();

    private final JScrollPane scroll = new JScrollPane(
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
    );
    private final boolean standalone;
    private final LinkedList<DisplaySource> cache = new LinkedList<DisplaySource>();
    private final Preferences prefs = Preferences.userNodeForPackage( getClass());
    private final FrameSaver frameSaver = new FrameSaver( this );
    private final String name;
    
    private JFileChooser fileDialog, repoDialog;
    private int cacheIndex = -1;
    private Desktop desktop;
    
    public Viewer( boolean standalone ) {
        this( standalone, null, null );
    }

    protected Viewer( boolean standalone, Viewer creator, String name ) {
        
        this.standalone = standalone;
        this.name = name;
        
        setTitle( TITLE );
        setDefaultCloseOperation( DISPOSE_ON_CLOSE );
        setFocusTraversalPolicy( new Pokus());

        setJMenuBar( menuBar );

        menuBar.add( mnuFile );
            mnuFile.add( newAction );
            mnuFile.add( openAction );
            mnuFile.add( downloadAction );
            mnuFile.add( mnuRecent );
            mnuFile.add( closeAction );
            mnuFile.addSeparator();
            mnuFile.add( printAction );
            mnuFile.add( pageSetupAction );
            mnuFile.addSeparator();
            mnuFile.add( exitAction );
        menuBar.add( mnuEdit );
            mnuEdit.add( cutAction );
            mnuEdit.add( copyAction );
            mnuEdit.add( pasteAction );
        menuBar.add( mnuTasks );
            mnuTasks.add( backAction );
            mnuTasks.add( forwardAction );
            mnuTasks.addSeparator();
            mnuTasks.add( reloadAction );
            mnuTasks.addSeparator();
            mnuTasks.add( listDataChannels );
        menuBar.add( mnuHelp );
            mnuHelp.add( helpAction );
            mnuHelp.add( aboutAction );

        toolBar.add( openAction );
        toolBar.add( downloadAction );
        toolBar.addSeparator();
        toolBar.add( printAction );
        toolBar.addSeparator();
        toolBar.add( cutAction );
        toolBar.add( copyAction );
        toolBar.add( pasteAction );
        toolBar.addSeparator();
        toolBar.add( reloadAction );
        toolBar.add( backAction );
        toolBar.add( forwardAction );
        toolBar.addSeparator();
        toolBar.add( helpAction );

        getContentPane().setLayout( new GridBagLayout());
        getContentPane().add( toolBar,   new GridBagConstraints( 0, 0, 1, 1, 1.0, 0.0,
                CENTER, HORIZONTAL, new Insets( 0, 0, 0, 0 ), 0, 0 ));
        getContentPane().add( scroll,    new GridBagConstraints( 0, 1, 1, 1, 1.0, 1.0,
                CENTER, BOTH,       new Insets( 0, 0, 0, 0 ), 0, 0 ));
        getContentPane().add( statusBar, new GridBagConstraints( 0, 2, 1, 1, 1.0, 0.0,
                CENTER, HORIZONTAL, new Insets( 0, 0, 0, 0 ), 0, 0 ));
        
        frameCount.incrementAndGet();

        if (name != null) {
            synchronized (Viewer.class) {
                if (!pool.containsKey( name )) {
                    pool.put( name, this );
                }
            }
        }

        if (creator == null) {
            frameSaver.restore( prefs );
        } else {

            Dimension screen = getToolkit().getScreenSize();

            int width = creator.getWidth();
            int height = creator.getHeight();

            if (width > screen.width) {
                width = screen.width;
            }
            if (height > screen.height) {
                height = screen.height;
            }

            int x = creator.getX() + NEW_FRAME_SHIFT;
            int y = creator.getY() + NEW_FRAME_SHIFT;

            if (x < 0 || x > screen.width - width) {
                x = 0;
            }
            if (y < 0 || y > screen.height - height) {
                y = 0;
            }

            setSize( width, height );
            setLocation( x, y );

        }

        recentCache.register( this );

        setVisible( true );

    }

    private void showException( Exception ex ) {
        JOptionPane.showMessageDialog(
            this,
            ex.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    @Override
    public void dispose() {
        if (name != null) {
            synchronized (Viewer.class) {
                if (pool.get( name ) == this) {
                    pool.remove( name );
                }
            }
        }
        evictCurrentDisplay();
        recentCache.unregister( this );
        frameSaver.save( prefs );
        super.dispose();
        if (frameCount.decrementAndGet() <= 0 && standalone) {
            recentCache.save();
            DaqInterfaceFactory.shutOffSharedInstance();
            System.exit( 0 );
        }
    }

    public void showRecentDisplayDialog() {
        recentCache.showDialog( this );
    }
    
    public void openDisplay( URI uri ) {
        openDisplay( uri, true );
    }

    public void openDisplay( URI uri, boolean withReset ) {
        try {
            DisplaySource<?> source = new DisplayURISource( uri );
            if (withReset) {
                closeDisplay(); // This also resets history
            }
            openDisplay( source );
        } catch (Exception ex) {
            showException( ex );
        }
    }

    public void openDisplay( File file ) {
        openDisplay( file.toURI());
    }

    public void openDisplay( Document doc, String name ) {
        DisplaySource<?> source = new DisplayXMLSource( doc, name );
        closeDisplay();
        openDisplay( source );
    }

    @Override
    public void openDisplay( DisplaySource<?> disp ) {
        if (SwingUtilities.isEventDispatchThread()) {
            openDisplayInDispatchThread( disp );
            return;
        }
        OpenDisplayTask task = new OpenDisplayTask( disp );
        try {
            SwingUtilities.invokeLater( task );
        } catch (Exception ex) {
            log.throwing( Viewer.class.getName(), "openDisplay", ex );
        }
    }

    @Override
    public JMenu getRecentDisplaysMenu() {
        return mnuRecent;
    }

    private synchronized void openDisplayInDispatchThread( DisplaySource<?> disp ) {
        setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ));
        try {
            
            evictCurrentDisplay();

            Element xml = disp.loadDocument().getDocumentElement();

            RootComponent display = new RootComponent();
            display.init( xml );
            scroll.setViewportView( display );

            DaqInterface daq = display.getDaqInterface();
            if (daq != null) {
                Component widget = daq.getControlWidget();
                if (widget != null) {
                    statusBar.getWidgetWrapper().setWidget( widget );
                }
            }

            pack();

            String dispName = disp.getSimpleName();
            if (dispName == null) {
                dispName = "No Name";
            }
            setTitle( dispName + " - " + TITLE );

            closeAction.setEnabled( true );
            reloadAction.setEnabled( true );

            DisplaySource current = (cacheIndex == -1) ? null : cache.get( cacheIndex );
            if (disp != current) {
                cacheIndex++;
                while (cache.size() > cacheIndex) {
                    cache.removeLast();
                }
                cache.add( disp );
            }

            backAction.setEnabled( cacheIndex > 0 );
            forwardAction.setEnabled( cacheIndex < cache.size() - 1 );

            recentCache.put( disp );

        } catch (FileNotFoundException ex) {

            JOptionPane.showMessageDialog(
                this,
                "File not found: " + disp,
                "Cannot Open Display",
                JOptionPane.ERROR_MESSAGE
            );

        } catch (Exception ex) {

            log.log( Level.SEVERE, "Cannot open " + disp + ": " + ex.getMessage(), ex );
            JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Cannot Open Display",
                JOptionPane.ERROR_MESSAGE
            );

        } finally {
            setCursor( Cursor.getDefaultCursor());
        }
    }

    public void openLink( URI uri, boolean useNewWindow ) {
        String uri_s = uri.toString();
        if (uri_s.toLowerCase().startsWith( "repo:" )) {
            uri_s = uri_s.substring( 5 );
        }
        if (uri_s.startsWith( "/" )) {
            Viewer viewer;
            if (useNewWindow) {
                synchronized (Viewer.class) {
                    viewer = pool.get( uri_s );
                }
                if (viewer == null) {
                    viewer = new Viewer( standalone, this, uri_s );
                }
            } else {
                viewer = this;
            }
            viewer.openDisplay( uri, false );
            viewer.toFront();
        } else {
            openWebPage( uri );
        }
    }

    private void openWebPage( URI uri ) {
        try {
            if (desktop == null) {
                desktop = Desktop.getDesktop();
            }
            desktop.browse( uri );
        } catch (IOException ex) {
            log.log( Level.SEVERE, "Cannot open web page", ex );
            JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Cannot Open Web Page.",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public void closeDisplay() {
        if (SwingUtilities.isEventDispatchThread()) {
            closeDisplayInDispatchThread();
        } else {
            CloseDisplayTask task = new CloseDisplayTask();
            try {
                SwingUtilities.invokeAndWait( task );
            } catch (Exception ex) {
                log.throwing( Viewer.class.getName(), "closeDisplay", ex );
            }
        }
    }

    private synchronized void closeDisplayInDispatchThread() {
        setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ));
        try {

            evictCurrentDisplay();

            setTitle( TITLE );

            closeAction.setEnabled( false );
            reloadAction.setEnabled( false );

            cache.clear();
            cacheIndex = -1;

            backAction.setEnabled( false );
            forwardAction.setEnabled( false );
            
        } catch (Exception ex) {
            log.throwing( getClass().getName(), "closeDisplay", ex );
        } finally {
            setCursor( Cursor.getDefaultCursor());
        }
    }

    private RootComponent getCurrentDisplay() {
        JViewport viewPort = scroll.getViewport();
        if (viewPort == null) {
            return null;
        }
        return (RootComponent)viewPort.getView();
    }

    private void evictCurrentDisplay() {
        JViewport viewPort = scroll.getViewport();
        if (viewPort != null) {
            RootComponent display = (RootComponent)viewPort.getView();
            if (display != null) {
                display.dispose();
                viewPort.setView( null );
            }
        }
        statusBar.getWidgetWrapper().clearWidget();
    }

    private JFileChooser initFileDialog( String title ) {
        if (fileDialog == null) {
            fileDialog = new JFileChooser();
            fileDialog.setMultiSelectionEnabled( false );
            fileDialog.setFileFilter( new DisplayFilter());
        }
        fileDialog.setDialogTitle( title );
        return fileDialog;
    }

    private JFileChooser initRepoDialog() {
        if (repoDialog == null) {
            repoDialog = new JFileChooser( new RepositoryView());
            repoDialog.setFileView( new RepositoryFileView());
            repoDialog.setMultiSelectionEnabled( false );
            repoDialog.setDialogTitle( "Download & Open Display" );
        }
        RepositoryFile.invalidate();
        return repoDialog;
    }

    private class OpenDisplayTask implements Runnable {

        private final DisplaySource<?> disp;

        OpenDisplayTask( DisplaySource<?> disp ) {
            this.disp = disp;
        }

        @Override
        public void run() {
            openDisplayInDispatchThread( disp );
        }

    }

    private class CloseDisplayTask implements Runnable {

        @Override
        public void run() {
            closeDisplayInDispatchThread();
        }

    }

    private class NewAction extends SynopticFrameAction {
        
        NewAction() {
            super( "New Window", 'N',
                    "/toolbarButtonGraphics/general/New16.gif",
                    'N', CTRL_MASK );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            new Viewer( standalone, Viewer.this, null );
        }

    }

    private class OpenAction extends SynopticFrameAction {
        
        OpenAction() {
            super( "Open...", 'O',
                    "/toolbarButtonGraphics/general/Open16.gif",
                    'O', CTRL_MASK );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            initFileDialog( "Load Display" );
            if (fileDialog.showOpenDialog( Viewer.this ) != APPROVE_OPTION) {
                return;
            }
            File file = fileDialog.getSelectedFile();
            try {
                openDisplay( file );
            } catch (IllegalArgumentException ex) {
                showException( ex );
            }
        }
        
    }

    private class DownloadAction extends SynopticFrameAction {

        DownloadAction() {
            super( "Download & Open...", 'D',
                    "/toolbarButtonGraphics/general/Import16.gif",
                    'D', CTRL_MASK );
            setEnabled( System.getProperty( "Synoptic.repository-root" ) != null );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            initRepoDialog();
            if (repoDialog.showOpenDialog( Viewer.this ) != APPROVE_OPTION) {
                return;
            }
            File file = repoDialog.getSelectedFile();
            try {
                openDisplay( file );
            } catch (IllegalArgumentException ex) {
                showException( ex );
            }
        }

    }

    private class CloseAction extends SynopticFrameAction {

        CloseAction() {
            super( "Close", 'C' );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            closeDisplay();
        }

    }

    private class PrintAction extends SynopticFrameAction {

        PrintAction() {
            super( "Print", 'I',
                    "/toolbarButtonGraphics/general/Print16.gif",
                    'P', CTRL_MASK );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            RootComponent display = getCurrentDisplay();
            if (display == null) {
                return;
            }
            PageFormat format = getPageFormat();
            display.setPageFormat( format );
            PrinterJob job = getPrinterJob();
            try {
                job.setPageable( display );
                job.setPrintable( display, format );
                if (job.printDialog()) {
                    job.print();
                }
            } catch (Exception ex) {
                job.cancel();
                showException( ex );
            }
        }

    }

    private class ExitAction extends SynopticFrameAction {

        ExitAction() {
            super( "Exit", 'x', null, 'X', ALT_MASK );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            dispose();
        }

    }


    private class CutAction extends SynopticFrameAction {

        CutAction() {
            super( "Cut", 'T',
                    "/toolbarButtonGraphics/general/Cut16.gif",
                    'X', CTRL_MASK );
            setEnabled( false );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            // TODO
        }

    }

    private class CopyAction extends SynopticFrameAction {

        CopyAction() {
            super( "Copy", 'C',
                    "/toolbarButtonGraphics/general/Copy16.gif",
                    'C', CTRL_MASK );
            setEnabled( false );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            // TODO
        }

    }

    private class PasteAction extends SynopticFrameAction {

        PasteAction() {
            super( "Paste", 'P',
                    "/toolbarButtonGraphics/general/Paste16.gif",
                    'V', CTRL_MASK );
            setEnabled( false );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            // TODO
        }

    }

    private class BackAction extends SynopticFrameAction {

        BackAction() {
            super( "Back", 'B',
                    "/toolbarButtonGraphics/navigation/Back16.gif" );
            setEnabled( false );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            synchronized (Viewer.this) {
                if (cacheIndex > 0) {
                    openDisplay( cache.get( --cacheIndex ));
                }
            }
        }

    }

    private class ForwardAction extends SynopticFrameAction {

        ForwardAction() {
            super( "Forward", 'F',
                    "/toolbarButtonGraphics/navigation/Forward16.gif" );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            synchronized (Viewer.this) {
                if (cacheIndex < cache.size() - 1) {
                    openDisplay( cache.get( ++cacheIndex ));
                }
            }
        }

    }

    private class ReloadAction extends SynopticFrameAction {

        ReloadAction() {
            super( "Repaint", 'R',
                    "/toolbarButtonGraphics/general/Refresh16.gif",
                    KeyEvent.VK_F5, 0 );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            synchronized (Viewer.this) {
                if (cacheIndex != -1) {
                    openDisplay( cache.get( cacheIndex ));
                }
            }
        }

    }

    private class ListDataChannelsAction extends SynopticFrameAction {

        ListDataChannelsAction() {
            super( "List Data Channels", 'L' );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            RootComponent root = getCurrentDisplay();
            if (root == null) {
                return;
            }
            Action action = root.getActionMap().get( "listDataChannels" );
            if (action == null) {
                return;
            }
            action.actionPerformed( e );
        }

    }

    private static class Pokus extends DefaultFocusTraversalPolicy {

        @Override
        public Component getDefaultComponent( Container aContainer ) {
            return null;
        }

    }

}
