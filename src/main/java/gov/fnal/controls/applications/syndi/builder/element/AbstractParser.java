// (c) 2001-2010 Fermi Research Allaince
// $Id: AbstractParser.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public abstract class AbstractParser<T> extends DefaultHandler {
    
    protected boolean done = false;
    protected T result = null;
    
    protected AbstractParser() {}

    public boolean isDone() {
        return done;
    }
    
    public T getResult() {
        if (!done) {
            throw new IllegalStateException( "Parsing not completed" );
        }
        return result;
    }

    public abstract int getOriginAdjustmentCount();

    public abstract int getRemovedInvisibleCount();

}
