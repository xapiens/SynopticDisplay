// (c) 2001-2010 Fermi Research Alliance
// $Id: Hyperlink.java,v 1.4 2010/09/20 21:54:39 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.applications.syndi.property.Alignment;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.applications.syndi.runtime.Viewer;
import gov.fnal.controls.tools.svg.SVGAnchor;
import gov.fnal.controls.tools.svg.SVGGraphics;
import gov.fnal.controls.applications.syndi.util.FormatConstants;
import gov.fnal.controls.applications.syndi.util.ImageFactory;
import gov.fnal.controls.tools.timed.TimedNumber;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 * A link to a synoptic display or a web page.
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/20 21:54:39 $
 */
@DisplayElement(

    name            = "Hyperlink",
    description     = "A link to a synoptic display or a web page",
    group           = "Controls",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.HyperlinkComponent",
    helpUrl         = "/Hyperlink",

    properties  = {
        @Property( caption="Width",              name="width",       value="100",       type=Integer.class                 ),
        @Property( caption="Height",             name="height",      value="20",        type=Integer.class                 ),
        @Property( caption="Border",             name="border",      value="gray",      type=Color.class,   required=false ),
        @Property( caption="Border Width",       name="borderWidth", value="1.0",       type=Double.class                  ),
        @Property( caption="Background",         name="background",  value="",          type=Color.class,   required=false ),
        @Property( caption="Text Color",         name="textColor",   value="purple",    type=Color.class                   ),
        @Property( caption="Font Size",          name="fontSize",    value="12",        type=Integer.class                 ),
        @Property( caption="Italic Font",        name="isItalicFont",value="false",     type=Boolean.class                 ),
        @Property( caption="Bold Font",          name="isBoldFont",  value="false",     type=Boolean.class                 ),
        @Property( caption="Alignment",          name="align",       value="CENTER",    type=Alignment.class               ),
        @Property( caption="Target URI",         name="target",      value="/Demo/Gauges", type=URI.class                  ),
        @Property( caption="Open In New Window", name="useNewWindow",value="false",     type=Boolean.class                 ),
        @Property( caption="Use Java WebStart",  name="useWebStart", value="false",     type=Boolean.class                 ),
        @Property( caption="Text",               name="text",        value="",                              required=false )
    },

    minInputs = 0,
    maxInputs = 0,
    minOutputs = 0,
    maxOutputs = 0

)

public class Hyperlink extends AbstractDisplay implements MouseListener {

    private static final Logger log = Logger.getLogger( Hyperlink.class.getName());

    private URI uri;
    private SVGAnchor link;
    private boolean useNewWindow, useWebStart, hover;

    public Hyperlink() {
        addMouseListener( this );
        setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ));
    }

    @Override
    public void init( PropertyCollection props ) throws Exception {

        super.init( props );

        String href = props.getValue( String.class, "target" );
        if (href != null) {
            try {
                uri = new URI( href );
            } catch (Exception ex) {
                log.log( Level.SEVERE, "Cannot create URI", ex );
            }
        }
        if (uri == null) {
            setText( FormatConstants.BAD_PREFIX + "Invalid Address" + FormatConstants.BAD_SUFFIX );
            return;
        }

        String text_ = props.getValue( String.class, "text" );
        if (props.getValue( Color.class, "textColor" ) == null)  {
            setText( null );
        } else if (text_ != null) {
            setText( text_ );
        } else {
            setText( uri.toString());
        }

        useNewWindow = props.getValue( Boolean.class, "useNewWindow", false );

        useWebStart = props.getValue( Boolean.class, "useWebStart", false );

        String title = "Open " + uri.toString();

        String uri_s = uri.toString();
        if (uri_s.toLowerCase().startsWith( "repo:" )) {
            uri_s = uri_s.substring( 5 );
        }
        String target;
        if (!useNewWindow) {
            target = "_top";
        } else if (uri_s.startsWith( "/" )) {
            target = uri_s.replace( '/', '_' );
        } else {
            target = "_blank";
        }
        link = new SVGAnchor( uri, target, title );

        if (bgImage == null) {
            bgImage = ImageFactory.getTransparentPixel();
        }

    }

    @Override
    protected void process( TimedNumber data ) {}

    @Override
    public void mouseClicked( MouseEvent e ) {
        if (link == null) {
            return;
        }
        if (useWebStart) {
            startJavaApp( uri );
        } else {
            openLink( uri, useNewWindow );
        }
    }

    @Override
    public void paint( Graphics g ) {
        if (g instanceof SVGGraphics) {
            ((SVGGraphics)g).setLink( link );
        }
        super.paint( g );
        if (g instanceof SVGGraphics) {
            ((SVGGraphics)g).setLink( null );
        }
    }

    @Override
    protected boolean paintBodrer( Graphics2D g2  ) {
        if (g2 instanceof SVGGraphics) {
            boolean painted = super.paintBodrer( g2 );
            if (painted) {
                ((SVGGraphics)g2).markHover();
            }
            return painted;
        } else if (hover) {
            boolean painted = super.paintBodrer( g2 );
            if (painted) {
                repaintEffectiveBounds();
            }
            return painted;
        } else {
            return false;
        }
    }

    @Override
    public void mousePressed( MouseEvent e ) {}

    @Override
    public void mouseReleased( MouseEvent e ) {}

    @Override
    public void mouseEntered( MouseEvent e ) {
        hover = true;
        repaint();
    }

    @Override
    public void mouseExited( MouseEvent e ) {
        hover = false;
        repaintEffectiveBounds();
    }

    private JFrame getOwningFrame() {
        Container cont = this;
        do {
            cont = cont.getParent();
        }  while (cont != null && !(cont instanceof JFrame));
        return (JFrame)cont;
    }

    private void openLink( URI uri, boolean useNewWindow ) {
        JFrame frame = getOwningFrame();
        if (frame instanceof Viewer) {
            ((Viewer)frame).openLink( uri, useNewWindow );
        } else {
            log.warning( "Owning frame not found" );
        }
    }

    private void startJavaApp( URI uri ) {
        String uri_str = uri.toString();
        String[] cmd = { "javaws", uri_str };
        try {
            Runtime.getRuntime().exec( cmd );
        } catch (Exception ex) {
            log.log(  Level.SEVERE, "Cannot start app \"" + uri_str + "\"", ex );
        }
    }

}
