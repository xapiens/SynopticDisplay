// (c) 2001-2010 Fermi Research Alliance
// $Id: DisplayElement.java,v 1.2 2010/09/15 15:57:16 apetrov Exp $
package gov.fnal.controls.applications.syndi.markup;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declaration of a runtime component. 
 * 
 * @author Timofei Bolshakov
 * @version $Date: 2010/09/15 15:57:16 $
 */
@Documented
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
public @interface DisplayElement {

    public String name();

    public String group() default "";

    public String description() default "";

    public String designTimeView() default "";

    public String icon() default "";

    public String helpUrl() default "";
            
    public Property[] properties() default {};

    public Pin[] outputs() default {};
            
    public Pin[] inputs() default {};

    public int minInputs();

    public int maxInputs();

    public int minOutputs();

    public int maxOutputs();

}
