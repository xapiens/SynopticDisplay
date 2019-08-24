// (c) 2001-2010 Fermi Research Alliance
// $Id: NameChecker.java,v 1.2 2010/09/15 15:53:52 apetrov Exp $
package gov.fnal.controls.applications.syndi.repository;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:53:52 $
 */
public class NameChecker implements DisplayAddressSyntax {
    
    public static boolean isValidName( String name ) {
        if (name == null) {
            return false;
        }
        return NAME_PATTERN.matcher( name ).matches();
    }

    private NameChecker() {}

}
