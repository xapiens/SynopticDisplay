// (c) 2001-2010 Fermi Research Allaince
// $Id: ImageParser.java,v 1.3 2010/09/15 19:08:35 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import gov.fnal.controls.applications.syndi.util.ImageFactory;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 19:08:35 $
 */
public class ImageParser extends AbstractParser<Image> {
    
    private static final Logger log = Logger.getLogger( ImageParser.class.getName());

    private final StringBuilder buf = new StringBuilder();
    private final AbstractComponent owner;
    
    private String type;
    private boolean started;
    
    public ImageParser() {
        this( null );
    }

    public ImageParser( AbstractComponent owner ) {
        this.owner = owner;
    }
    
    @Override
    public void startElement( String uri, String name, String qName, Attributes attrs ) 
            throws SAXException { 
        if (done || started) {
            throw new SAXException( "Illegal state" );
        }
        started = true;
        type = attrs.getValue( "type" );
        if (type == null) {
            type = attrs.getValue( "mime-type" );
        }
        String url = attrs.getValue( "url" );            
        if (url != null) {
            try {
                result = Toolkit.getDefaultToolkit().getImage( new URL( url ));
            } catch (Exception ex) {
                log.log( Level.WARNING, "Cannot load background image from " + url, ex );
            }
        }
    }
    
    @Override
    public void endElement( String uri, String name, String qName ) 
            throws SAXException {
        if (done) {
            throw new SAXException( "Illegal state" );
        }
        done = true;
        if (buf.length() > 0) {
            if (result != null) {
                log.warning( "Duplicated image data" );
            }
            String str = buf.toString().trim();
            try {
                result = ImageFactory.decode( str, type );
            } catch (IOException ex) {
                log.log( Level.WARNING, "Cannot decode background image", ex );
            }
        }
        if (owner != null) {
            owner.setBackgroundImage( result );
        }
    }
    
    @Override
    public void characters( char ch[], int start, int length ) throws SAXException {
        if (done) {
            throw new SAXException( "Illegal state" );
        }
        buf.append( ch, start, length );
    }
    
    @Override
    public int getOriginAdjustmentCount() {
        return 0;
    }

    @Override
    public int getRemovedInvisibleCount() {
        return 0;
    }

}
