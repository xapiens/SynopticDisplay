// (c) 2001-2010 Fermi Research Allaince
// $Id: RootComponent.java,v 1.3 2010/09/20 21:55:16 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime;

import gov.fnal.controls.applications.syndi.runtime.daq.DaqChannel;
import gov.fnal.controls.applications.syndi.runtime.daq.DaqInterface;
import gov.fnal.controls.applications.syndi.runtime.daq.DaqInterfaceFactory;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import org.w3c.dom.Element;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/20 21:55:16 $
 */
public class RootComponent extends DisplayComponent 
        implements Printable, Pageable, MouseListener {

    private static final Map<RenderingHints.Key,Object> RENDERING_HINTS;

    static {
        Map<RenderingHints.Key,Object> hints = new HashMap<RenderingHints.Key,Object>();
        hints.put( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
        hints.put( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
        RENDERING_HINTS = Collections.unmodifiableMap( hints );
    }

    private static final int UPDATE_RATE = 1000;

    private static final Logger log = Logger.getLogger( RootComponent.class.getName());
    private static final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

    private final JPopupMenu popup = new JPopupMenu();

    private final AbstractAction listDataChannelsAction = new ListDataChannelsAction();

    private DaqInterface daq;
    private Future<?> updateTask;
    private PageFormat pageFormat;

    public RootComponent() {

        addMouseListener( this );

        ActionMap am = getActionMap();
        am.put( "listDataChannels", listDataChannelsAction );

        popup.add( listDataChannelsAction );

    }

    @Override
    public void init( Element source ) throws Exception {
        super.init( source );
        Set<DaqChannel> channels = getChannels();
        if (!channels.isEmpty()) {
            DaqInterfaceFactory daqFac = DaqInterfaceFactory.getSharedInstance();
            daq = daqFac.createDaqInterface();
            daq.start( channels );
            daq.addSettingStateListener( this );
            setSettingEnabled( daq.isSettingEnabled());
        }
        updateTask = exec.scheduleAtFixedRate(
            this,
            UPDATE_RATE,
            UPDATE_RATE,
            TimeUnit.MILLISECONDS
        );
    }

    public DaqInterface getDaqInterface() {
        return daq;
    }

    public void dispose() {
        setSettingEnabled( false );
        if (daq != null) {
            try {
                daq.stop();
                daq.removeSettingStateListener( this );
            } catch (Throwable ex) {
                log.throwing( RootComponent.class.getName(), "dispose", ex );
            } finally {
                daq = null;
            }
        }
        if (updateTask != null) {
            updateTask.cancel( false );
            updateTask = null;
        }
    }

    @Override
    public void paint( Graphics g ) {
        RenderingHints storedHints = ((Graphics2D)g).getRenderingHints();
        ((Graphics2D)g).setRenderingHints( RENDERING_HINTS );
        super.paint( g );
        ((Graphics2D)g).setRenderingHints( storedHints );
    }

    @Override
    public int print( Graphics g, PageFormat format, int pageIndex )
            throws PrinterException {

        if (pageIndex != 0) {
            return NO_SUCH_PAGE;
        }

        BufferedImage img = new BufferedImage(
            getWidth(),
            getHeight(),
            BufferedImage.TYPE_INT_ARGB
        );
        this.paint( img.getGraphics());

        Graphics2D g2 = (Graphics2D)g;

        double x = format.getImageableX();
        double y = format.getImageableY();

        g2.translate( x, y );

        double w = format.getImageableWidth();
        double h = format.getImageableHeight();

        AffineTransform xform = new AffineTransform();

        double c = Math.min( w / img.getWidth() , h / img.getHeight() );
        xform.scale( c, c );

        double tx = (w / c - img.getWidth()) / 2.0;
        double ty = (h / c - img.getHeight()) / 2.0;
        xform.translate( tx, ty );

        g2.drawImage( img, xform, null );

        return PAGE_EXISTS;
    }

    @Override
    public int getNumberOfPages() {
        return 1;
    }

    @Override
    public Printable getPrintable( int pageIndex ) {
        return this;
    }

    @Override
    public PageFormat getPageFormat( int pageIndex ) {
        return pageFormat;
    }

    public void setPageFormat( PageFormat pageFormat ) {
        this.pageFormat = pageFormat;
    }

    private void maybePopup( MouseEvent e ) {
        if (e.isPopupTrigger()) {
            popup.show( this, e.getX(), e.getY());
        }
    }

    @Override
    public void mouseClicked( MouseEvent e ) {}

    @Override
    public void mousePressed( MouseEvent e ) {
        maybePopup( e );
    }

    @Override
    public void mouseReleased( MouseEvent e ) {
        maybePopup( e );
    }

    @Override
    public void mouseEntered( MouseEvent e ) {}

    @Override
    public void mouseExited( MouseEvent e ) {}

    private class ListDataChannelsAction extends AbstractAction {

        ListDataChannelsAction() {
            super( "List Data Channels" );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            Set<DaqChannel> channels = getChannels();
            DataChannelList list = new DataChannelList( channels );
            JScrollPane scroll = new JScrollPane( list );
            JOptionPane.showMessageDialog(
                RootComponent.this,
                scroll,
                "Data Channels",
                JOptionPane.PLAIN_MESSAGE
            );
        }

    }

}
