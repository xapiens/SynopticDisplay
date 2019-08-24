// (c) 2001-2010 Fermi Research Allaince
//  $Id: ImageFactory.java,v 1.2 2010/09/15 15:19:09 apetrov Exp $
package gov.fnal.controls.applications.syndi.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.swing.ImageIcon;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:19:09 $
 */
public class ImageFactory {
    
    public static final String DEFAULT_IMAGE_TYPE = "image/jpeg";

    public static String encode( Image img ) {
        return encode( img, DEFAULT_IMAGE_TYPE );
    }

    public static String encode( Image img, String type ) 
            throws IllegalArgumentException {
        if (type == null) {
            type = DEFAULT_IMAGE_TYPE;
        }
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType( type );
        if (!writers.hasNext()) {
            throw new IllegalArgumentException( "Image writer for " + type + " not found" );
        }
        ImageWriter writer = writers.next();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageOutputStream ios = new MemoryCacheImageOutputStream( out );
            try {
                writer.setOutput( ios );
                writer.write( toBufferedImage( img ));
                return new String( Base64.encodeBase64( out.toByteArray()), "UTF-8" );
            } finally {
                ios.close();
            }
        } catch (IOException ex) {
            throw new RuntimeException( ex ); // Shouldn't happen
        } finally {
            writer.dispose();
        }
    }
    
    public static BufferedImage decode( String data ) throws IOException {
        return decode( data, DEFAULT_IMAGE_TYPE );
    }

    public static BufferedImage decode( String data, String type )
            throws IOException, IllegalArgumentException {
        if (type == null) {
            type = DEFAULT_IMAGE_TYPE;
        }
        Iterator<ImageReader> readers = ImageIO.getImageReadersByMIMEType( type );
        if (!readers.hasNext()) {
            throw new IllegalArgumentException( "Image reader for " + type + " not found" );
        }
        ImageReader reader = readers.next();
        try {
            byte[] buf = Base64.decodeBase64( data.getBytes( "UTF-8" ));
            ByteArrayInputStream inp = new ByteArrayInputStream( buf );
            ImageInputStream iis = new MemoryCacheImageInputStream( inp );
            try {
                reader.setInput( iis );
                return reader.read( 0 );
            } catch (IndexOutOfBoundsException ex) {
                throw new IOException( ex );
            } finally {
                iis.close();
            }
        } finally {
            reader.dispose();
        }
    }

    public static BufferedImage toBufferedImage( Image image ) {
        image = new ImageIcon( image ).getImage(); // Make sure all pixels are loaded
        BufferedImage bimage = new BufferedImage( 
            image.getWidth( null ), 
            image.getHeight( null ), 
            hasAlpha( image ) 
                ? BufferedImage.TYPE_INT_ARGB 
                : BufferedImage.TYPE_INT_RGB
        );
        Graphics g = bimage.createGraphics();
        g.drawImage( image, 0, 0, null );
        g.dispose();
        return bimage;        
    }

    private static boolean hasAlpha( Image image ) {
        if (image instanceof BufferedImage) {
            return ((BufferedImage)image).getColorModel().hasAlpha();
        }
        PixelGrabber pg = new PixelGrabber( image, 0, 0, 1, 1, false );
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {}
        ColorModel cm = pg.getColorModel();
        return (cm == null) ? false : cm.hasAlpha();
    }

    public static Image load( File file ) throws IOException {
        if (file == null) {
            throw new NullPointerException();
        }
        ImageInputStream inp = new FileImageInputStream( file );
        try {
            Iterator<ImageReader> readers = ImageIO.getImageReaders( inp );
            if (!readers.hasNext()) {
                throw new IllegalArgumentException( "Unrecognized image format" );
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput( inp );
                return reader.read( 0 );
            } catch (IndexOutOfBoundsException ex) {
                throw new IOException( ex );
            } finally {
                reader.dispose();
            }
        } finally {
            inp.close();
        }
    }

    public static void save( Image img, File file ) throws IOException {
        if (img == null || file == null) {
            throw new NullPointerException();
        }
        String name = file.getName();
        int i = name.lastIndexOf( '.' );
        if (i == -1 || i == name.length() - 1) {
            throw new IllegalArgumentException( "File extension should describe the image format" );
        }
        String type = name.substring( i + 1 );
        Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix( type );
        if (!writers.hasNext()) {
            throw new IllegalArgumentException( "Image writer for " + type + " not found" );
        }
        ImageWriter writer = writers.next();
        try {
            ImageOutputStream inp = new FileImageOutputStream( file );
            try {
                writer.setOutput( inp );
                writer.write( toBufferedImage( img ));
            } finally {
                inp.close();
            }
        } finally {
            writer.dispose();
        }
    }

    public static BufferedImage getTransparentPixel() throws IOException {
        BufferedImage img = new BufferedImage( 1, 1, BufferedImage.TYPE_INT_ARGB );
        Graphics g = img.getGraphics();
        g.setColor( new Color( 0, 0, 0, 0 ));
        g.fillRect( 0, 0, 1, 1 );
        return img;
    }
    
    private ImageFactory() {}
    
}
