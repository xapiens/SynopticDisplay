// (c) 2001-2010 Fermi Research Alliance
// $Id: RepositoryAccess.java,v 1.2 2010/09/15 15:53:52 apetrov Exp $
package gov.fnal.controls.applications.syndi.repository;

import gov.fnal.controls.applications.syndi.runtime.TimedDocument;
import java.io.IOException;

/**
 * Abstract implementation of a display repository.
 *  
 * @author  Andrey Petrov
 * @version $Revision: 1.2 $
 */
public interface RepositoryAccess {
    
    boolean isDisplay( String path ) throws IOException;

    boolean isDirectory( String path ) throws IOException;
    
    TimedDocument load( String path ) throws IOException;

    TimedDocument load( String path, long since ) throws IOException;

}
