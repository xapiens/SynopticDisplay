// (c) 2001-2010 Fermi Research Allaince
// $Id: Builder.java,v 1.6 2010/09/20 21:56:09 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder;

import javax.swing.JMenu;
import gov.fnal.controls.applications.syndi.SynopticFrameAction;
import gov.fnal.controls.applications.syndi.SynopticFrame;
import gov.fnal.controls.applications.syndi.builder.element.GenericContainer;
import gov.fnal.controls.applications.syndi.builder.element.BuilderComponent;
import gov.fnal.controls.applications.syndi.builder.element.ComponentLink;
import gov.fnal.controls.applications.syndi.builder.element.GridAttributes;
import gov.fnal.controls.applications.syndi.builder.element.NewDisplayFactory;
import gov.fnal.controls.applications.syndi.builder.element.BuilderComponentSaver;
import gov.fnal.controls.applications.syndi.builder.palette.Palette;
import gov.fnal.controls.applications.syndi.util.DisplayFilter;
import gov.fnal.controls.applications.syndi.repository.RepositoryFile;
import gov.fnal.controls.applications.syndi.builder.palette.PaletteNode;
import gov.fnal.controls.applications.syndi.repository.DisplaySource;
import gov.fnal.controls.applications.syndi.repository.DisplayURISource;
import gov.fnal.controls.applications.syndi.repository.NameChecker;
import gov.fnal.controls.applications.syndi.repository.RepositoryFileView;
import gov.fnal.controls.applications.syndi.repository.RepositoryView;
import gov.fnal.controls.applications.syndi.runtime.Viewer;
import gov.fnal.controls.applications.syndi.runtime.daq.DaqInterfaceFactory;
import gov.fnal.controls.applications.syndi.util.FrameSaver;
import gov.fnal.controls.tools.svg.SVGColor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import static java.awt.event.KeyEvent.ALT_MASK;
import static javax.swing.JFileChooser.APPROVE_OPTION;

/**
 * The main frame of Synoptic Display Builder.
 * 
 * @author Andrey Petrov
 * @version $Date: 2010/09/20 21:56:09 $
 */ 
public class Builder extends SynopticFrame {

    private static final String TITLE = "Synoptic Builder";

    private static final Logger log = Logger.getLogger( Builder.class.getName());

    private final BuilderStatusBar statusBar = new BuilderStatusBar();

    protected final FrameMenuBar menuBar = new FrameMenuBar();
    protected final FrameToolBar toolBar = new FrameToolBar();

    protected final JMenu mnuFile = new FrameMenu( "File", 'f' );
    protected final JMenu mnuEdit = new FrameMenu( "Edit", 'e' );
    protected final JMenu mnuView = new FrameMenu( "View", 'v' );
    protected final JMenu mnuElement = new FrameMenu( "Element", 'l' );
    protected final JMenu mnuTools = new FrameMenu( "Tools", 't' );
    protected final JMenu mnuHelp = new FrameMenu( "Help", 'h' );
    protected final JMenu mnuBgImage = new FrameMenu( "Background Image", 'i' );

    private final Action newAction = new NewAction();
    private final Action openAction = new OpenAction();
    private final Action downloadAction = new DownloadAction();
    private final Action saveAction = new SaveAction();
    private final Action saveAsAction = new SaveAsAction();
    private final Action preferencesAction = new PreferencesAction();
    private final Action pageSetupAction = new PageSetupAction();
    private final Action printAction = new PrintAction();
    private final Action exitAction = new ExitAction();
    private final Action discardAction = new DiscardAction();
    private final Action nestedAction = new NestedAction();
    private final Action linkAction = new LinkAction();
    private final Action launchAction = new LaunchAction();
    private final Action helpAction = new HelpAction();
    private final Action aboutAction = new AboutAction();

    protected final JTabbedPane tabbedPane = new JTabbedPane();
    protected final Preferences prefs = Preferences.userNodeForPackage( Builder.class );
    protected final DrawingCanvas canvas = new DrawingCanvas();
    protected final Palette palette = new Palette();
    protected final JSplitPane splitPane;
    
