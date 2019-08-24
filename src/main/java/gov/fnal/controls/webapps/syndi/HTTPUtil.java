// (c) 2001-2010 Fermi Research Alliance
// $Id: HTTPUtil.java,v 1.2 2010/09/15 16:14:05 apetrov Exp $
package gov.fnal.controls.webapps.syndi;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

/**
 * Utilities for outputting XML data and images in HTTP.  
 *  
 * @author  Andrey Petrov
 * @version $Revision: 1.2 $
 */
public class HTTPUtil {
    
    private static final Logger log = Logger.getLogger( HTTPUtil.class.getName());

    private static final TransformerFactory trf = TransformerFactory.newInstance();
    
    private static final Properties XFORM_PROPS = new Properties();
    
    private static final ErrorListener errorHandler = new ErrorListener() {
        
        @Override
        public void warning( TransformerException error ) {
            log.warning( error.getMessage());
        }

        @Override
        public void error( TransformerException error ) {
            log.severe( error.getMessage());
        }

        @Override
        public void fatalError( TransformerException error ) {
            log.severe( error.getMessage());
        }
        
    };
    
    static {
        XFORM_PROPS.put( OutputKeys.METHOD, "xml" );
        XFORM_PROPS.put( OutputKeys.ENCODING, "UTF-8" );
        XFORM_PROPS.put( OutputKeys.INDENT, "yes" );
        XFORM_PROPS.put( "{http://xml.apache.org/xslt}indent-amount", "4" );
    }

    public static void output( Document doc, OutputStream out ) 
            throws IOException {
        
        try {
            Transformer t = trf.newTransformer();
            t.setErrorListener( errorHandler );
            t.setOutputProperties( XFORM_PROPS );
            t.transform( new DOMSource( doc ), new StreamResult( out ));
        } catch (TransformerException ex) {
            throw new IOException( ex );
        }
        
    }

    public static void output( BufferedImage img, OutputStream out, String type ) 
            throws IOException {

        Iterator<ImageWriter> z = ImageIO.getImageWritersBySuffix( type );
        if (!z.hasNext()) {
            throw new IOException( "Unsupported image type: " + type );
        }
        ImageWriter writer = z.next();
        try {
            ImageOutputStream ios = ImageIO.createImageOutputStream( out );
            try {
                writer.setOutput( ios );
                writer.write( img );
                ios.flush();
            } finally {
                ios.close();
                ios = null;
            }
        } finally {
            writer.setOutput( null );
            writer.dispose();
            writer = null;
        }
        
    }
    
    private HTTPUtil() {}
    
}
