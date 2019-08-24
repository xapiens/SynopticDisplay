//  (c) 2001-2010 Fermi Research Alliance
//  $Id: PropertyException.java,v 1.2 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public class PropertyException extends Exception {

    public PropertyException() {
        super();
    }

    public PropertyException( String message ) {
        super( message );
    }

    public PropertyException( Throwable cause ) {
        super( cause );
    }

    public PropertyException( String message, Throwable cause ) {
        super( message, cause );
    }

}
