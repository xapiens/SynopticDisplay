// (c) 2001-2010 Fermi Research Alliance
// $Id: DaqComponent.java,v 1.3 2010/09/15 16:10:15 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element.variant;

import gov.fnal.controls.applications.syndi.builder.element.ComponentType;
import gov.fnal.controls.applications.syndi.property.ComponentProperty;
import gov.fnal.controls.applications.syndi.property.PropertyException;
import java.awt.Color;
import java.util.Collection;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:10:15 $
 */
public class DaqComponent extends InvisibleComponent {

    private static final Color BACKGROUND_COLOR = new Color( 0xccccff );

    private String deviceName;
    
    public DaqComponent() {
        super( BACKGROUND_COLOR );
    }

    @Override
    public String getCaption() {
        return (deviceName != null) ? deviceName : getName();
    }
    
    @Override
    public ComponentType getType() {
        return ComponentType.DATA_CHANNEL;
    }

    @Override
    public void setProperties( Collection<ComponentProperty<?>> val ) throws PropertyException {
        super.setProperties( val );
        deviceName = props.getValue( String.class, "devName" );
        super.setProperties( val );
    }
    
}
