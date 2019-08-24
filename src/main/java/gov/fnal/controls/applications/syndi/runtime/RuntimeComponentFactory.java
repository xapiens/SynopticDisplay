// (c) 2001-2010 Fermi Research Allaince
// $Id: RuntimeComponentFactory.java,v 1.4 2010/09/15 15:25:14 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime;

import gov.fnal.controls.applications.syndi.SynopticConfig;
import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:25:14 $
 */
public class RuntimeComponentFactory {

    public static final String DEFAULT_COMPONENT =
            "gov.fnal.controls.applications.syndi.runtime.DisplayComponent";

    private static final Logger log = Logger.getLogger( RuntimeComponentFactory.class.getName());
    
    public static RuntimeComponent createComponent( String runtimeClass )
            throws DisplayFormatException {
        String classTranslated = translateClass( runtimeClass );
        try {
            Class<?> clazz = Class.forName( classTranslated );
            return (RuntimeComponent)clazz.newInstance();
        } catch (ClassNotFoundException ex) {
            throw new DisplayFormatException( "Unknown component: " + classTranslated, ex );
        } catch (ClassCastException ex) {
            throw new DisplayFormatException( "Invalid component: " + classTranslated, ex );
        } catch (Throwable ex) {
            throw new DisplayFormatException( "Cannot create component " + classTranslated, ex );
        }
    }

    public static DisplayElement getComponentInfo( String runtimeClass ) {
        String classTranslated = translateClass( runtimeClass );
        try {
            Class<?> clazz = Class.forName( classTranslated );
            return clazz.getAnnotation( DisplayElement.class );
        } catch (Throwable ex) {
            log.log( Level.FINE, "Cannot create class " + classTranslated, ex );
        }
        return null;
    }

    private static String translateClass( String runtimeClass ) {
        if (runtimeClass == null || runtimeClass.isEmpty()) {
            return DEFAULT_COMPONENT;
        }
        String classOverride = SynopticConfig.getComponentMap().get( runtimeClass );
        if (classOverride != null) {
            return classOverride;
        } else {
            return runtimeClass;
        }
    }

    private RuntimeComponentFactory() {}

}
