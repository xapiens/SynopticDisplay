//  (c) 2009 Fermi Research Alliance
//  $Id: SVGTag.java,v 1.1 2009/07/27 21:03:00 apetrov Exp $
package gov.fnal.controls.tools.svg;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2009/07/27 21:03:00 $
 */

@Documented
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
public @interface SVGTag {

    public String value();

}
