// (c) 2001-2010 Fermi Research Allaince
// $Id: SvgDisplayImages.java,v 1.2 2010/09/15 15:25:14 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:25:14 $
 */
class SvgDisplayImages {
    
    private static final Logger log = Logger.getLogger( SvgDisplayImages.class.getName());

    private final DocumentBuilder builder;
    private final Document startBanner, errorBanner;
    private final BufferedImage defaultImage;

    SvgDisplayImages() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating( false );
        dbf.setNamespaceAware( true );
        try {
            builder = dbf.newDocumentBuilder();
        } catch (Exception ex) {
            throw new Error( ex );
        }
        String startBannerLoc = System.getProperty( "Synoptic.start-banner" );
        startBanner = (startBannerLoc == null) ? null : loadSvg( startBannerLoc );
        String errorBannerLoc = System.getProperty( "Synoptic.error-banner" );
        errorBanner = (errorBannerLoc == null) ? null : loadSvg( errorBannerLoc );
        defaultImage = loadPng( "default.png" );
    }

    private Document loadSvg( String fileName ) {
        try {
            URL url = getClass().getResource( fileName );
            if (url == null) {
                throw new FileNotFoundException();
            }
            InputStream inp = url.openStream();
            try {
                return builder.parse( inp );
            } finally {
                inp.close();
            }
        } catch (Exception ex) {
            log.log( Level.SEVERE, "Cannot load standard image " + fileName, ex );
            return null;
        }
    }

    private BufferedImage loadPng( String fileName ) {
        try {
            URL url = getClass().getResource( fileName );
            if (url == null) {
                throw new FileNotFoundException();
            }
            InputStream inp = url.openStream();
            try {
                ImageReader reader = ImageIO.getImageReadersBySuffix( "png" ).next();
                ImageInputStream iis = ImageIO.createImageInputStream( inp );
                reader.setInput( iis );
                return reader.read( 0 );
            } finally {
                inp.close();
            }
        } catch (Exception ex) {
            log.log( Level.SEVERE, "Cannot load standard image " + fileName, ex );
            return null;
        }
    }

    public Document getStartBanner() {
        if (startBanner == null) {
            return startBanner;
        }
        return (Document)startBanner.cloneNode( true );
    }

    public Document getErrorBanner( String message ) {
        if (errorBanner == null) {
            return errorBanner;
        }
        Document doc = (Document)errorBanner.cloneNode( true );
        if (message != null) {
            NodeList list = doc.getElementsByTagName( "text" );
            for (int i = 0; i < list.getLength(); ++i) {
                Element e = (Element)list.item( i );
                if ("0".equals( e.getAttribute( "id" ))) {
                    e.appendChild( doc.createTextNode( message ));
                }
            }
        }
        return doc;
    }

    public BufferedImage getDefaultImage() {
        return defaultImage;
    }

}
