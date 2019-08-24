// (c) 2001-2010 Fermi Research Allaince
//  $Id: SVGUtil.java,v 1.2 2010/09/15 15:19:09 apetrov Exp $
package gov.fnal.controls.applications.syndi.util;

import gov.fnal.controls.applications.syndi.builder.element.AbstractSVGComponent;
import gov.fnal.controls.applications.syndi.builder.element.BuilderComponent;
import gov.fnal.controls.applications.syndi.builder.element.GenericContainer;
import gov.fnal.controls.tools.svg.SVGComponent;
import gov.fnal.controls.tools.svg.SVGContainer;
import gov.fnal.controls.tools.svg.SVGSvg;
import gov.fnal.controls.tools.svg.SVGText;
import java.awt.Rectangle;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:19:09 $
 */
public class SVGUtil {

    private static final Logger log = Logger.getLogger( SVGUtil.class.getName());
    
    private SVGUtil() {}

    public static int forceMonospaceFonts( SVGComponent comp ) {

        if (comp instanceof SVGText) {
            comp.setFontFamily( "monospace" );
            return 1;
        }

        comp.setFontFamily( null );

        if (comp instanceof SVGContainer) {
            int cnt = 0;
            SVGContainer cont = (SVGContainer)comp;
            for (SVGComponent c : cont) {
                cnt += forceMonospaceFonts( c );
            }
            return cnt;
        }

        return 0;
        
    }

    public static int adjustOrigin( SVGSvg svg ) {

        Rectangle2D r2 = svg.getBounds();
        if (r2 == null) {
            return 0;
        }
        Rectangle bounds = r2.getBounds();

        int dx;
        if (bounds.x > 0) {
            dx = -bounds.x;
        } else if (bounds.x + bounds.width < 0) {
            dx = -(bounds.x + bounds.width);
        } else {
            dx = 0;
        }

        int dy;
        if (bounds.y > 0) {
            dy = -bounds.y;
        } else if (bounds.y + bounds.height < 0) {
            dy = -(bounds.y + bounds.height);
        } else {
            dy = 0;
        }

        if (dx == 0 && dy == 0) {
            return 0;
        }

        bounds.x += dx;
        bounds.y += dy;

        try {

            svg.setBounds( bounds );

            int x = (svg.getX() == null) ? 0 : Math.round( svg.getX().floatValue());
            int y = (svg.getY() == null) ? 0 : Math.round( svg.getY().floatValue());

            svg.setX( x - dx );
            svg.setY( y - dy );
            
            return 1;

        } catch (NoninvertibleTransformException ex) {
            log.log( Level.WARNING, "Cannot adjust origin", ex );
        }

        return 0;
        
    }

    public static int removeInvisibleImages( GenericContainer cont ) {

        Rectangle limits = new Rectangle( 0, 0, cont.getWidth(), cont.getHeight());
        List<BuilderComponent> dead = new ArrayList<BuilderComponent>();
        for (BuilderComponent comp : cont) {
            if (!isVisible( comp, limits )) {
                dead.add( comp );
            }
        }

        for (BuilderComponent comp : dead) {
            cont.remove( comp );
        }

        return dead.size();

    }

    private static boolean isVisible( BuilderComponent comp, Rectangle limits ) {
        if (comp instanceof AbstractSVGComponent) {
            return isVisible( (AbstractSVGComponent)comp, limits );
        } else {
            return true;
        }
    }

    private static boolean isVisible( AbstractSVGComponent comp, Rectangle limits ) {
        Rectangle bounds = comp.getBounds();
        if (bounds.width < 1) {
            bounds.width = 1;
        }
        if (bounds.height < 1) {
            bounds.height = 1;
        }
        return limits.intersects( bounds );
    }

}
