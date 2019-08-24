// (c) 2001-2010 Fermi Research Alliance
// $Id: VisualComponent.java,v 1.2 2010/09/15 16:10:15 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element.variant;

import gov.fnal.controls.applications.syndi.builder.element.ComponentType;
import gov.fnal.controls.applications.syndi.builder.element.GenericComponent;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:10:15 $
 */
public class VisualComponent extends GenericComponent {
    
    public VisualComponent() {}

    @Override
    public ComponentType getType() {
        return ComponentType.VISUAL;
    }

}
