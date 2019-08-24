// (c) 2001-2010 Fermi Research Allaince
// $Id: BackgroundComponentFactory.java,v 1.2 2010/09/15 15:25:14 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime;

import gov.fnal.controls.applications.syndi.util.ImageFactory;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.image.VolatileImage;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import org.w3c.dom.Element;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:25:14 $
 */
class BackgroundComponentFactory {

    private JComponent owner;

    BackgroundComponentFactory( JComponent owner ) {
        this.owner = owner;
    }

    JComponent createComponent( Element source ) throws DisplayFormatException {
        return createComponent( source, null );
    }

    JComponent createComponent( Element source, String type ) throws DisplayFormatException {
        String data = source.getTextContent();
        if (data == null) {
            throw new DisplayFormatException( "Invalid background image" );
        }
        data = data.trim();
        try {
            Image image = ImageFactory.decode( data, type );
            image = new ImageIcon( image ).getImage(); // make sure the image is loaded
            return new BackgroundComponent( image );
        } catch (Exception ex) {
            throw new DisplayFormatException( "Cannot create background", ex );
        }
    }

    class BackgroundComponent extends JComponent {

        private final Image image;
        private final int width, height;
        private final Color bgColor;

        private VolatileImage vim;

        private BackgroundComponent( Image image ) {
            width = owner.getWidth();
            height = owner.getHeight();
            bgColor = owner.getBackground();
            this.image = image;
        }

        @Override
        public void paint( Graphics g ) {
            do {
                checkImage();
                g.drawImage( vim, 0, 0, this );
             } while (vim.contentsLost());
        }

        @Override
        public Dimension getSize() {
            return new Dimension( width, height );
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        private void checkImage() {
            GraphicsConfiguration conf = getGraphicsConfiguration();
            int s = (vim == null)
                    ? VolatileImage.IMAGE_INCOMPATIBLE
                    : vim.validate( conf );
            if (s == VolatileImage.IMAGE_INCOMPATIBLE) {
                vim = createVolatileImage( width, height );
            }
            if (s == VolatileImage.IMAGE_INCOMPATIBLE || s == VolatileImage.IMAGE_RESTORED) {
                Graphics2D g = vim.createGraphics();
                g.drawImage( image, 0, 0, width, height, bgColor, null );
                g.dispose();
            }
        }

    }

}