    private final NewDisplayFactory dispFac = new NewDisplayFactory();
    private final FrameSaver frameSaver = new FrameSaver( this );

    private File currentFile;
    private JFileChooser fileDialog;
    private JFileChooser repoDialog;

    public Builder() {
        
        setDefaultCloseOperation( DISPOSE_ON_CLOSE );
        
        setJMenuBar( menuBar );

        ActionMap am1 = canvas.getActionMap();

        menuBar.add( mnuFile );
            mnuFile.add( newAction );
            mnuFile.add( openAction );
            mnuFile.add( downloadAction );
            mnuFile.add( saveAction );
            mnuFile.add( saveAsAction );
            mnuFile.addSeparator();
            mnuFile.add( preferencesAction );
            mnuFile.addSeparator();
            mnuFile.add( pageSetupAction );
            mnuFile.add( printAction );
            mnuFile.addSeparator();
            mnuFile.add( exitAction );
        menuBar.add( mnuEdit );
            mnuEdit.add( am1.get( "undo" ));
            mnuEdit.add( am1.get( "redo" ));
            mnuEdit.addSeparator();
            mnuEdit.add( am1.get( "cut" ));
            mnuEdit.add( am1.get( "copy" ));
            mnuEdit.add( am1.get( "paste" ));
            mnuEdit.add( am1.get( "delete" ));
            mnuEdit.add( am1.get( "selectAll" ));
            mnuEdit.addSeparator();
            mnuEdit.add( am1.get( "find" ));
        menuBar.add( mnuView );
            mnuView.add( am1.get( "actualSize" ));
            mnuView.add( am1.get( "zoomIn" ));
            mnuView.add( am1.get( "zoomOut" ));
            mnuView.addSeparator();
            mnuView.add( new JCheckBoxMenuItem( am1.get( "showGrid" )));
            mnuView.add( new JCheckBoxMenuItem( am1.get( "snapToGrid" )));
            mnuView.add( new JCheckBoxMenuItem( am1.get( "hideInvisible" )));
            mnuView.addSeparator();
            mnuView.add( am1.get( "repaint" ));
        menuBar.add( mnuElement );
            mnuElement.add( am1.get( "properties" ));
            mnuElement.add( am1.get( "ioConfig" ));
            mnuElement.add( mnuBgImage );
                mnuBgImage.add( am1.get( "loadBackground" ));
                mnuBgImage.add( am1.get( "saveBackground" ));
                mnuBgImage.add( am1.get( "clearBackground" ));
            mnuElement.addSeparator();
            mnuElement.add( am1.get( "rotate" ));
            mnuElement.add( am1.get( "toFront" ));
            mnuElement.add( am1.get( "toBack" ));
            mnuElement.addSeparator();
            mnuElement.add( am1.get( "info" ));
        menuBar.add( mnuTools );
            mnuTools.add( linkAction );
            mnuTools.add( nestedAction );
            mnuTools.add( discardAction );
            mnuTools.addSeparator();
            mnuTools.add( launchAction );
            mnuTools.add( am1.get( "globalProps" ));
        menuBar.add( mnuHelp );
            mnuHelp.add( helpAction );
            mnuHelp.add( aboutAction );

        mnuElement.addMenuListener( new ElementListener());
            
        JToggleButton hideInvisibleButton = new JToggleButton( am1.get( "hideInvisible" ));
        hideInvisibleButton.setText( null );
            
        toolBar.add( newAction );
        toolBar.add( openAction );
        toolBar.add( downloadAction );
        toolBar.add( saveAction );
        toolBar.add( printAction );
        toolBar.addSeparator();
        toolBar.add( am1.get( "cut" ));
        toolBar.add( am1.get( "copy" ));
        toolBar.add( am1.get( "paste" ));
        toolBar.addSeparator();
        toolBar.add( am1.get( "repaint" ));
        toolBar.addSeparator();
        toolBar.add( am1.get( "properties" ));
        toolBar.add( am1.get( "ioConfig" ));
        toolBar.add( am1.get( "rotate" ));
        toolBar.add( am1.get( "info" ));
        toolBar.addSeparator();
        toolBar.add( linkAction );
        toolBar.add( discardAction );
        toolBar.addSeparator();
        toolBar.add( hideInvisibleButton );
        toolBar.addSeparator();
        toolBar.add( launchAction );
        toolBar.addSeparator();
        toolBar.add( am1.get( "actualSize" ));
        toolBar.add( am1.get( "zoomIn" ));
        toolBar.add( am1.get( "zoomOut" ));
        toolBar.addSeparator();
        toolBar.add( helpAction );

        palette.addTreeSelectionListener( new TreeSelectionListener() {
            @Override 
            public void valueChanged( TreeSelectionEvent e ) {
                PaletteNode node = (PaletteNode)palette.getLastSelectedPathComponent();
                if (node != null && node.isLeaf()) {
                    placeComponent( node );
                }
            }
        });

        canvas.addPropertyChangeListener( new PropertyChangeListener() {

            @Override
            public void propertyChange( PropertyChangeEvent e ) {
                String name = e.getPropertyName();
                if ("pointer".equals( name )) {
                    statusBar.setPoint( (Point)e.getNewValue() );
                } else if ("zoom".equals( name )) {
                    statusBar.setZoom( (Float)e.getNewValue() );
                } else if ("change".equals( name )) {
                    setChanged( (Boolean)e.getNewValue() );
                } else if ("placing".equals( name )) {
                    boolean placing = (Boolean)e.getNewValue();
                    discardAction.setEnabled( placing );
                    linkAction.setEnabled( !placing );
                    //nestedAction.setEnabled( !placing );
                    if (placing) {
                        statusBar.setMessage( "Placing new item..." );
                    } else {
                        palette.setSelectionPath( null );
                    }
                } else if ("selection".equals( name )) {
                    if (canvas.isPlacing()) {
                        return;
                    }
                    BuilderComponent[] selection = (BuilderComponent[])e.getNewValue();
                    if (selection != null && selection.length > 0) {
                        if (selection.length == 1) {
                            statusBar.setMessage( "Selection: " + selection[ 0 ].getName());
                        } else {
                            statusBar.setMessage( selection.length + " items selected" );
                        }
                    } else {
                        statusBar.setMessage( " " );
                    }
                }
            }
        });

        GridAttributes ga = new GridAttributes();
        ga.restore( prefs );
        canvas.setGridAttributes( ga );
        
        JScrollPane toolsScroll = new JScrollPane( palette );
        
        tabbedPane.add( "Components", toolsScroll );
        
        frameSaver.restore( prefs );

        JScrollPane canvasScroll = new JScrollPane( 
            canvas,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        
        JViewport vp = canvasScroll.getViewport();
        vp.setOpaque( true );
        vp.setBackground( UIManager.getColor( "Panel.background" ));

        splitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, tabbedPane, canvasScroll );
        splitPane.setOneTouchExpandable( true );

        int p0 = prefs.getInt( "divider-location", splitPane.getDividerLocation());
        splitPane.setDividerLocation( p0 );

        int p1 = prefs.getInt( "last-divider-location", splitPane.getLastDividerLocation());
        splitPane.setLastDividerLocation( p1 );

        int dw = prefs.getInt( "default-display-width", 0 );
        if (dw > 0) {
            dispFac.setDefaultWidth( dw );
        }
        int dh = prefs.getInt( "default-display-height", 0 );
        if (dh > 0) {
            dispFac.setDefaultHeight( dh );
        }
        String bg = prefs.get( "default-display-background", null );
        if (bg != null) {
            try {
                dispFac.setDefaultBackground( "".equals( bg ) ? null : SVGColor.parseColor( bg ));
            } catch (Exception ex) { /*ignore*/ }
        }
        
        getContentPane().setLayout( new BorderLayout());
        getContentPane().add( splitPane, BorderLayout.CENTER );
        getContentPane().add( toolBar, BorderLayout.NORTH );
        getContentPane().add( statusBar, BorderLayout.SOUTH );
        
        newAction.actionPerformed( null );
        
        statusBar.setZoom( canvas.getZoom());
        
        setCurrentFile( null );

        setVisible( true );
        
    }

