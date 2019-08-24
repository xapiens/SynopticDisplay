// (c) 2001-2010 Fermi Research Allaince
// $Id: PinAddress.java,v 1.2 2010/09/15 16:11:48 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element.pin;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:11:48 $
 */
public class PinAddress {
    
    private final int componentId, pinId;
    
    public PinAddress( int componentId, int pinId ) {
        this.componentId = componentId;
        this.pinId = pinId;
    }
    
    public int getComponentId() {
        return componentId;
    }
    
    public int getPinId() {
        return pinId;
    }

    @Override
    public int hashCode() {
        return componentId ^ pinId;
    }

    @Override
    public boolean equals( Object obj ) {
        return obj instanceof PinAddress
                && ((PinAddress)obj).componentId == componentId
                && ((PinAddress)obj).pinId == pinId;
    }

    @Override
    public String toString() {
        return "(" + componentId + ":" + pinId + ")";
    }

}
