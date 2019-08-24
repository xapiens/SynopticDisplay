// (c) 2001-2010 Fermi Research Allaince
// $Id: RuntimeComponentSupport.java,v 1.3 2010/09/23 15:50:10 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime;

import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.tools.timed.TimedNumber;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/23 15:50:10 $
 */
public class RuntimeComponentSupport extends AbstractRuntimeComponent {
    
    public RuntimeComponentSupport() {}

    @Override
    protected void init( PropertyCollection props ) {}

    @Override
    public void offer( TimedNumber data, int inputIndex ) {}

}
