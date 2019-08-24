//  (c) 2010 Fermi Research Alliance
//  $Id: SVGPath.java,v 1.4 2010/09/02 21:08:30 apetrov Exp $
package gov.fnal.controls.tools.svg;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/02 21:08:30 $
 */

@SVGTag( "path" )
public class SVGPath extends SVGAbstractPath {

    private static final Logger log = Logger.getLogger( SVGPath.class.getName());

    public SVGPath() {}

    @Override
    public void applyAttributes( Attributes attr ) {
        super.applyAttributes( attr );
        String d = attr.getValue( "d" );
        try {
            setPathString( d );
        } catch (IllegalArgumentException ex) {
            log.warning( "Illegal 'd' attribute: " + d );
        }
    }

    public void setPathString( String pathString ) {
        GeneralPath newPath = new GeneralPath();
        if (pathString != null) {
            newPath.append( new SVGPathIterator( pathString ), true );
        }
        setPath( newPath );
    }

    @Override
    public String toString() {
        return toString( getPath());
    }

    public static String toString( Shape path ) {
        return toString( path, null );
    }

    public static String toString( Shape path, AffineTransform xform ) {
        if (path == null) {
            return "";
        }
        StringBuilder buf = new StringBuilder();
        float[] seg = new float[ 6 ];
        for (PathIterator z = path.getPathIterator( xform ); !z.isDone(); z.next()) {
            switch (z.currentSegment( seg )) {
                case PathIterator.SEG_MOVETO:
                    buf.append( "M" );
                    appendSegment( buf, seg, 2 );
                    break;
                case PathIterator.SEG_LINETO:
                    buf.append( "L" );
                    appendSegment( buf, seg, 2 );
                    break;
                case PathIterator.SEG_QUADTO:
                    buf.append( "Q" );
                    appendSegment( buf, seg, 4 );
                    break;
                case PathIterator.SEG_CUBICTO:
                    buf.append( "C" );
                    appendSegment( buf, seg, 6 );
                    break;
                case PathIterator.SEG_CLOSE:
                    buf.append( "Z" );
                    break;
            }
        }
        return buf.toString();
    }

    public static Path2D toPath( String pathString ) {
        return toPath( pathString, null );
    }

    public static Path2D toPath( String pathString, AffineTransform xform ) {
        if (pathString == null) {
            throw new NullPointerException();
        }
        Path2D path = new GeneralPath();
        path.append( new SVGPathIterator( pathString ), true );
        if (xform != null) {
            path.transform( xform );
        }
        return path;
    }

    public static Point2D getFirstPoint( Path2D path ) {
        if (path == null) {
            throw new NullPointerException();
        }
        PathIterator z = path.getPathIterator( null );
        if (!z.isDone()) {
            float[] seg = new float[ 6 ];
            if (z.currentSegment( seg ) == PathIterator.SEG_MOVETO) {
                return new Point2D.Float( seg[ 0 ], seg[ 1 ]);
            }
        }
        return new Point( 0, 0 );
    }

    private static void appendSegment( StringBuilder buf, float[] seg, int count ) {
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                buf.append( ' ' );
            }
            buf.append( SVGNumber.toString( seg[ i ]));
        }
    }

    @Override
    public Element getXML( Document doc ) {
        Element res = super.getXML( doc );
        String d = toString();
        if (d != null) {
            res.setAttribute( "d", d );
        }
        return res;
    }

}
