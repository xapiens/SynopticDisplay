// (c) 2001-2010 Fermi Research Allaince
// $Id: DisplayFormatException.java,v 1.2 2010/09/15 15:25:14 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:25:14 $
 */
public class DisplayFormatException extends Exception {

    public DisplayFormatException() {
        super();
    }
    
    public DisplayFormatException( String message ) {
        super( message );
    }

    public DisplayFormatException( Throwable cause ) {
        super( cause );
    }

    public DisplayFormatException( String message, Throwable cause ) {
        super( message, cause );
    }
    
}
