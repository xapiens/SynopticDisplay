//  (c) 2001-2010 Fermi Research Alliance
//  $Id: MirrorInterfaceFactory.java,v 1.3 2010/09/23 15:50:45 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.daq.mirror;

import gov.fnal.controls.applications.syndi.runtime.daq.DaqInterfaceFactory;
import java.util.logging.Logger;

/**
 *
 * @author  Andrey Petrov
 * @version $Date: 2010/09/23 15:50:45 $
 */
public class MirrorInterfaceFactory extends DaqInterfaceFactory {
    
    private static final Logger log = Logger.getLogger( MirrorInterfaceFactory.class.getName());

    public MirrorInterfaceFactory() {
        log.info( "Created " + this );
    }

    @Override
    public MirrorInterface createDaqInterface() throws Exception {
        return new MirrorInterface();
    }

    @Override
    protected void shutOff() {
        log.info( "Shut off " + this );
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
