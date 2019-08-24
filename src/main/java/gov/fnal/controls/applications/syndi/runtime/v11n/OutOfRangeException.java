// (c) 2001-2010 Fermi Research Alliance
// $Id: OutOfRangeException.java,v 1.2 2010/09/15 15:48:22 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:48:22 $
 */
public class OutOfRangeException extends IllegalArgumentException {

    public OutOfRangeException() {
    }

    public OutOfRangeException( String message ) {
        super( message );
    }

    public OutOfRangeException( Throwable cause ) {
        super( cause );
    }

    public OutOfRangeException( String message, Throwable cause ) {
        super( message, cause );
    }

}
