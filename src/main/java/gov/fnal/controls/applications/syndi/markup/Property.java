// (c) 2001-2010 Fermi Research Alliance
// $Id: Property.java,v 1.2 2010/09/15 15:57:16 apetrov Exp $
package gov.fnal.controls.applications.syndi.markup;

import java.lang.annotation.Documented;

/**
 * Declaration of a runtime component's attribute. 
 * 
 * @author Timofei Bolshakov
 * @version $Date: 2010/09/15 15:57:16 $
 */
@Documented
public @interface Property {

    public String name();
    
    public Class<?> type() default String.class; 
            
    public String value();
    
    public String caption() default "";
    
    public boolean required() default true;

}
