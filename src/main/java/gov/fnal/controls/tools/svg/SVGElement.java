//  (c) 2009 Fermi Research Alliance
//  $Id: SVGElement.java,v 1.1 2009/07/27 21:03:00 apetrov Exp $
package gov.fnal.controls.tools.svg;

import org.xml.sax.Attributes;

/**
 * A fundamental SVG element, either visual or containing some meta information.
 *
 * @author Andrey Petrov
 */

public interface SVGElement {

    boolean add( SVGElement child );

    void setText( String text );

    void applyAttributes( Attributes attr );

}
