// (c) 2001-2010 Fermi Research Allaince
//  $Id: FrameSaver.java,v 1.2 2010/09/15 15:19:09 apetrov Exp $
package gov.fnal.controls.applications.syndi.util;

import java.awt.Dimension;
import java.util.prefs.Preferences;
import javax.swing.JFrame;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:19:09 $
 */
public class FrameSaver {

    private static final Dimension DEFAULT_SIZE = new Dimension( 850, 600 );
    private static final Dimension MIN_SIZE = new Dimension( 300, 200 );

    private final JFrame frame;
    private final String prefix;
    
    public FrameSaver( JFrame frame ) {
        if (frame == null) {
            throw new NullPointerException();
        }
        this.frame = frame;
        this.prefix = frame.getClass().getSimpleName() + ".frame.";
    }

    public void save( Preferences prefs ) {

        prefs.putInt( prefix + "x", frame.getX());
        prefs.putInt( prefix + "y", frame.getY());
        prefs.putInt( prefix + "width", frame.getWidth());
        prefs.putInt( prefix + "height", frame.getHeight());
        
        // Removing obsolete properties
        prefs.remove( "frame-x" );
        prefs.remove( "frame-y" );
        prefs.remove( "frame-width" );
        prefs.remove( "frame-height" );

    }

    public void restore( Preferences prefs ) {
        
        Dimension screen = frame.getToolkit().getScreenSize();

        int width = prefs.getInt( prefix + "width", DEFAULT_SIZE.width );
        int height = prefs.getInt( prefix + "height", DEFAULT_SIZE.height );

        if (width > screen.width) {
            width = screen.width;
        } else if (width < MIN_SIZE.width) {
            width = MIN_SIZE.width;
        }

        if (height > screen.height) {
            height = screen.height;
        } else if (height < MIN_SIZE.height) {
            height = MIN_SIZE.height;
        }

        int x = prefs.getInt( prefix + "x", (screen.width - width) / 2 );
        int y = prefs.getInt( prefix + "y", (screen.height - height) / 2 );

        if (x < 0) {
            x = 0;
        } else if (x > screen.width - width) {
            x = screen.width - width;
        }

        if (y < 0) {
            y = 0;
        } else if (y > screen.height - height) {
            y = screen.height - height;
        }

        frame.setSize( width, height );
        frame.setLocation( x, y );

    }


}
