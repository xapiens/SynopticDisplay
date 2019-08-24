// (c) 2001-2010 Fermi Research Allaince
// $Id: Quarantine.java,v 1.2 2010/09/15 15:25:14 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:25:14 $
 */
public interface Quarantine {

    boolean isQuarantined( String dispName );

    void quarantine( String dispName );

}
