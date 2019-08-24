// (c) 2001-2010 Fermi Research Alliance
// $Id: SimulationComponent.java,v 1.3 2010/09/15 16:10:15 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element.variant;

import gov.fnal.controls.applications.syndi.builder.element.ComponentType;
import java.awt.Color;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:10:15 $
 */
public class SimulationComponent extends InvisibleComponent {

    private static final Color BACKGROUND_COLOR = new Color( 0xffcccc );
    
    public SimulationComponent() {
        super( BACKGROUND_COLOR );
    }

    @Override
    public ComponentType getType() {
        return ComponentType.DATA_CHANNEL;
    }

}
