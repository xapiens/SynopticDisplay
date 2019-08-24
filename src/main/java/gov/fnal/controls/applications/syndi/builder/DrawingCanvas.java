// (c) 2001-2010 Fermi Research Allaince
// $Id: DrawingCanvas.java,v 1.5 2010/09/20 21:54:07 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.AbstractAction;
import gov.fnal.controls.applications.syndi.SynopticFrameAction;
import gov.fnal.controls.applications.syndi.builder.element.AbstractCanvasAction;
import gov.fnal.controls.applications.syndi.builder.element.GridAttributes;
import gov.fnal.controls.applications.syndi.builder.element.SelectComponentAction;
import gov.fnal.controls.applications.syndi.builder.element.InfoPanel;
import gov.fnal.controls.applications.syndi.builder.element.pin.PinSelectPanel;
import gov.fnal.controls.applications.syndi.builder.element.AbstractComponent;
import gov.fnal.controls.applications.syndi.builder.element.BuilderContainer;
import gov.fnal.controls.applications.syndi.builder.element.Config;
import gov.fnal.controls.applications.syndi.builder.element.GenericContainer;
import gov.fnal.controls.applications.syndi.builder.element.GenericContainer.State;
import gov.fnal.controls.applications.syndi.builder.element.BuilderComponent;
import gov.fnal.controls.applications.syndi.builder.element.BuilderComponentLoader;
import gov.fnal.controls.applications.syndi.builder.element.ComponentLink;
import gov.fnal.controls.applications.syndi.builder.element.ComponentType;
import gov.fnal.controls.applications.syndi.builder.element.pin.PinCollection;
import gov.fnal.controls.applications.syndi.builder.element.BuilderComponentSaver;
import gov.fnal.controls.applications.syndi.builder.element.LinkInfoPanel;
import gov.fnal.controls.applications.syndi.builder.element.Rotatable;
import gov.fnal.controls.applications.syndi.builder.element.TempContainer;
import gov.fnal.controls.applications.syndi.property.ComponentProperty;
import gov.fnal.controls.applications.syndi.property.PropertyList;
import gov.fnal.controls.applications.syndi.property.PropertyException;
import gov.fnal.controls.applications.syndi.util.ImageFactory;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import static java.awt.event.KeyEvent.ALT_MASK;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/20 21:54:07 $
 */