    private void setCurrentFile( File file ) {
        this.currentFile = file;
        setTitle( file, canvas.isChanged());
    }
    
    private void setChanged( boolean changed ) {
        saveAction.setEnabled( changed );
        setTitle( currentFile, changed );
    }
    
    private void setTitle( File file, boolean changed ) {
        StringBuilder buf = new StringBuilder();
        buf.append( (file == null) ? "Untitled" : file.getName() );
        if (changed) {
            buf.append( '*' );
        }
        buf.append( " - " );
        buf.append( TITLE );
        setTitle( buf.toString());
    }
    
    private void showException( Exception ex ) {
        JOptionPane.showMessageDialog(  
            this,
            ex.getMessage(), 
            "Error", 
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    private void placeComponent( PaletteNode node ) {
        try {
            canvas.place( node.createBuilderComponent());
        } catch (Exception ex) {
            log.log( Level.SEVERE, "Cannot place component", ex );
            String message = ex.getMessage();
            if (message == null || message.isEmpty()) {
                message = ex.getClass().getSimpleName();
            }
            JOptionPane.showMessageDialog(
                this,
                message,
                "Cannot Place Component",
                JOptionPane.ERROR_MESSAGE
            );
        }
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

    public void openDisplay( URI uri ) {
        DisplaySource<?> source = new DisplayURISource( uri );
        openDisplay( source );
    }

    public void openDisplay( File file ) {
        openDisplay( file.toURI());
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
    public javax.swing.JMenu getRecentDisplaysMenu() {
        //return mnuRecent;
        return null;
    }

    private synchronized void openDisplayInDispatchThread( DisplaySource<?> disp ) {
        setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ));
        try {
            
            canvas.setDisplay( disp.createBuilderComponent());

            setCurrentFile( disp.getLocalFile());

            if (disp instanceof DisplayURISource) {
                String s = ((DisplayURISource)disp).getSource().getScheme();
                if (s == null || "repo".equals( s )) {
                    JOptionPane.showMessageDialog(
                        Builder.this,
                        "Please note that you CAN NOT save displays on the server directly.\n" +
                            "To edit a file in the central repository, check it out using CVS tab.\n" +
                            "For more info, go to http://www-bd.fnal.gov/issues/wiki/SynopticCVS.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE
                    );
                }
            }

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

    private boolean saveDisplay( boolean askWhere ) {
        File file = (currentFile instanceof RepositoryFile) ? null : currentFile;
        if (file == null) {
            askWhere = true;
        }
        while (askWhere) {

            initFileDialog( "Save Display" );

            if (file != null) {
                fileDialog.setSelectedFile( file );
            }
            if (fileDialog.showSaveDialog( this ) != APPROVE_OPTION) {
                return false;
            }
            file = fileDialog.getSelectedFile();

            String name, extension;
            String fullName = file.getName();
            int i = fullName.indexOf( '.' );
            if (i == -1) {
                name = fullName;
                extension = null;
            } else {
                name = fullName.substring( 0, i );
                extension = fullName.substring( i + 1 );
            }

            if (!NameChecker.isValidName( name )) {
                JOptionPane.showMessageDialog(
                    this,
                    "The file name you have entered contains invalid symbols.\n" +
                        "Please use only alphanumeric characters, dashes, and underscores.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
                continue;
            }

            if (!"xml".equals( extension )) {
                String suggestion = name + ".xml";
                int res = JOptionPane.showConfirmDialog(
                    this,
                    "The display file must have an '.xml' extension.\n" +
                        "The name you have entered was '" + fullName + "'.\n" +
                        "Do you wish to use '" + suggestion + "' instead?",
                    "Warning",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
                if (res == JOptionPane.YES_OPTION) {
                    file = new File( file.getParentFile(), suggestion );
                    fullName = file.getName();
                } else if (res == JOptionPane.NO_OPTION) {
                    continue;
                } else {
                    return false;
                }
            }

            if (file.exists()) {
                int res = JOptionPane.showConfirmDialog(
                    this,
                    "A file '" + fullName  + "' already exists.\n" +
                        "Are you sure you wish to replace it?",
                    "Warning",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
                if (res == JOptionPane.NO_OPTION) {
                    continue;
                } else if (res != JOptionPane.YES_OPTION) {
                    return false;
                }
            }

            break;
        }

        setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ));
        try {

            Writer writer = new OutputStreamWriter( new FileOutputStream( file ), "UTF-8" );
            try {
                List<BuilderComponent> list = new ArrayList<BuilderComponent>();
                list.add( canvas.getDisplay());
                BuilderComponentSaver.getInstance().save( writer, list );
            } finally {
                writer.close();
            }

            displaySaved( file );
            return true;

        } catch (Exception ex) {
            log.log( Level.SEVERE, "Cannot save " + file + ": " + ex.getMessage(), ex );
            JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Cannot Save Display",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        } finally {
            setCursor( Cursor.getDefaultCursor());
        }
    }

    protected boolean saveCurrent() {
        if (!canvas.isChanged()) {
            return true;
        }
        int res = JOptionPane.showConfirmDialog(
            this,
            "Current display was changed.\nDo you wish to save it?", 
            "Warning",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (res == JOptionPane.YES_OPTION) {
            return saveDisplay( false );
        } else {
            return res == JOptionPane.NO_OPTION;
        }
    }

    protected void displaySaved( File file ) {
        canvas.setChanged( false );
        setCurrentFile( file );
    }

    private void launchProject() {
        try {
            GenericContainer project = canvas.getDisplay();
            if (project == null) {
                return;
            }
            Document doc = BuilderComponentSaver.getInstance().newDocument();
            Element xml = project.getXML( doc );
            doc.appendChild( xml );
            Viewer viewer = createViewer();
            viewer.setVisible( true );
            viewer.toFront();
            viewer.requestFocusInWindow();
            String name = (currentFile == null) ? "Untitled" : currentFile.getName();
            viewer.openDisplay( doc, name );
        } catch (Exception ex) {
            showException( ex );
        }
    }

    protected Viewer createViewer() {
        return new Viewer( false );
    }
    
    @Override
    public void dispose() {
        if (!saveCurrent()) {
            return;
        }
        savePreferences();
        DaqInterfaceFactory.shutOffSharedInstance();
        super.dispose();
        System.exit( 0 );
    }

    protected void savePreferences() {
        frameSaver.save( prefs );
        canvas.getGridAttributes().save( prefs );
        prefs.putInt( "divider-location", splitPane.getDividerLocation());
        prefs.putInt( "last-divider-location", splitPane.getLastDividerLocation());
        prefs.putInt( "default-display-width", dispFac.getDefaultWidth());
        prefs.putInt( "default-display-height", dispFac.getDefaultHeight());
        Color bgColor = dispFac.getDefaultBackground();
        prefs.put( "default-display-background", (bgColor == null) ? "" : String.valueOf( bgColor ));
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
    
    private class ElementListener implements MenuListener {

        @Override
        public void menuSelected( MenuEvent e ) {
            boolean res = false;
            for (Component c : mnuBgImage.getMenuComponents()) {
                if (c.isEnabled()) {
                    res = true;
                    break;
                }
            }
            mnuBgImage.setEnabled( res );
        }

        @Override
        public void menuDeselected( MenuEvent e ) {}

        @Override
        public void menuCanceled( MenuEvent e ) {}

    }

    private class NewAction extends SynopticFrameAction {
        
        NewAction() {
            super( "New", 'N', 
                    "/toolbarButtonGraphics/general/New16.gif", 'N', CTRL_MASK );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            if (!saveCurrent()) {
                return;
            }
            canvas.setDisplay( dispFac.createNewDisplay());
            setCurrentFile( null );
        }
        
    }

    private class OpenAction extends SynopticFrameAction {
        
        OpenAction() {
            super( "Open...", 'O', 
                    "/toolbarButtonGraphics/general/Open16.gif", 'O', CTRL_MASK );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            if (!saveCurrent()) {
                return;
            }
            initFileDialog( "Load Display" );
            if (fileDialog.showOpenDialog( Builder.this ) != APPROVE_OPTION) {
                return;
            }
            openDisplay( fileDialog.getSelectedFile());
        }
        
    }

    private class DownloadAction extends SynopticFrameAction {
        
        DownloadAction() {
            super( "Download & Open...", 'D', 
                    "/toolbarButtonGraphics/general/Import16.gif", 'D', CTRL_MASK );
            setEnabled( System.getProperty( "Synoptic.repository-root" ) != null );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            if (!saveCurrent()) {
                return;
            }
            initRepoDialog();
            if (repoDialog.showOpenDialog( Builder.this ) != APPROVE_OPTION) {
                return;
            }
            openDisplay( repoDialog.getSelectedFile());
        }
        
    }

    private class SaveAction extends SynopticFrameAction {
        
        SaveAction() {
            super( "Save", 'S', 
                    "/toolbarButtonGraphics/general/Save16.gif", 'S', CTRL_MASK );
            setEnabled( false );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            saveDisplay( false );
        }
        
    }

    private class SaveAsAction extends SynopticFrameAction {
        
        SaveAsAction() {
            super( "Save As...", 'A', 
                    "/toolbarButtonGraphics/general/SaveAs16.gif" );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            saveDisplay( true );
        }
        
    }

    private class PreferencesAction extends SynopticFrameAction {

        PreferencesAction() {
            super( "Preferences...", 'P', null, KeyEvent.VK_COMMA, CTRL_MASK );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            GridAttributes gridAttrs = canvas.getGridAttributes();
            PreferencesDialog dialog = new PreferencesDialog( 
                gridAttrs.getStep(),
                dispFac.getDefaultWidth(),
                dispFac.getDefaultHeight(),
                dispFac.getDefaultBackground()
            );
            int res = JOptionPane.showConfirmDialog( 
                    Builder.this,
                    dialog, 
                    "Builder Preferences", 
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE );
            if (res == JOptionPane.OK_OPTION) {
                gridAttrs.setStep( dialog.getGridStep());
                dispFac.setDefaultWidth( dialog.getDefaultWidth());
                dispFac.setDefaultHeight( dialog.getDefaultHeight());
                dispFac.setDefaultBackground( dialog.getDefaultBgColor());
            }
            repaint();
        }
        
    }
    
    private class PrintAction extends SynopticFrameAction {
        
        PrintAction() {
            super( "Print", 'I', 
                    "/toolbarButtonGraphics/general/Print16.gif", 'P', CTRL_MASK );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            PageFormat format = getPageFormat();
            canvas.setPageFormat( format );
            PrinterJob job = getPrinterJob();
            try {
                job.setPageable( canvas );
                job.setPrintable( canvas, format );
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

    private class DiscardAction extends SynopticFrameAction {
        
        DiscardAction() {
            super( "Cancel New Element", 'C',
                    "resources/Pointer16.gif", KeyEvent.VK_ESCAPE, 0 );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            canvas.place( null );
        }
        
    }

    private class NestedAction extends SynopticFrameAction {
        
        NestedAction() {
            super( "Place Nested Project", 'N', null );
            setEnabled( false );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            // Not implemented yet
        }
        
    }

    private class LinkAction extends SynopticFrameAction {
        
        LinkAction() {
            super( "Place Link", 'P',
                    "resources/Link16.gif", KeyEvent.VK_C, 0 );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            canvas.place( new ComponentLink());
        }
        
    }

    private class LaunchAction extends SynopticFrameAction {
        
        LaunchAction() {
            super( "Launch Display", 'L',
                    "/toolbarButtonGraphics/media/Play16.gif", KeyEvent.VK_F9, 0 );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            launchProject();
        }
        
    }

}
