// (c) 2001-2010 Fermi Research Allaince
// $Id: DisplayComponent.java,v 1.2 2010/09/15 15:25:14 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime;

import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.applications.syndi.runtime.daq.DaqChannel;
import gov.fnal.controls.applications.syndi.runtime.daq.SettingStateListener;
import gov.fnal.controls.applications.syndi.runtime.v11n.VisualComponent;
import gov.fnal.controls.tools.timed.TimedNumber;
import java.awt.Component;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Run-time incarnation of a display 
 * 
 * @author  Andrey Petrov, Timofei Bolshakov
 * @version $Date: 2010/09/15 15:25:14 $
 */
public abstract class DisplayComponent extends JPanel implements RuntimeComponent, RuntimeControl,
        Runnable, SettingStateListener {

    private static final Logger log = Logger.getLogger( DisplayComponent.class.getName());

    private final List<RuntimeComponent> comps = new ArrayList<RuntimeComponent>();
    private final Map<Integer,RuntimeComponent> compMap = new HashMap<Integer,RuntimeComponent>();
    private final Map<Integer,Set<Integer>> linkMap = new HashMap<Integer,Set<Integer>>();

    private static Integer toInteger( Element source, String attName )
            throws DisplayFormatException {
        try {
            return new Integer( source.getAttribute( attName ));
        } catch (NumberFormatException ex) {
            throw new DisplayFormatException( "Invalid integer attribute \"" + attName + "\"" );
        }
    }
    
    protected DisplayComponent() {
        super( null ); // no layout manager
        super.setOpaque( false );
    }
    
    @Override
    public void init( Element source ) throws Exception {
        if (source == null) {
            throw new NullPointerException();
        }
        VisualComponent.handleStandartProps( PropertyCollection.create( source ), this );
        initComponents( source );
        initLinks( source );
    }

    protected void initComponents( Element source ) throws Exception {
        BackgroundComponentFactory bgFac = null;
        StaticComponentFactory svgFac = null;
        JComponent background = null;
        for (Node n = source.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element e = (Element)n;
            String tag = e.getTagName();
            if ("element".equals( tag )) {
                String runtimeClass = e.getAttribute( "implementation" );
                RuntimeComponent comp = RuntimeComponentFactory.createComponent( runtimeClass );
                Integer id = toInteger( e, "id" );
                if (compMap.containsKey( id )) {
                    throw new DisplayFormatException( "Duplicate component ID: " + id );
                }
                compMap.put( id, comp );
                if (comp instanceof JComponent) {
                    ((JComponent)comp).setDoubleBuffered( isDoubleBuffered());
                }
                if (comp instanceof Component) {
                    add( (Component)comp, 0 );
                }
                comp.init( e );
            } else if ("svg".equals( tag )) {
                if (svgFac == null) {
                    svgFac = new StaticComponentFactory();
                }
                add( svgFac.createComponent( e ), 0 );
            } else if ("bkimage".equals( tag )) {
                if (bgFac == null) {
                    bgFac = new BackgroundComponentFactory( this );
                }
                String type = e.getAttribute( "type" );
                if ("".equals( type )) {
                    type = null;
                }
                background = bgFac.createComponent( e, type );
                background.setDoubleBuffered( isDoubleBuffered());
            }
        }
        if (background != null) {
            add( background );
        }
    }

    protected void initLinks( Element source ) throws Exception {
        for (Node n = source.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element e = (Element)n;
            if (!"link".equals( e.getTagName())) {
                continue;
            }

            Integer sid = toInteger( e, "source" );
            Integer spin = toInteger( e, "source_pin" );
            RuntimeComponent src = compMap.get( sid );
            if (src == null) {
                throw new DisplayFormatException( "Illegal source component #" + sid );
            }

            Integer tid = toInteger( e, "target" );
            Integer tpin = toInteger( e, "target_pin" );
            RuntimeComponent tgt = compMap.get( tid );
            if (tgt == null) {
                throw new DisplayFormatException( "Illegal target component #" + tid );
            }

            src.setOutput( spin, tgt, tpin );
            tgt.setInput( tpin, src, spin );

            Set<Integer> targets = linkMap.get( sid );
            if (targets == null) {
                targets = new HashSet<Integer>();
                linkMap.put( sid, targets );
            }
            targets.add( tid );
        }
        if (hasCyclicLinks()) {
            throw new DisplayFormatException( "Cyclic link" );
        }
        createComponentList();
    }

    private boolean hasCyclicLinks() {
        Set<Integer> usd = new HashSet<Integer>();
        Set<Integer> src, tgt;
        for (Integer id : compMap.keySet()) {
            usd.clear();
            src = new HashSet<Integer>();
            src.add( id );
            do {
                tgt = new HashSet<Integer>();
                for (Integer i : src) {
                    if (linkMap.containsKey( i )) {
                        tgt.addAll( linkMap.get( i ));
                    }
                }
                if (tgt.contains( id )) {
                    return true;
                }
                tgt.removeAll( usd );
                usd.addAll( tgt );
                src = tgt;
            } while (!src.isEmpty());
        }
        return false;
    }

    private void createComponentList() {
        comps.clear();
        Set<Integer> heap = new TreeSet<Integer>( compMap.keySet()); // Remaining components
        Set<Integer> curr = new TreeSet<Integer>(); // Components on the current level
        StringBuilder buf = new StringBuilder();
        int level = 0;
        // Finding components that are connected only as sources
        for (Iterator<Integer> z = heap.iterator(); z.hasNext();) {
            Integer id = z.next();
            boolean f = false;
            for (Set<Integer> _targets : linkMap.values()) {
                if (_targets.contains( id )) {
                    f = true;
                    break;
                }
            }
            if (!f) {
                z.remove();
                curr.add( id );
            }
        }
        do {
            if (curr.isEmpty() && !heap.isEmpty()) {
                curr.add( heap.iterator().next());
                heap.removeAll( curr );
            }
            do {
                buf.append( "\n    " );
                buf.append( level );
                buf.append( ": " );
                for (Integer id : curr) {
                    comps.add( compMap.get( id ));
                    buf.append( id );
                    buf.append( " " );
                }
                level++;
                Set<Integer> _sources = curr;
                curr = new TreeSet<Integer>();
                for (Integer sourceId : _sources) {
                    if (linkMap.containsKey( sourceId )) {
                        curr.addAll( linkMap.get( sourceId ));
                    }
                }
                curr.retainAll( heap );
                heap.removeAll( curr );
            } while (!curr.isEmpty());
        } while (!heap.isEmpty());
        log.fine( "Found " + level + " level(s) in the circuit" );
        log.finer( "Component IDs on each level: " + buf.toString());
    }
    
    @Override
    public void setInput( int index, RuntimeComponent comp, int reverseIndex ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RuntimeComponent getInput( int index ) {
        return null;
    }

    @Override
    public void setOutput( int index, RuntimeComponent comp, int reverseIndex ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RuntimeComponent getOutput( int index ) {
        return null;
    }

    @Override
    public void run() {
        for (RuntimeComponent r : comps) {
            if (!(r instanceof Runnable)) {
                continue;
            }
            try {
                ((Runnable)r).run();
            } catch (Throwable ex) {
                log.log( Level.SEVERE, "Cannot run a component", ex );
            }
        }
    }

    @Override // RunTimeControl interface
    public void setSettingEnabled( boolean settingEnabled ) {
        for (RuntimeComponent r : comps) {
            if (r instanceof RuntimeControl) {
                ((RuntimeControl)r).setSettingEnabled( settingEnabled );
            }
        }
    }

    protected Set<DaqChannel> getChannels() {
        Set<DaqChannel> res = new HashSet<DaqChannel>();
        for (RuntimeComponent c : comps) {
            if (c instanceof DaqChannel) {
                res.add( (DaqChannel)c );
            } else if (c instanceof DisplayComponent) {
                res.addAll( ((DisplayComponent)c).getChannels());
            }
        }
        return res;
    }

    @Override // SettingStateListener interface
    public void settingStateChanged( boolean settingEnabled ) {
        setSettingEnabled( settingEnabled );
    }

    @Override
    public void offer( TimedNumber data, int inputIndex ) {}

    @Override
    public String getDataTag( int outIndex ) {
        return null;
    }

    @Override
    public boolean doesSetting() {
        return false; // TODO;
    }

    @Override
    public void paint( Graphics g ) {
        if (isBackgroundSet()) {
            g.setColor( getBackground());
            g.fillRect( 0, 0, getWidth(), getHeight());
        }
        paintChildren( g );
    }

    @Override
    public void setOpaque( boolean opaque ) {}

}
