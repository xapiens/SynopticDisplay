//  (c) 2010 Fermi Research Alliance
//  $Id: AbstractResourceIterator.java,v 1.2 2010/08/30 16:05:08 apetrov Exp $
package gov.fnal.controls.tools.resource;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/08/30 16:05:08 $
 */

public abstract class AbstractResourceIterator<T> implements Iterator<T> {

    private T next;

    protected AbstractResourceIterator() {}

    @Override
    public final boolean hasNext() {
        return (next != null);
    }

    @Override
    public final T next() throws NoSuchElementException {
        if (next == null) {
            throw new NoSuchElementException();
        }
        T res = next;
        next = fetch();
        return res;
    }

    @Override
    public final void remove() {
        throw new UnsupportedOperationException();
    }

    protected void prefetch() {
        next = fetch();
    }

    protected abstract T fetch();

}
