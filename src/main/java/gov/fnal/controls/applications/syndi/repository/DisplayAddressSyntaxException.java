// (c) 2001-2010 Fermi Research Alliance
// $Id: DisplayAddressSyntaxException.java,v 1.2 2010/09/15 15:53:52 apetrov Exp $
package gov.fnal.controls.applications.syndi.repository;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:53:52 $
 */
public class DisplayAddressSyntaxException extends IllegalArgumentException {

    public DisplayAddressSyntaxException() {
        super();
    }

    public DisplayAddressSyntaxException( String message ) {
        super( message );
    }

    public DisplayAddressSyntaxException( Throwable cause ) {
        super( cause );
    }

    public DisplayAddressSyntaxException( String message, Throwable cause ) {
        super( message, cause );
    }

}