public class DrawingCanvas extends JComponent implements BuilderContainer, Config,
        MouseListener, MouseMotionListener, PropertyChangeListener, ComponentListener,
        Printable, Pageable {

    private static final int MARGIN = 10;
    private static final int HISTORY_SIZE = 32;

    private static final float MAX_ZOOM = 16.0f;
    private static final float MIN_ZOOM = 1.0f/16;
    
    private static final int MAX_BG_IMAGE_SIZE = 400; // kB

    private static final Map<RenderingHints.Key,Object> RENDERING_HINTS;

    static {
        Map<RenderingHints.Key,Object> hints = new HashMap<RenderingHints.Key,Object>();
        hints.put( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
        hints.put( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
        RENDERING_HINTS = Collections.unmodifiableMap( hints );
    }
    
    private static final Logger log = Logger.getLogger( DrawingCanvas.class.getName());
    
    private static final EnumSet<ComponentType> INVISIBLE_TYPES = 
        EnumSet.of( 
            ComponentType.DATA_CHANNEL,
            ComponentType.DATA_PIPE,
            ComponentType.LINK 
        );
    
    private final AbstractCanvasAction[] history = new AbstractCanvasAction[ HISTORY_SIZE ];
    private final EnumSet<ComponentType> visibleTypes = EnumSet.allOf( ComponentType.class );
    private final JPopupMenu popup = new JPopupMenu();
    private final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    private final Action undoAction = new UndoAction();
    private final Action redoAction = new RedoAction();
    private final Action repaintAction = new RepaintAction();
    private final Action deleteAction = new DeleteAction();
    private final Action propertiesAction = new PropertiesAction();
    private final Action globalPropsAction = new GlobalPropsAction();
    private final Action ioConfigAction = new IOConfigAction();
    private final Action infoAction = new InfoAction();
    private final Action rotateAction = new RotateAction();
    private final Action loadBackgroundAction = new LoadBackgroundAction();
    private final Action saveBackgroundAction = new SaveBackgroundAction();
    private final Action clearBackgroundAction = new ClearBackgroundAction();
    private final Action selectAllAction = new SelectAllAction();
    private final Action cutAction = new CutAction();
    private final Action copyAction = new CopyAction();
    private final Action pasteAction = new PasteAction();
    private final Action findAction = new FindAction();
    private final Action toFrontAction = new ToFrontAction();
    private final Action toBackAction = new ToBackAction();
    private final Action zoomInAction = new ZoomInAction();
    private final Action zoomOutAction = new ZoomOutAction();
    private final Action actualSizeAction = new ActualSizeAction();
    private final Action showGridAction = new ShowGridAction();
    private final Action snapToGridAction = new SnapToGridAction();
    private final Action hideInvisibleAction = new HideInvisibleAction();

    private final JMenu mnuBgImage  = new JMenu( "Background Image" );
    private final JMenu mnuCompStack  = new JMenu( "Component Stack" );

    private final JFileChooser imageChooser = new JFileChooser();

    private GridAttributes gridAttributes = new GridAttributes();
    private int historyIndex = -1;
    private GenericContainer disp = null;
    private boolean moveOver = false;
    private boolean changed = false;
    private Point origin = new Point( 0, 0 );
    private float zoom = 1;
    private Point dispPoint, contPoint;
    private PageFormat format;
    
    public DrawingCanvas() {
        
        addMouseListener( this );
        addMouseMotionListener( this );
        addComponentListener( this );

        setFocusable( true );

        ActionMap am = getActionMap();
        am.put( "undo", undoAction );
        am.put( "redo", redoAction );
        am.put( "repaint", repaintAction );
        am.put( "delete", deleteAction );
        am.put( "properties", propertiesAction );
        am.put( "globalProps", globalPropsAction );
        am.put( "ioConfig", ioConfigAction );
        am.put( "info", infoAction );
        am.put( "rotate", rotateAction );
        am.put( "loadBackground", loadBackgroundAction );
        am.put( "saveBackground", saveBackgroundAction );
        am.put( "clearBackground", clearBackgroundAction );
        am.put( "selectAll", selectAllAction );
        am.put( "cut", cutAction );
        am.put( "copy", copyAction );
        am.put( "paste", pasteAction );
        am.put( "find", findAction );
        am.put( "toFront", toFrontAction );
        am.put( "toBack", toBackAction );
        am.put( "zoomIn", zoomInAction );
        am.put( "zoomOut", zoomOutAction );
        am.put( "actualSize", actualSizeAction );
        am.put( "showGrid", showGridAction );
        am.put( "snapToGrid", snapToGridAction );
        am.put( "hideInvisible", hideInvisibleAction );
        
        popup.add( propertiesAction );
        popup.add( ioConfigAction );
        popup.add( mnuBgImage );
            mnuBgImage.add( loadBackgroundAction );
            mnuBgImage.add( saveBackgroundAction );
            mnuBgImage.add( clearBackgroundAction );
        popup.add( infoAction );
        popup.addSeparator();
        popup.add( rotateAction );
        popup.add( toFrontAction );
        popup.add( toBackAction );
        popup.addSeparator();
        popup.add( cutAction );
        popup.add( copyAction );
        popup.add( pasteAction );
        popup.add( deleteAction );
        popup.addSeparator();
        popup.add( zoomInAction );
        popup.add( zoomOutAction );
        popup.addSeparator();
        popup.add( mnuCompStack );

        popup.addPopupMenuListener( new PopupListener());
        
        showGridAction.putValue( Action.SELECTED_KEY, gridAttributes.isVisible());
        snapToGridAction.putValue( Action.SELECTED_KEY, gridAttributes.isEnabled());

        imageChooser.setFileFilter( new ImageFileFilter());
        
        setDisplay( new GenericContainer());

    }
    
    public void setDisplay( GenericContainer disp ) {
        if (disp == null) {
            throw new NullPointerException();
        }
        if (disp == this.disp) {
            return;
        }
        if (this.disp != null) {
            this.disp.setParent( null );
        }
        
        this.disp = disp;
        disp.setParent( this );
        
        disp.setLocation( 0, 0 );
        displayResized();
        
        disp.selectNone();
        selectionChanged( null );
        
        Arrays.fill( history, null );
        historyIndex = -1;

        setChanged( false );
        moveOver = false;

        repaint();
    }

    @Override
    public boolean add( BuilderComponent comp ) {
        return false;
    }

    @Override
    public Iterator<BuilderComponent> iterator() {
        return null;
    }

    public GenericContainer getDisplay() {
        return disp;
    }
    
    @Override
    public void paint( Graphics g ) {
        Point p = origin;
        if (p == null || p.x < 0 || p.y < 0) {
            return;
        }
        Graphics2D g2 = (Graphics2D)g;
        g2.translate( p.x, p.y );
        g2.scale( zoom, zoom );
        RenderingHints storedHints = ((Graphics2D)g).getRenderingHints();
        g2.setRenderingHints( RENDERING_HINTS );
        disp.paint( g2 );
        g2.setRenderingHints( storedHints );
        g2.scale( 1 / zoom, 1 / zoom );
        g2.translate( -p.x, -p.y );
    }

    @Override
    public void repaintComponent( int x, int y, int w, int h ) {
        repaint( 
            (int)(x * zoom) + origin.x,
            (int)(y * zoom) + origin.y,
            (int)((w + 0.5f) * zoom), 
            (int)((h + 0.5f) * zoom)
        );
    }

    private Dimension getViewSize() {
        Container parent = getParent();
        if (parent instanceof JViewport) {
            return ((JViewport)parent).getExtentSize();
        } else {
            return getSize();
        }
    }

    private Point getViewPosition() {
        Container parent = getParent();
        if (parent instanceof JViewport) {
            return ((JViewport)parent).getViewPosition();
        } else {
            return new Point( 0, 0 );
        }
    }

    private void setViewPosition( Point p ) {
        Container parent = getParent();
        if (parent instanceof JViewport) {
            ((JViewport)parent).setViewPosition( p );
        }
    }

    private void displayResized() {
        Dimension d = new Dimension(
            (int)((disp.getWidth() + 2 * MARGIN) * zoom),
            (int)((disp.getHeight() + 2 * MARGIN) * zoom)
        );
        setPreferredSize( d );
        Dimension v = getViewSize();
        setSize(
            Math.max( d.width, v.width ),
            Math.max( d.height, v.height )
        );
        adjustOrigin();
        adjustViewPosition();
        repaint();
    }

    private boolean adjustOrigin() {
        Dimension d = getPreferredSize();
        Dimension v = getViewSize();
        Point p = new Point(
            (int)(MARGIN * zoom + (Math.max( d.width, v.width ) - d.width) / 2),
            (int)(MARGIN * zoom + (Math.max( d.height, v.height ) - d.height) / 2)
        );
        if (p.equals( origin )) {
            return false;
        }
        origin = p;
        invalidate();
        return true;
    }

    private void adjustViewPosition() {
        if (dispPoint == null || contPoint == null) {
            return;
        }
        Point contPoint1 = dispToCont( dispPoint );
        int dx = contPoint1.x - contPoint.x;
        int dy = contPoint1.y - contPoint.y;
        if (dx == 0 && dy == 0) {
            return;
        }
        Point p = getViewPosition();
        p.translate( dx, dy );
        setViewPosition( p );
        contPoint = contPoint1;
    }

    private Point contToDisp( Point p ) {
        return new Point(
            (int)((p.x - origin.x) / zoom),
            (int)((p.y - origin.y) / zoom)
        );
    }

    private Point dispToCont( Point p ) {
        return new Point(
            (int)(p.x * zoom) + origin.x,
            (int)(p.y * zoom) + origin.y
        );
    }

    private MouseEvent translate( MouseEvent e ) {
        Point p = contToDisp( e.getPoint());
        return new MouseEvent(
            e.getComponent(),
            e.getID(),
            e.getWhen(),
            e.getModifiers(),
            p.x,
            p.y,
            e.getClickCount(),
            e.isPopupTrigger()
        );
    }

    private void reportCursorPosition( Point contPoint, Point dispPoint ) {
        Point p0 = this.dispPoint;
        this.contPoint = contPoint;
        this.dispPoint = dispPoint;
        firePropertyChange( "pointer", p0, dispPoint );
    }

    private void updateComponentStack( int x, int y ) {
        Point p = contToDisp( new Point( x, y ));
        List<BuilderComponent> stack = disp.getComponentsAt( p );
        mnuCompStack.removeAll();
        for (BuilderComponent c : stack) {
            Action a = new SelectComponentInStackAction( c );
            JMenuItem mi = new JCheckBoxMenuItem( a );
            mi.setSelected( c.isSelected());
            mnuCompStack.add( mi );
        }
    }

    private void maybePopup( MouseEvent e ) {
        if (e.isConsumed() || !e.isPopupTrigger()) {
            return;
        }
        int x = e.getX();
        int y = e.getY();
        updateComponentStack( x, y );
        popup.show( this, x, y );
        //mnuCompStack.removeAll();
    }
    
    @Override
    public void mouseMoved( MouseEvent e ) {
        MouseEvent e1 = translate( e );
        boolean f = disp.contains( e1.getPoint());
        if (moveOver != f) {
            moveOver = f;
            if (moveOver) {
                disp.mouseEntered( e1 );
            } else {
                disp.mouseExited( e1 );
            }
        }
        if (moveOver) {
            disp.mouseMoved( e1 );
        }
        reportCursorPosition( e.getPoint(), moveOver ? e1.getPoint() : null );
    }

    @Override
    public void mouseDragged( MouseEvent e ) {
        MouseEvent e1 = translate( e );
        if (moveOver) {
            disp.mouseDragged( e1 );
        }
        reportCursorPosition( e.getPoint(), moveOver ? e1.getPoint() : null );
    }

    @Override
    public void mouseClicked( MouseEvent e ) {
        if (moveOver) {
            MouseEvent e1 = translate( e );
            disp.mouseClicked( e1 );
            if (!e1.isConsumed() && SwingUtilities.isLeftMouseButton( e1 ) 
                    && e1.getClickCount() >= 2) {
                propertiesAction.actionPerformed( null );
            }
        } else {
            disp.selectNone();
        }
    }

    @Override
    public void mousePressed( MouseEvent e ) {
        if (moveOver) {
            requestFocus();
            disp.mousePressed( translate( e ));
        }
        maybePopup( e );
    }

    @Override
    public void mouseReleased( MouseEvent e ) {
        if (moveOver) {
            disp.mouseReleased( translate( e ));
        }
        maybePopup( e );
    }

    @Override
    public void mouseEntered( MouseEvent e ) {}

    @Override
    public void mouseExited( MouseEvent e ) {}

    @Override
    public void doAction( CanvasAction action ) {
        AbstractCanvasAction aca = (AbstractCanvasAction)action;
        aca.setCanvas( this );
        if (historyIndex == HISTORY_SIZE - 1) {
            System.arraycopy( history, 1, history, 0, HISTORY_SIZE - 1 );
        } else {
            historyIndex++;
        }
        history[historyIndex] = aca;
        for (int i = historyIndex + 1; i < HISTORY_SIZE; i++) {
            history[i] = null;
        }
        action.reDo();
        historyChanged();
        if (!(action instanceof SelectComponentAction)) {
            setChanged( true );
        }
    }

    @Override
    public void componentShown( ComponentEvent e ) {}

    @Override
    public void componentHidden( ComponentEvent e ) {}

    @Override
    public void componentMoved( ComponentEvent e ) {}

    @Override
    public void componentResized( ComponentEvent e ) {
        if (adjustOrigin()) {
            repaint();
        }
    }

    private boolean isUndoEnabled() {
        if (historyIndex < 0) {
            return false;
        }
        for (int i = historyIndex; i >= 0; i--) {
            if (!(history[ i ] instanceof SelectComponentAction)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRedoEnabled() {
        if ((historyIndex >= HISTORY_SIZE - 1) || (history[ historyIndex + 1 ] == null)) {
            return false;
        }
        for (int i = historyIndex + 1; i < HISTORY_SIZE - 1; i++) {
            if (history[ i ] != null /* || (!(stack[ i ] instanceof SelectComponentAction)) */) {
                return true;
            }
        }
        return false;
    }

    private void historyChanged() {
        undoAction.setEnabled( isUndoEnabled());
        redoAction.setEnabled( isRedoEnabled());
    }
    
    public void place( BuilderComponent comp ) {
        List<BuilderComponent> list = new ArrayList<BuilderComponent>();
        if (comp != null) {
            list.add( comp );
        }
        disp.place( list );
    }
    
    public boolean isPlacing() {
        State state = disp.getState();
        return state == State.PLACE_COMPONENT || state == State.PLACE_LINE;
    }

    private BuilderComponent getSingleSelection() {
        List<BuilderComponent> sel = disp.getSelection();
        return sel.isEmpty() ? null : sel.get( 0 );
    }
    
    private void showException( Exception ex ) {
        JOptionPane.showMessageDialog(  
            this,
            ex.getMessage(), 
            "Error", 
            JOptionPane.ERROR_MESSAGE
        );
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged( boolean val ) {
        if (changed == val) {
            return;
        }
        changed = val;
        firePropertyChange( "change", !val, val );
    }

    private BufferedImage getImage() {
        int w = disp.getWidth() + 2 * HIT_TOLERANCE;
        int h = disp.getHeight() + 2 * HIT_TOLERANCE;
        BufferedImage res = new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB );
        Graphics2D g2 = (Graphics2D)res.getGraphics();
        g2.translate( HIT_TOLERANCE, HIT_TOLERANCE );
        disp.paint( g2 );
        return res;
    }
    
    @Override
    public int print( Graphics graphics, PageFormat format, int pageIndex  ) {
        
        if (pageIndex != 0) {
            return NO_SUCH_PAGE;
        }
        BufferedImage img = getImage();

        Graphics2D g = (Graphics2D)graphics;

        double x = format.getImageableX();
        double y = format.getImageableY();

        g.translate( x, y );
        
        double w = format.getImageableWidth();
        double h = format.getImageableHeight();
        
        AffineTransform xform = new AffineTransform();
        
        double c = Math.min( w / img.getWidth() , h / img.getHeight() );
        xform.scale( c, c );

        double tx = (w / c - img.getWidth()) / 2.0;
        double ty = (h / c - img.getHeight()) / 2.0;
        xform.translate( tx, ty );

        g.drawImage( img, xform, null );

        return PAGE_EXISTS;
    }


    @Override
    public int getNumberOfPages() {
        return 1;
    }
    
    public void setPageFormat( PageFormat format ) {
        this.format = format;
    }

    @Override
    public PageFormat getPageFormat( int pageIndex ) {
        return format;
    }

    @Override
    public Printable getPrintable( int pageIndex ) {
        return this;
    }

    public void setGridAttributes( GridAttributes attrs ) {
        if (attrs.equals( gridAttributes )) {
            return;
        }
        GridAttributes attrs0 = gridAttributes;
        this.gridAttributes = attrs;
        showGridAction.putValue( Action.SELECTED_KEY, gridAttributes.isVisible());
        snapToGridAction.putValue( Action.SELECTED_KEY, gridAttributes.isEnabled());
        firePropertyChange( "grid", attrs0, attrs );
        repaint();
    }

    @Override
    public GridAttributes getGridAttributes() {
        return gridAttributes;
    }

    @Override
    public EnumSet<ComponentType> getVisibleTypes() {
        return EnumSet.copyOf( visibleTypes );
    }

    @Override
    public void propertyChange( PropertyChangeEvent evt ) {
        String name = evt.getPropertyName();
        if ("selection".equals( name )) {
            selectionChanged( (BuilderComponent[])evt.getNewValue() );
        } else if ("size".equals( name ) && evt.getSource() == disp) {
            displayResized();
        } else if ("shape".equals( name )) {
            setChanged( true );
        }
        firePropertyChange( name, evt.getOldValue(), evt.getNewValue());
    }
    
    private void selectionChanged( BuilderComponent[] selection ) {
        if (selection == null) {
            selection = new BuilderComponent[ 0 ];
        }
        BuilderComponent comp = selection.length == 1 ? selection[ 0 ] : null;
        int size = 0;
        for (BuilderComponent c : selection) {
            if (c != disp) {
                size++;
            }
        }
        boolean plc = isPlacing();
        propertiesAction.setEnabled( !plc && comp != null );
        ioConfigAction.setEnabled( !plc && comp instanceof AbstractComponent &&
                !(comp instanceof BuilderContainer));
        rotateAction.setEnabled( !plc && comp != null && comp != disp && comp instanceof Rotatable);
        toBackAction.setEnabled( !plc && comp != null && comp != disp );
        toFrontAction.setEnabled( !plc && comp != null && comp != disp );
        infoAction.setEnabled( !plc && comp != null );
        cutAction.setEnabled( !plc && size > 0 );
        copyAction.setEnabled( !plc && size > 0 );
        deleteAction.setEnabled( !plc && size > 0 );
        pasteAction.setEnabled( !plc );
        selectAllAction.setEnabled( !plc );
        globalPropsAction.setEnabled( !plc );
        selectionBackgroundChanged( comp );
    }

    private void selectionBackgroundChanged( BuilderComponent selection ) {
        if (isPlacing() || !(selection instanceof AbstractComponent)
                || !((AbstractComponent)selection).isBackgroundImageEnabled()) {
            loadBackgroundAction.setEnabled( false );
            saveBackgroundAction.setEnabled( false );
            clearBackgroundAction.setEnabled( false );
        } else {
            loadBackgroundAction.setEnabled( true );
            saveBackgroundAction.setEnabled( ((AbstractComponent)selection).getBackgroundImage() != null );
            clearBackgroundAction.setEnabled( ((AbstractComponent)selection).getBackgroundImage() != null );
        }
    }

    private void setZoom( float zoom ) {
        if (this.zoom == zoom) {
            return;
        }
        float z0 = this.zoom;
        if (zoom > MAX_ZOOM) {
            zoom = MAX_ZOOM;
        } else if (zoom < MIN_ZOOM) {
            zoom = MIN_ZOOM;
        } 
        this.zoom = zoom;
        displayResized();
        zoomInAction.setEnabled( zoom < MAX_ZOOM );
        zoomOutAction.setEnabled( zoom > MIN_ZOOM );
        actualSizeAction.setEnabled( zoom != 1.0f );
        firePropertyChange( "zoom", z0, zoom );
    }
    
    public float getZoom() {
        return zoom;
    }

    @Override
    public void setGlobalProperties( Collection<ComponentProperty<?>> props ) throws PropertyException {
        disp.setGlobalProperties( props );
    }

    @Override
    public Collection<ComponentProperty<?>> getGlobalProperties() {
        return disp.getGlobalProperties();
    }

    private class PopupListener implements PopupMenuListener {

        @Override
        public void popupMenuWillBecomeVisible( PopupMenuEvent e ) {
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
        public void popupMenuWillBecomeInvisible( PopupMenuEvent e ) {}

        @Override
        public void popupMenuCanceled( PopupMenuEvent e ) {}

    }

    private class UndoAction extends SynopticFrameAction {
        
        UndoAction() {
            super( "Undo", 'U', 
                    "/toolbarButtonGraphics/general/Undo16.gif", 'Z', CTRL_MASK );
        }
            
        @Override
        public void actionPerformed( ActionEvent e ) {
            AbstractCanvasAction action;
            if (isUndoEnabled()) {
                do {
                    action = history[ historyIndex-- ];
                    action.unDo();
                } while (historyIndex >= 0 && action instanceof SelectComponentAction);
            }
            historyChanged();
        }
        
    }

    private class RedoAction extends SynopticFrameAction {
        
        RedoAction() {
            super( "Redo", 'R', 
                    "/toolbarButtonGraphics/general/Redo16.gif", 'Y', CTRL_MASK );
        }
            
        @Override
        public void actionPerformed( ActionEvent e ) {
            AbstractCanvasAction action;
            boolean actionPerfd = false, stopFlag = false;
            if (isRedoEnabled()) {
                do {
                    action = history[ ++historyIndex ];
                    if (action != null) {
                        if (action instanceof SelectComponentAction) {
                            action.reDo();
                        } else if (!actionPerfd) {
                            action.reDo();
                            actionPerfd = true;
                        } else {
                            stopFlag = true;
                            historyIndex--;
                        }
                    } else {
                        historyIndex--;
                    }
                }
                while (historyIndex < HISTORY_SIZE - 1 
                        && action != null 
                        && (action instanceof SelectComponentAction || !stopFlag));
            }
            historyChanged();
        }
        
    }
    
    private class RepaintAction extends SynopticFrameAction {
        
        RepaintAction() {
            super( "Repaint", 'R', 
                    "/toolbarButtonGraphics/general/Refresh16.gif", KeyEvent.VK_F5, 0 );
        }
            
        @Override
        public void actionPerformed( ActionEvent e ) {
            if (disp != null) {
                disp.reload();
                disp.repaintComponent();
            }
        }
        
    }
    
    private class DeleteAction extends SynopticFrameAction {
        
        DeleteAction() {
            super( "Delete", 'D', 
                    "/toolbarButtonGraphics/general/Remove16.gif", KeyEvent.VK_DELETE, 0 );
        }
            
        @Override
        public void actionPerformed( ActionEvent e ) {
            disp.deleteSelection();
        }
        
    }

    private class PropertiesAction extends SynopticFrameAction {
        
        PropertiesAction() {
            super( "Properties", 'E',
                    "resources/EleProp16.gif", KeyEvent.VK_ENTER, ALT_MASK );
        }
            
        @Override
        public void actionPerformed( ActionEvent e ) {
            BuilderComponent comp = getSingleSelection();
            if (comp instanceof ComponentLink) {
                showLinkProperties( (ComponentLink)comp );
            } else if (comp != null) {
                showComponentProperties( comp );
            }
        }

        private void showComponentProperties( BuilderComponent comp ) {

            PropertyList props = new PropertyList( comp.getProperties());
            PropertyTable table = new PropertyTable( props );
            JScrollPane scroll = new JScrollPane( table );
            
            Object[] options;
            
            String helpUrl = comp.getHelpUrl();
            if (helpUrl != null) {
                options = new Object[]{ "Ok", "Cancel", new HelpButton( helpUrl )};
            } else {
                options = new Object[]{ "Ok", "Cancel" };
            }

            JOptionPane pane = new JOptionPane( scroll, JOptionPane.PLAIN_MESSAGE );
            pane.setOptions( options );
            String title = "Properties of " + comp;
            JDialog dialog = pane.createDialog( DrawingCanvas.this, title );
            try {
                while (true) {
                    dialog.setVisible( true );
                    if (!"Ok".equals( pane.getValue())) {
                        return;
                    }
                    table.commitChanges();
                    setChanged( true );
                    try {
                        comp.setProperties( props );
                        break;
                    } catch (Exception ex) {
                        showException( ex );
                    }
                }
            } finally {
                dialog.dispose();
            }
            repaint();
        }

        private void showLinkProperties( ComponentLink link ) {
            LinkInfoPanel panel = new LinkInfoPanel( link );
            JOptionPane pane = new JOptionPane( panel, JOptionPane.PLAIN_MESSAGE );
            pane.setOptions( new String[]{ "Close" });
            String title = "Properties of " + link;
            JDialog dialog = pane.createDialog( DrawingCanvas.this, title );
            dialog.setVisible( true );
            dialog.dispose();
        }
        
    }

    private class GlobalPropsAction extends SynopticFrameAction {

        GlobalPropsAction() {
            super( "Global Properties...", 'G', null );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {

            PropertyList props = new PropertyList( getGlobalProperties());
            GlobalPropertyTable table = new GlobalPropertyTable( props );
            JScrollPane scroll = new JScrollPane( table );

            String helpUrl = "/ParametrizedSynopticDisplays";
            Object[] options = new Object[]{ "Ok", "Cancel", new HelpButton( helpUrl )};

            String title = "Display's Global Properties";

            JOptionPane pane = new JOptionPane( scroll, JOptionPane.PLAIN_MESSAGE );
            pane.setOptions( options );
            JDialog dialog = pane.createDialog( DrawingCanvas.this, title );

            while (true) {
                dialog.setVisible( true );
                if (!"Ok".equals( pane.getValue())) {
                    return;
                }
                table.commitChanges();
                setChanged( true );
                try {
                    setGlobalProperties( props );
                    break;
                } catch (Exception ex) {
                    showException( ex );
                }
            }
            repaint();
        }

    }

    private class IOConfigAction extends SynopticFrameAction {
        
        IOConfigAction() {
            super( "I/O Config...", 'O',
                    "resources/ElePin16.gif", KeyEvent.VK_F11, 0 );
        }
            
        @Override
        public void actionPerformed( ActionEvent e ) {
            BuilderComponent comp = getSingleSelection();
            if (!(comp instanceof AbstractComponent)) {
                return;
            }
            PinCollection pins = ((AbstractComponent)comp).pins();
            PinSelectPanel editor = new PinSelectPanel( pins );
            while (true) {
                int res = JOptionPane.showConfirmDialog( 
                    DrawingCanvas.this,
                    editor, 
                    comp.getName() + " Pins",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE 
                );
                if (res != JOptionPane.OK_OPTION) {
                    return;
                }
                setChanged( true );
                try {
                    pins.changePinCount( editor.getInputs(), editor.getOutputs());
                    break;
                } catch (Exception ex) {
                    showException( ex );
                }
            }
            repaint();
        }
        
    }

    private class InfoAction extends SynopticFrameAction {
        
        InfoAction() {
            super( "Information...", 'N', 
                    "/toolbarButtonGraphics/general/About16.gif", 'I', CTRL_MASK );
        }
            
        @Override
        public void actionPerformed( ActionEvent e ) {
            BuilderComponent comp = getSingleSelection();
            if (comp == null) {
                return;
            }
            String desc = comp.getDescription();
            if (desc == null) {
                desc = "";
            }
            InfoPanel editor = new InfoPanel( desc, true );
            JScrollPane scroll = new JScrollPane( editor );
            while (true) {
                int res = JOptionPane.showConfirmDialog( 
                    DrawingCanvas.this,
                    scroll, 
                    comp.toString(),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE 
                );
                if (!editor.isEditable() || res != JOptionPane.OK_OPTION) {
                    return;
                }
                setChanged( true );
                try {
                    desc = editor.getText().trim();
                    if ("".equals( desc )) {
                        desc = null;
                    }
                    comp.setDescription( desc );
                    break;
                } catch (Exception ex) {
                    showException( ex );
                }
            }
        }
        
    }

    private class RotateAction extends SynopticFrameAction {
        
        RotateAction() {
            super( "Rotate", 'R',
                    "resources/EleRotate16.gif", 'R', ALT_MASK );
        }
            
        @Override
        public void actionPerformed( ActionEvent e ) {
            BuilderComponent comp = getSingleSelection();
            if (comp instanceof Rotatable) {
                ((Rotatable)comp).rotate();
            }
        }
        
    }

    private class LoadBackgroundAction extends SynopticFrameAction {
        
        LoadBackgroundAction() {
            super( "Load From File...", 'L', null );
        }
            
        @Override
        public void actionPerformed( ActionEvent e ) {
            BuilderComponent comp = getSingleSelection();
            if (!(comp instanceof AbstractComponent) 
                    || !((AbstractComponent)comp).isBackgroundImageEnabled()) {
                return;
            }
            do {
                imageChooser.setDialogTitle( "Load Background Image" );
                int res = imageChooser.showDialog( DrawingCanvas.this, "Load" );
                if (res != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                File file = imageChooser.getSelectedFile();
                try {
                    if (file.length() / 1024 > MAX_BG_IMAGE_SIZE) {
                        throw new Exception( "Image is too big. " +
                                "Maximum size is " + MAX_BG_IMAGE_SIZE + " kB" );
                    }
                    Image img = ImageFactory.load( file );
                    ((AbstractComponent)comp).setBackgroundImage( img );
                    repaint();
                    selectionBackgroundChanged( comp );
                    setChanged( true );
                    return;
                } catch (Exception ex) {
                    showException( ex );
                }
            } while (true);
        }
        
    }

    private class SaveBackgroundAction extends SynopticFrameAction {

        SaveBackgroundAction() {
            super( "Save To File...", 'S', null );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            BuilderComponent comp = getSingleSelection();
            if (!(comp instanceof AbstractComponent)
                    || !((AbstractComponent)comp).isBackgroundImageEnabled()) {
                return;
            }
            Image img = ((AbstractComponent)comp).getBackgroundImage();
            if (img == null) {
                return;
            }
            do {
                imageChooser.setDialogTitle( "Save Background Image" );
                int res = imageChooser.showDialog( DrawingCanvas.this, "Save" );
                if (res != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                File file = imageChooser.getSelectedFile();
                if (file.exists()) {
                    res = JOptionPane.showConfirmDialog(
                        DrawingCanvas.this,
                        "Are you sure you want to overwrite the existing file?",
                        "File already exists",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                    );
                    if (res != JOptionPane.YES_OPTION) {
                        continue;
                    }
                }
                try {
                    ImageFactory.save( img, file );
                    return;
                } catch (Exception ex) {
                    showException( ex );
                }
            } while (true);
        }

    }

    private class ClearBackgroundAction extends SynopticFrameAction {
        
        ClearBackgroundAction() {
            super( "Clear", 'C', null );
        }
            
        @Override
        public void actionPerformed( ActionEvent e ) {
            BuilderComponent comp = getSingleSelection();
            if (!(comp instanceof AbstractComponent)
                    || !((AbstractComponent)comp).isBackgroundImageEnabled()) {
                return;
            }
            int res = JOptionPane.showConfirmDialog( 
                DrawingCanvas.this,
                "Are you sure you want to remove the background image?", 
                "Removing Background", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE 
            );
            if (res == JOptionPane.YES_OPTION) {
                ((AbstractComponent)comp).setBackgroundImage( null );
                repaint();
                selectionBackgroundChanged( comp );
                setChanged( true );
            }
        }
        
    }

    private class SelectAllAction extends SynopticFrameAction {
        
        SelectAllAction() {
            super( "Select All", 'A', null, 'A', CTRL_MASK );
        }
            
        @Override
        public void actionPerformed( ActionEvent e ) {
            disp.selectAll();
        }
        
    }

    private class CutAction extends SynopticFrameAction {
        
        CutAction() {
            super( "Cut", 'T', 
                    "/toolbarButtonGraphics/general/Cut16.gif", 'X', CTRL_MASK );
        }
            
        @Override
        public void actionPerformed( ActionEvent e ) {
            copyAction.actionPerformed( null );
            deleteAction.actionPerformed( null );
        }
        
    }

    private class CopyAction extends SynopticFrameAction {
        
        CopyAction() {
            super( "Copy", 'C', 
                    "/toolbarButtonGraphics/general/Copy16.gif", 'C', CTRL_MASK );
        }
            
        @Override
        public void actionPerformed( ActionEvent e ) {
            List<BuilderComponent> cc = new ArrayList<BuilderComponent>();
            for (BuilderComponent c : disp.getSelection()) {
                if (c != disp) {
                    cc.add( c );
                }
            }
            CharArrayWriter writer = new CharArrayWriter();
            try {
                BuilderComponentSaver.getInstance().save( writer, cc );
                StringSelection buf = new StringSelection( writer.toString());
                clipboard.setContents( buf, null );
            } catch (Exception ex) {
                log.log( Level.SEVERE, "Cannot copy to clipboard", ex );
            } finally {
                writer.close();
            }
        }
        
    }
    
    private class PasteAction extends SynopticFrameAction {

        PasteAction() {
            super( "Paste", 'P', 
                    "/toolbarButtonGraphics/general/Paste16.gif", 'V', CTRL_MASK );
            setEnabled( true );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            Transferable xfer = clipboard.getContents( this );
            if (xfer == null || !xfer.isDataFlavorSupported( DataFlavor.stringFlavor )) {
                return;
            }
            try {
                String data = (String)xfer.getTransferData( DataFlavor.stringFlavor );
                if (data == null) {
                    return;
                }
                StringReader reader = new StringReader( data );
                try {
                    BuilderComponent comp = BuilderComponentLoader.getInstance().load( reader );
                    List<BuilderComponent> list = new ArrayList<BuilderComponent>();
                    if (comp instanceof TempContainer) {
                        for (BuilderComponent c : (TempContainer)comp) {
                            list.add( c );
                        }
                    } else {
                        list.add( comp );
                    }
                    disp.place( list );
                } finally {
                    reader.close();
                }
            } catch (Exception ex) {
                log.log( Level.SEVERE, "Cannot paste from clipboard", ex );
                JOptionPane.showMessageDialog(
                    DrawingCanvas.this,
                    ex.getMessage(),
                    "Cannot Paste From Clipboard",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }

    }

    private class FindAction extends SynopticFrameAction {

        FindAction() {
            super( "Find...", 'F', null, 'F', CTRL_MASK );
            setEnabled( true );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            FindDialog findPanel = new FindDialog( null );
            JOptionPane pane = new JOptionPane( findPanel, JOptionPane.PLAIN_MESSAGE );
            pane.setOptions( new Object[]{ "Find", "Cancel" });
            JDialog dialog = pane.createDialog( DrawingCanvas.this, "Find" );
            try {
                while (true) {
                    dialog.setVisible( true );
                    Object value = pane.getValue();
                    if (!("Find".equals( value ) || "uninitializedValue".equals( value ))) {
                        return;
                    }
                    Integer compId = findPanel.getComponentId();
                    if (compId == null) {
                        JOptionPane.showMessageDialog(
                            DrawingCanvas.this,
                            "Invalid Component ID",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                        continue;
                    }
                    if (disp.select( compId )) {
                        break;
                    }
                    JOptionPane.showMessageDialog(
                        DrawingCanvas.this,
                        "Component #" + compId + " is not found.",
                        "Not Found",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                }
            } finally {
                dialog.dispose();
            }
        }

    }

    private class ToFrontAction extends SynopticFrameAction {
        
        ToFrontAction() {
            super( "Bring To Front", 'F', null, 'F', ALT_MASK );
        }
            
        @Override
        public void actionPerformed( ActionEvent e ) {
            BuilderComponent comp = getSingleSelection();
            if (comp == null) {
                return;
            }
            disp.changeOrder( comp, 1 );
        }
        
    }

    private class ToBackAction extends SynopticFrameAction {
        
        ToBackAction() {
            super( "Send To Back", 'B', null, 'B', ALT_MASK );
        }
            
        @Override
        public void actionPerformed( ActionEvent e ) {
            BuilderComponent comp = getSingleSelection();
            if (comp == null) {
                return;
            }
            disp.changeOrder( comp, -1 );
        }
        
    }

    private class ZoomInAction extends SynopticFrameAction {
        
        ZoomInAction() {
            super( "Zoom In", 'I', 
                    "/toolbarButtonGraphics/general/ZoomIn16.gif", KeyEvent.VK_EQUALS, CTRL_MASK );
            setEnabled( true );
        }
            
        @Override
        public void actionPerformed( ActionEvent e ) {
            setZoom( zoom * 2 );
        }
        
    }

    private class ZoomOutAction extends SynopticFrameAction {
        
        ZoomOutAction() {
            super( "Zoom Out", 'O', 
                    "/toolbarButtonGraphics/general/ZoomOut16.gif", KeyEvent.VK_MINUS, CTRL_MASK );
            setEnabled( true );
        }
            
        @Override
        public void actionPerformed( ActionEvent e ) {
            setZoom( zoom / 2 );
        }
        
    }

    private class ActualSizeAction extends SynopticFrameAction {
        
        ActualSizeAction() {
            super( "Actual Size", 'A', 
                    "/toolbarButtonGraphics/general/AlignCenter16.gif", '0', CTRL_MASK );
            setEnabled( false );
        }
            
        @Override
        public void actionPerformed( ActionEvent e ) {
            setZoom( 1 );
        }
        
    }

    private class ShowGridAction extends SynopticFrameAction {
        
        ShowGridAction() {
            super( "Show Grid", 'G', null );
            putValue( Action.SELECTED_KEY, true );
            setEnabled( true );
        }
            
        @Override
        public void actionPerformed( ActionEvent e ) {
            gridAttributes.setVisible( (Boolean)getValue( SELECTED_KEY ));
            repaint();
        }
        
    }

    private class SnapToGridAction extends SynopticFrameAction {
        
        SnapToGridAction() {
            super( "Snap To Grid", 'S', null );
            putValue( Action.SELECTED_KEY, true );
            setEnabled( true );
        }
            
        @Override
        public void actionPerformed( ActionEvent e ) {
            gridAttributes.setEnabled( (Boolean)getValue( SELECTED_KEY ));
            repaint();
        }
        
    }

    private class HideInvisibleAction extends SynopticFrameAction {
        
        HideInvisibleAction() {
            super( "Hide Invisible Components", 'H', 
                    "/toolbarButtonGraphics/general/Find16.gif" );
            putValue( Action.SELECTED_KEY, false );
            setEnabled( true );
        }
            
        @Override
        public void actionPerformed( ActionEvent e ) {
            boolean val = getValue( Action.SELECTED_KEY ) == Boolean.TRUE;
            if (val) {
                visibleTypes.removeAll( INVISIBLE_TYPES );
            } else {
                visibleTypes.addAll( INVISIBLE_TYPES );
            }
            repaint();
        }
        
    }

    private class SelectComponentInStackAction extends AbstractAction {

        private final BuilderComponent comp;

        public SelectComponentInStackAction( BuilderComponent comp ) {
            super( comp.getCaption());
            this.comp = comp;
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            disp.select( comp );
        }

    }
    
}
