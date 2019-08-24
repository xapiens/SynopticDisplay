// (c) 2001-2010 Fermi Research Alliance
// $Id: PipeComponent.java,v 1.3 2010/09/15 16:10:15 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element.variant;

import gov.fnal.controls.applications.syndi.builder.element.ComponentType;
import java.awt.Color;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:10:15 $
 */
public class PipeComponent extends InvisibleComponent {

    private static final Color BACKGROUND_COLOR = new Color( 0xccffcc );

    public PipeComponent() {
        super( BACKGROUND_COLOR );
    }

    @Override
    public ComponentType getType() {
        return ComponentType.DATA_PIPE;
    }

}
