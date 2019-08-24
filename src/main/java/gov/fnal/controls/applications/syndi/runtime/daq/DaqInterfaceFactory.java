// (c) 2001-2010 Fermi Research Allaince
// $Id: DaqInterfaceFactory.java,v 1.3 2010/09/23 15:49:01 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.daq;

/**
 * A factory for creating custom data acquisition interfaces.
 *
 * @author  Andrey Petrov
 * @version $Date: 2010/09/23 15:49:01 $
 */
public abstract class DaqInterfaceFactory {

    private static DaqInterfaceFactory sharedInstance;

    private static DaqInterfaceFactory create() {
        String className = System.getProperty( "Synoptic.daq" );
        if (className == null) {
            throw new RuntimeException(
                "Implementation of the data acquisition interface factory is not specified."
            );
        }
        try {
            Class<?> clazz = Class.forName( className );
            return (DaqInterfaceFactory)clazz.newInstance();
        } catch (Throwable ex) {
            throw new Error( "Cannot create data acquisition interface factory.", ex );
        }
    }

    /**
     * Returns a shared instance of the factory.
     * <p>
     * The factory is instantiated via reflections, using a class name from
     * <code>Synoptic.daq</code> system property.
     *
     * @return a shared instance of the factory.
     */
    public static synchronized DaqInterfaceFactory getSharedInstance() {
        if (sharedInstance == null) {
            sharedInstance = create();
        }
        return sharedInstance;
    }

    /**
     * Destroys the shared instance of the factory and all associated data acquisition interfaces.
     */
    public static synchronized void shutOffSharedInstance() {
        if (sharedInstance == null) {
            return;
        }
        sharedInstance.shutOff();
        sharedInstance = null;
    }

    protected DaqInterfaceFactory() {}

    /**
     * Creates a new instance of the data acquisition interface.
     *
     * @return a DAQ interface interface.
     * @throws Exception is thrown is the DAQ instance cannot be created.
     */
    public abstract DaqInterface createDaqInterface() throws Exception;

    /**
     * Destroys the factory and releases all associated data acquisition interfaces.
     * <p>
     * This method should never throw exceptions.
     */
    protected abstract void shutOff();

}
