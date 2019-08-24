//  (c) 2009 Fermi Research Alliance
//  $Id: SVGPolyline.java,v 1.1 2009/07/27 21:03:00 apetrov Exp $
package gov.fnal.controls.tools.svg;

import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2009/07/27 21:03:00 $
 */

@SVGTag( "polyline" )
public class SVGPolyline extends SVGAbstractPath {

    private static final Logger log = Logger.getLogger( SVGPolyline.class.getName());

    public SVGPolyline() {}

    @Override
    public void applyAttributes( Attributes attr ) {
        super.applyAttributes( attr );
        GeneralPath path_ = new GeneralPath();
        String pp = attr.getValue( "points" );
        if (pp != null) {
            try {
                int n = 0;
                for (StringTokenizer z = new StringTokenizer( pp, " ," ); z.hasMoreTokens();) {
                    float x = Float.parseFloat( z.nextToken());
                    if (!z.hasMoreTokens()) {
                        throw new IllegalArgumentException();
                    }
                    float y = Float.parseFloat( z.nextToken());
                    if (n++ == 0) {
                        path_.moveTo( x, y );
                    } else {
                        path_.lineTo( x, y );
                    }
                }
            } catch (IllegalArgumentException ex) {
                log.warning( "Illegal 'points' attribute: " + pp );
            }
        }
        setPath( path_ );
    }

    @Override
    public Element getXML( Document doc ) {
        Element res = super.getXML( doc );
        Path2D path = getPath();
        if (path != null) {
            StringBuilder buf = new StringBuilder();
            float[] seg = new float[ 6 ];
            for (PathIterator z = path.getPathIterator( null ); !z.isDone(); z.next()) {
                if (buf.length() > 0) {
                    buf.append( ' ' );
                }
                z.currentSegment( seg );
                buf.append( SVGNumber.toString( seg[ 0 ]));
                buf.append( ',' );
                buf.append( SVGNumber.toString( seg[ 1 ]));
            }
            res.setAttribute( "points", buf.toString());
        }
        return res;
    }

}
