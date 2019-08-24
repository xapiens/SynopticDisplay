//  (c) 2009 Fermi Research Alliance
//  $Id: SVGMetaElement.java,v 1.1 2009/07/27 21:03:01 apetrov Exp $
package gov.fnal.controls.tools.svg;

import org.xml.sax.Attributes;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2009/07/27 21:03:01 $
 */

public abstract class SVGMetaElement implements SVGElement {

    private String text;

    protected SVGMetaElement() {}

    @Override
    public boolean add( SVGElement child ) {
        return false;
    }

    @Override
    public void applyAttributes( Attributes attr ) {}

    @Override
    public void setText( String text ) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

}
