// (c) 2001-2010 Fermi Research Alliance
// $Id: Pin.java,v 1.2 2010/09/15 15:57:16 apetrov Exp $
package gov.fnal.controls.applications.syndi.markup;

import java.lang.annotation.Documented;

/**
 * Declaration of a runtime component's input pin. 
 * 
 * @author Timofei Bolshakov
 * @version $Date: 2010/09/15 15:57:16 $
 */
@Documented
public @interface Pin {

    public String name() default "";

    public int number();
    
    public double x();

    public double y();
    
}
