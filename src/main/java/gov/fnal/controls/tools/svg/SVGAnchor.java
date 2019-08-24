//  (c) 2009 Fermi Research Alliance
//  $Id: SVGAnchor.java,v 1.2 2009/12/10 21:43:51 apetrov Exp $
package gov.fnal.controls.tools.svg;

import java.net.URI;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2009/12/10 21:43:51 $
 */

public class SVGAnchor {

    private final URI uri;
    private final String target;
    private final String title;

    public SVGAnchor( URI uri, String target, String title ) {
        if (uri == null || target == null) {
            throw new NullPointerException();
        }
        this.uri = uri;
        this.target = target;
        this.title = title;
    }

    public URI getURI() {
        return uri;
    }

    public String getTarget() {
        return target;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public int hashCode() {
        return uri.hashCode() ^ target.hashCode();
    }

    @Override
    public boolean equals( Object obj ) {
        return (obj instanceof SVGAnchor)
                && ((SVGAnchor)obj).uri.equals( uri )
                && ((SVGAnchor)obj).target.equals( target );
    }

}
