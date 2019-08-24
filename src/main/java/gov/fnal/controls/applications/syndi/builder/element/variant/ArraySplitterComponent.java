// (c) 2001-2010 Fermi Research Alliance
// $Id: ArraySplitterComponent.java,v 1.2 2010/09/15 16:10:15 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element.variant;

import gov.fnal.controls.applications.syndi.builder.element.pin.Pin;
import gov.fnal.controls.applications.syndi.builder.element.pin.PinType;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:10:15 $
 */
public class ArraySplitterComponent extends PipeComponent {
    
    public ArraySplitterComponent() {}

    @Override
    protected String getPinCaption( Pin pin ) {
        if (pin.getType() == PinType.INPUT) {
            return null;
        }
        return String.valueOf( pin.getIndex());
    }

}
