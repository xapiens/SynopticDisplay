// (c) 2001-2010 Fermi Research Alliance
// $Id: AbstractDisplay.java,v 1.2 2010/09/15 15:48:22 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

import gov.fnal.controls.applications.syndi.property.Alignment;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.tools.svg.SVGGraphics;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:48:22 $
 */
public abstract class AbstractDisplay extends VisualComponent {

    private static final int TEXT_MARGIN = 1;

    private final Alignment defaultAlign;
    
    protected String text, message;
    protected Alignment align;
    protected Color borderColor;
    protected Stroke borderStroke;
    protected int borderWidth;

    private String[] lines;
    private Rectangle bounds;
    private FontMetrics fontMetrics;
    
    protected AbstractDisplay() {
        this( Alignment.CENTER );
    }

    protected AbstractDisplay( Alignment defaultAlign ) {
        this.defaultAlign = defaultAlign;
    }

    @Override
    public void init( PropertyCollection props ) throws Exception {
        align = props.getValue( Alignment.class, "align", defaultAlign );
        borderColor = props.getValue( Color.class, "border" );
        borderWidth = props.getValue( Double.class, "borderWidth", 0.0 ).intValue();
        borderStroke = (borderWidth > 0) ? new BasicStroke( (float)borderWidth ) : null;
        fontMetrics = getFontMetrics( getFont());
    }

    protected void setText( String text ) {

        if ((text == null) ? this.text == null : text.equals( this.text )) {
            return;
        }

        this.text = text;
        lines = createLines( text, getDataTag( 0 ));

        Rectangle oldBounds = getEffectiveBounds();
        Rectangle newBounds = getBounds( lines );
        this.bounds = newBounds;

        Container parent = getParent();
        if (parent == null) {
            return;
        }

        Rectangle dirty = new Rectangle();
        Rectangle.union( oldBounds, newBounds, dirty );

        parent.repaint(
            dirty.x + getX(),
            dirty.y + getY(),
            dirty.width,
            dirty.height
        );

    }

    private Rectangle getEffectiveBounds() {
        if (bounds == null) {
            bounds = new Rectangle( getSize());
        }
        return bounds;
    }

    protected void repaintEffectiveBounds() {
        Container parent = getParent();
        if (parent == null) {
            return;
        }
        Rectangle r = getEffectiveBounds();
        parent.repaint( r.x + getX(), r.y + getY(), r.width, r.height );
    }

    protected void setMessage( String message ) {
        if ((message == null) ? this.message == null : message.equals( this.message )) {
            return;
        }
        this.message = message;
        setToolTipText( message );
    }

    @Override
    public void paint( Graphics g ) {

        Graphics2D g2 = (Graphics2D)g;

        if (g instanceof SVGGraphics) {
            ((SVGGraphics)g).setToolTip( message );
        }

        g2.setFont( getFont());
        g2.setClip( null );

        Rectangle r = getEffectiveBounds();

        if (isBackgroundSet()) {
            g2.setColor( getBackground());
            g2.fill( r );
        }

        if (bgImage != null) {
            g2.drawImage( bgImage, r.x, r.y, r.width, r.height, null, null );
        }

        paintBodrer( g2 );

        if (lines != null) {

            g2.setColor( getForeground());

            int y;
            int h = fontMetrics.getHeight();
            switch (align) {
                case NORTHWEST :
                case NORTH :
                case NORTHEAST :
                    y = r.y + borderWidth + TEXT_MARGIN + h;
                    break;
                case SOUTHWEST :
                case SOUTH :
                case SOUTHEAST :
                    y = r.y + r.height - borderWidth - TEXT_MARGIN - h * (lines.length - 1);
                    break;
                default :
                    y = r.y + (r.height - h * lines.length) / 2 + h;
            }
            y -= fontMetrics.getDescent();

            for (String str : lines) {
                int x;
                int w = fontMetrics.stringWidth( str );
                switch (align) {
                    case NORTHWEST :
                    case WEST :
                    case SOUTHWEST :
                        x = r.x + borderWidth + TEXT_MARGIN;
                        break;
                    case NORTHEAST :
                    case EAST :
                    case SOUTHEAST :
                        x = r.x + r.width - borderWidth - TEXT_MARGIN - w;
                        break;
                    default :
                        x = r.x + (r.width - w) / 2;
                }
                g.drawString( str, x, y );
                y += h;
            }

        }

        if (g instanceof SVGGraphics) {
            ((SVGGraphics)g).setToolTip( null );
        }
        
    }

    protected boolean paintBodrer( Graphics2D g2 ) {
        if (borderColor == null || borderStroke == null) {
            return false;
        }
        g2.setColor( borderColor );
        g2.setStroke( borderStroke );

        Rectangle r = getEffectiveBounds();
        g2.drawRect(
            r.x + borderWidth / 2,
            r.y + borderWidth / 2,
            r.width - borderWidth,
            r.height - borderWidth
        );

        return true;
    }

    private String[] createLines( String text, String dataTag ) {
        if (text == null) {
            return new String[ 0 ];
        }
        if (dataTag == null) {
            dataTag = "";
        }
        return text.replace( "\\$", dataTag ).split( "\\s*\\\\n\\s*" );
    }
    
    private Rectangle getBounds( String[] lines ) {
        
        // Component's default dimension
        int defHeight = getHeight();
        int defWidth = getWidth();

        int height = fontMetrics.getHeight() * lines.length + 2 * (borderWidth + TEXT_MARGIN);
        if (height < defHeight) {
            height = defHeight;
        }
        
        int width = 0;
        for (String s : lines) {
            int w = fontMetrics.stringWidth( s );
            if (w > width) {
                width = w;
            }
        }
        width += 2 * (borderWidth + TEXT_MARGIN);
        if (width < defWidth) {
            width = defWidth;
        }

        int x;
        switch (align) {
            case NORTHWEST :
            case WEST :
            case SOUTHWEST :
                x = 0;
                break;
            case NORTHEAST :
            case EAST :
            case SOUTHEAST :
                x = defWidth - width;
                break;
            default :
                x = (defWidth - width) / 2;
        }

        int y;
        switch (align) {
            case NORTHWEST :
            case NORTH :
            case NORTHEAST :
                y = 0;
                break;
            case SOUTHWEST :
            case SOUTH :
            case SOUTHEAST :
                y = defHeight - height;
                break;
            default :
                y = (defHeight - height) / 2;
        }

        return new Rectangle( x, y, width, height );
        
    }

}
