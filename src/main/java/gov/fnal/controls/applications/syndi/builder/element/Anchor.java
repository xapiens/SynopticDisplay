// (c) 2001-2010 Fermi Research Allaince
// $Id: Anchor.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import java.awt.Cursor;
import java.awt.geom.Point2D;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public enum Anchor {

    NORTHWEST   ( 0.0f, 0.0f, Cursor.getPredefinedCursor( Cursor.NW_RESIZE_CURSOR )),
    NORTH       ( 0.5f, 0.0f, Cursor.getPredefinedCursor( Cursor.N_RESIZE_CURSOR )),
    NORTHEAST   ( 1.0f, 0.0f, Cursor.getPredefinedCursor( Cursor.NE_RESIZE_CURSOR )),
    EAST        ( 1.0f, 0.5f, Cursor.getPredefinedCursor( Cursor.E_RESIZE_CURSOR )),
    SOUTHEAST   ( 1.0f, 1.0f, Cursor.getPredefinedCursor( Cursor.SE_RESIZE_CURSOR )),
    SOUTH       ( 0.5f, 1.0f, Cursor.getPredefinedCursor( Cursor.S_RESIZE_CURSOR )),
    SOUTHWEST   ( 0.0f, 1.0f, Cursor.getPredefinedCursor( Cursor.SW_RESIZE_CURSOR )),
    WEST        ( 0.0f, 0.5f, Cursor.getPredefinedCursor( Cursor.W_RESIZE_CURSOR ));

    private final float x, y;
    private final Cursor cursor;
    
    private Anchor( float x, float y, Cursor cursor ) {
        this.x = x;
        this.y = y;
        this.cursor = cursor;
    }
    
    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
    
    public Point2D getLocation() {
        return new Point2D.Float( x, y );
    }
    
    public Cursor getCursor() {
        return cursor;
    }

    public Anchor rotate( int quads ) {
        return values()[ (ordinal() + quads * 2) % 8 ];
    }

}
