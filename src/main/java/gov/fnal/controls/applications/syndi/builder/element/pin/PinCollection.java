// (c) 2001-2010 Fermi Research Allaince
// $Id: PinCollection.java,v 1.2 2010/09/15 16:11:48 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element.pin;

import gov.fnal.controls.applications.syndi.builder.element.AbstractComponent;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:11:48 $
 */
public class PinCollection implements Iterable<Pin> {

    private static final Logger log = Logger.getLogger( PinCollection.class.getName());
    
    public static final int MAX_COUNT = 64;

    private final SortedMap<Integer,Pin> map = new TreeMap<Integer,Pin>();
    private final AbstractComponent owner;
    
    private int inpCnt = 0, minInpCnt = 0, maxInpCnt = MAX_COUNT;
    private int outCnt = 0, minOutCnt = 0, maxOutCnt = MAX_COUNT;

    public PinCollection( AbstractComponent owner ) {
        this.owner = owner;
    }

    @Override
    public Iterator<Pin> iterator() {
        return new PinIterator();
    }

    public Pin getPin( int index ) {
        return map.get( index );
    }

    public Pin addPin( Integer index, PinType type, String name ) throws IllegalStateException {
        if (type == null) {
            throw new NullPointerException();
        }
        if (index == null) {
            index = map.isEmpty() ? 0 : map.lastKey() + 1;
        } else if (map.containsKey( index )) {
            throw new IllegalStateException( "Duplicated pin index: " + index );
        }
        if (type == PinType.INPUT) {
            if (inpCnt >= maxInpCnt) {
                throw new IllegalStateException( "Too many inputs: " + (inpCnt + 1) );
            }
            inpCnt++;
        } else {
            if (outCnt >= maxOutCnt) {
                throw new IllegalStateException( "Too many outputs: " + (outCnt + 1) );
            }
            outCnt++;
        }
        Pin res = new Pin( owner, index, type, name );
        map.put( index, res );
        return res;
    }

    public int getInputCount() {
        return inpCnt;
    }

    public int getOutputCount() {
        return outCnt;
    }

    public void changePinCount( int inpCnt, int outCnt ) throws IllegalArgumentException {
        if (inpCnt < minInpCnt || inpCnt > maxInpCnt) {
            throw new IllegalArgumentException( "New input count is out of bounds, " +
                    "must be between " + minInpCnt + " and " + maxInpCnt + "." );
        }
        Collection<Pin> deadInp = null;
        if (inpCnt < this.inpCnt) {
            deadInp = getUnused( PinType.INPUT );
            if (deadInp.size() < (this.inpCnt - inpCnt)) {
                throw new IllegalArgumentException( "Cannot reduce the number of inputs" );
            }
        }
        if (outCnt < minOutCnt || outCnt > maxOutCnt) {
            throw new IllegalArgumentException( "New output count is out of bounds, " +
                    "must be between " + minOutCnt + " and " + maxOutCnt + "." );
        }
        Collection<Pin> deadOut = null;
        if (outCnt < this.outCnt) {
            deadOut = getUnused( PinType.OUTPUT );
            if (deadOut.size() < (this.outCnt - outCnt)) {
                throw new IllegalArgumentException( "Cannot reduce the number of outputs" );
            }
        }
        if (deadInp != null) {
            Iterator<Pin> z = deadInp.iterator();
            do {
                map.remove( z.next().getIndex());
            } while (--this.inpCnt > inpCnt);
        } else {
            while (this.inpCnt < inpCnt) {
                addPin( null, PinType.INPUT, null );
            }
            float dy = 1.0f / (this.inpCnt + 1);
            float y = dy;
            for (Pin p : map.values()) {
                if (p.getType() == PinType.INPUT) {
                    p.setLocation( new Point2D.Float( 0, y ));
                    y += dy;
                }
            }
        }
        if (deadOut != null) {
            Iterator<Pin> z = deadOut.iterator();
            do {
                map.remove( z.next().getIndex());
            } while (--this.outCnt > outCnt);
        } else {
            while (this.outCnt < outCnt) {
                addPin( null, PinType.OUTPUT, null );
            }
            float dy = 1.0f / (this.outCnt + 1);
            float y = dy;
            for (Pin p : map.values()) {
                if (p.getType() == PinType.OUTPUT) {
                    p.setLocation( new Point2D.Float( 1, y ));
                    y += dy;
                }
            }
        }
    }
    
    private Collection<Pin> getUnused( PinType type ) {
        Collection<Pin> res = new TreeSet<Pin>( Collections.reverseOrder());
        for (Pin p : map.values()) {
            if (p.getType() == type && !p.isInUse() && p.getName() == null) {
                res.add( p );
            }
        }
        return res;
    }

    public void setMinInputCount( int val ) throws IllegalArgumentException {
        if (val < 0) {
            val = 0;
        }
        if (val > inpCnt) {
            log.severe( "Actual inputs: " + inpCnt + "; new minimum: " + val );
            throw new IllegalArgumentException( "Invalid number of inputs" );
        }
        minInpCnt = val;
    }

    public int getMinInputCount() {
        return minInpCnt;
    }

    public void setMinOutputCount( int val ) throws IllegalArgumentException {
        if (val < 0) {
            val = 0;
        }
        if (val > outCnt) {
            log.severe( "Actual outputs: " + outCnt + "; new minimum: " + val );
            throw new IllegalArgumentException( "Invalid number of outputs" );
        }
        minOutCnt = val;
    }

    public int getMinOutputCount() {
        return minOutCnt;
    }

    public void setMaxInputCount( int val ) throws IllegalArgumentException {
        if (val < 0 || val > MAX_COUNT) {
            val = MAX_COUNT;
        }
        if (val < inpCnt) {
            log.severe( "Actual inputs: " + inpCnt + "; new maximum: " + val );
            throw new IllegalArgumentException( "Invalid number of inputs" );
        }
        maxInpCnt = val;
    }

    public int getMaxInputCount() {
        return maxInpCnt;
    }

    public void setMaxOutputCount( int val ) throws IllegalArgumentException {
        if (val < 0 || val > MAX_COUNT) {
            val = MAX_COUNT;
        }
        if (val < outCnt) {
            log.severe( "Actual outputs: " + outCnt + "; new maximum: " + val );
            throw new IllegalArgumentException( "Invalid number of outputs" );
        }
        maxOutCnt = val;
    }

    public int getMaxOutputCount() {
        return maxOutCnt;
    }
    
    private class PinIterator implements Iterator<Pin> {
        
        private final Iterator<Pin> z = map.values().iterator();

        @Override
        public boolean hasNext() {
            return z.hasNext();
        }

        @Override
        public Pin next() {
            return z.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    

}
