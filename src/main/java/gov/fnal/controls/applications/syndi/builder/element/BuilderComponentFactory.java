// (c) 2001-2010 Fermi Research Allaince
// $Id: BuilderComponentFactory.java,v 1.4 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import gov.fnal.controls.applications.syndi.SynopticConfig;
import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.runtime.DisplayFormatException;
import gov.fnal.controls.applications.syndi.runtime.RuntimeComponentFactory;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public class BuilderComponentFactory {

    public static final String DEFAULT_COMPONENT =
            "gov.fnal.controls.applications.syndi.builder.element.GenericComponent";

    public static BuilderComponent createComponent(
            Integer id,
            String name,
            String builderClass,
            String runtimeClass ) throws DisplayFormatException {
        DisplayElement info = RuntimeComponentFactory.getComponentInfo( runtimeClass );
        if (info != null) {
            if (info.designTimeView() != null) {
                builderClass = info.designTimeView();
            }
            if (info.name() != null) {
                name = info.name();
            }
        }
        String classTranslated = translateClass( builderClass );
        try {
            Class<?> clazz = Class.forName( classTranslated );
            BuilderComponent comp = (BuilderComponent)clazz.newInstance();
            if (comp instanceof AbstractComponent) {
                AbstractComponent acomp = (AbstractComponent)comp;
                if (id != null) {
                    acomp.setId( id );
                }
                if (name != null) {
                    acomp.setName( name );
                } else {
                    acomp.setName( clazz.getSimpleName());
                }
                if (runtimeClass != null) {
                    acomp.setRuntimeClass( runtimeClass );
                }
            }
            return comp;
        } catch (ClassNotFoundException ex) {
            throw new DisplayFormatException( "Unknown component: " + builderClass, ex );
        } catch (ClassCastException ex) {
            throw new DisplayFormatException( "Invalid component: " + builderClass, ex );
        } catch (Throwable ex) {
            throw new DisplayFormatException( "Cannot create component: " + builderClass, ex );
        }
    }

    public static String translateClass( String builderClass ) {
        if (builderClass == null || builderClass.isEmpty()) {
            return DEFAULT_COMPONENT;
        }
        String classOverride = SynopticConfig.getComponentMap().get( builderClass );
        if (classOverride != null) {
            return classOverride;
        } else {
            return builderClass;
        }
    }

    private BuilderComponentFactory() {}

}
