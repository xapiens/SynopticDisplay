// (c) 2001-2010 Fermi Research Alliance
// $Id: HyperlinkComponent.java,v 1.2 2010/09/15 16:10:15 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element.variant;

import gov.fnal.controls.applications.syndi.property.ComponentProperty;
import gov.fnal.controls.applications.syndi.property.PropertyException;
import java.awt.Color;
import java.util.Collection;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:10:15 $
 */
public class HyperlinkComponent extends VisualComponent {

    private String caption;

    public HyperlinkComponent() {}

    @Override
    public String getCaption() {
        return caption;
    }

    @Override
    public void setProperties( Collection<ComponentProperty<?>> val ) throws PropertyException {
        super.setProperties( val );
        if (props.getValue( Color.class, "textColor" ) == null) {
            caption = null;
        } else {
            caption = props.getValue( String.class, "text" );
            if (caption == null) {
                caption = props.getValue( String.class, "target" );
            }
        }
    }

}
