// (c) 2001-2010 Fermi Research Allaince
//  $Id: LogoPanel.java,v 1.2 2010/09/15 15:19:09 apetrov Exp $
package gov.fnal.controls.applications.syndi.util;

import java.awt.GridBagConstraints;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import static java.awt.GridBagConstraints.*;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:19:09 $
 */
public final class LogoPanel extends JPanel {

    private static final String IMAGE_NAME = "logo.png";

    private static final Font TEXT_FONT = new Font( "Dialog", Font.BOLD, 12 );
    private static final Color TEXT_COLOR = Color.BLACK;

    private static final int VERSION_OFFSET = 120;

    private final Image image;
    private final Dimension size;

    public LogoPanel() {
        URL url = LogoPanel.class.getResource( IMAGE_NAME );
        if (url != null) {
            image = new ImageIcon( url ).getImage();
            size = new Dimension(
                    image.getWidth( null ),
                    image.getHeight( null )
            );
        } else {
            image = null;
            size = new Dimension();
        }
        setLayout( new GridBagLayout());
        add( new Text( "Version " + System.getProperty( "Synoptic.version", "N/A" )),
                new GridBagConstraints( 0, 0, 1, 1, 0.0, 1.0,
                NORTH, NONE, new Insets( VERSION_OFFSET, 0, 0, 0 ), 0, 0 ));
    }

    @Override
    protected void paintComponent( Graphics g ) {
        if (image != null) {
            g.drawImage( image, 0, 0, null );
        }
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension( size );
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension( size );
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension( size );
    }

    private static class Text extends JLabel {

        Text( String text ) {
            super( text );
            setForeground( TEXT_COLOR );
            setFont( TEXT_FONT );
        }

    }

}
