// (c) 2001-2010 Fermi Research Allaince
//  $Id: Base64IconEncoder.java,v 1.2 2010/09/15 15:19:09 apetrov Exp $
package gov.fnal.controls.applications.syndi.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:19:09 $
 */
public class Base64IconEncoder {
    
    private static final String OUTPUT_FORMAT = "png";
    
    public static void main( String[] args ) {
        if (args.length == 0) {
            throw new IllegalArgumentException( "Input file should be specified" );
        }
        
        try {
            
            String fileName = args[ 0 ];
            int i = fileName.lastIndexOf( "." );
            String type = (i >= 0 && i < fileName.length()-1) 
                    ? fileName.substring( i+1 )
                    : "";
            
            BufferedImage img;
            
            Iterator<ImageReader> readers = ImageIO.getImageReadersBySuffix( type );
            if (!readers.hasNext()) {
                throw new Exception( "Cannot find image reader for " + type + " files" );
            }
            ImageReader reader = readers.next();

            InputStream inp = new FileInputStream( fileName );
            try {
                ImageInputStream iis = ImageIO.createImageInputStream( inp );
                reader.setInput( iis );
                img = reader.read( 0 );
            } finally {
                inp.close();
            }
            
            Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix( OUTPUT_FORMAT );
            if (!writers.hasNext()) {
                throw new Exception( "Cannot find image writer for " + OUTPUT_FORMAT + " files" );
            }
            ImageWriter writer = writers.next();
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                MemoryCacheImageOutputStream ios = new MemoryCacheImageOutputStream( out );
                try {
                    writer.setOutput( ios );
                    writer.write( img );
                    String s = new String( Base64.encodeBase64( out.toByteArray()), "UTF-8" );
                    System.out.println( s );
                } finally {
                    ios.close();
                }
            } finally {
                out.close();
            }
            
        } catch (Exception ex) {
            throw new RuntimeException( ex );
        }
    }
    
    private Base64IconEncoder() {}

}
