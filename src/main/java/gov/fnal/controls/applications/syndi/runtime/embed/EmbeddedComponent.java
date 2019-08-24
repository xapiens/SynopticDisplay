//  (c) 2001-2010 Fermi Research Alliance
//  $Id: EmbeddedComponent.java,v 1.2 2010/09/15 15:31:20 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.embed;

import gov.fnal.controls.applications.syndi.property.Alignment;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.applications.syndi.repository.DisplayURISource;
import gov.fnal.controls.applications.syndi.runtime.*;
import gov.fnal.controls.applications.syndi.runtime.v11n.VisualComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.net.URI;
import java.net.URL;
import org.w3c.dom.Element;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:31:20 $
 */
public abstract class EmbeddedComponent extends DisplayComponent {

    private Rectangle clip, border;
    private Color borderColor;
    private int borderWidth;
    private Stroke borderStroke;
    private DisplayURISource source;

    @Override
    public final void init( Element e ) throws Exception {

        PropertyCollection props = PropertyCollection.create( e );
        VisualComponent.handleStandartProps( props, this );

        borderColor = props.getValue( Color.class, "border", null );
        borderWidth = props.getValue( Double.class, "borderWidth", 0.0 ).intValue();
        borderStroke = (borderWidth > 0) ? new BasicStroke( (float)borderWidth ) : null;

        int width = props.getValue( Integer.class, "width", 0 );
        int height = props.getValue( Integer.class, "height", 0 );

        URI target = props.getValue( URI.class, "target" );
        if (target != null) {
            source = new DisplayURISource( target );
            checkLoop();
            super.init( source.loadDocument().getDocumentElement());
        } else {
            source = null;
            setSize( width, height );
        }

        Alignment align = props.getValue( Alignment.class, "align", Alignment.CENTER );

        int tx, ty;
        switch (align) {
            case NORTHWEST :
            case WEST :
            case SOUTHWEST :
                tx = 0;
                break;
            case NORTHEAST :
            case EAST :
            case SOUTHEAST :
                tx = getWidth() - width;
                break;
            default :
                tx = (getWidth() - width) / 2;
        }
        switch (align) {
            case NORTHWEST :
            case NORTH :
            case NORTHEAST :
                ty = 0;
                break;
            case SOUTHWEST :
            case SOUTH :
            case SOUTHEAST :
                ty = getHeight() - height;
                break;
            default :
                ty = (getHeight() - height) / 2;
        }

        setLocation( 
            props.getValue( Integer.class, "x", 0 ) - tx,
            props.getValue( Integer.class, "y", 0 ) - ty
        );

        clip = new Rectangle( tx, ty, width, height );
        border = new Rectangle(
                tx + borderWidth / 2,
                ty + borderWidth / 2,
                width - borderWidth,
                height - borderWidth
        );

        init( props );
    }

    private void checkLoop() throws DisplayFormatException {
        URL u0 = source.getLocation();
        Container c0 = getParent(), c1;
        while (c0 instanceof RuntimeComponent) {
            c1 = c0;
            c0 = c0.getParent();
            if (!(c1 instanceof EmbeddedComponent)) {
                continue;
            }
            DisplayURISource s1 = ((EmbeddedComponent)c1).source;
            if (s1 == null) {
                continue;
            }
            URL u1 = s1.getLocation();
            if (u0.equals( u1 )) {
                throw new DisplayFormatException( "Cyclic reference" );
            }
        }
    }

    @Override
    public void paint( Graphics g ) {

        Graphics2D g2 = (Graphics2D)g;

        Shape clip0 = g2.getClip();
        g2.clip( clip );

        super.paint( g2 );

        g2.setClip( clip0 );

        if (borderColor != null && borderStroke != null) {
            g2.setColor( borderColor );
            g2.setStroke( borderStroke );
            g2.draw( border );
        }

    }

    protected abstract void init( PropertyCollection props ) throws Exception;

}
