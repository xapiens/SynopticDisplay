// (c) 2001-2010 Fermi Research Allaince
// $Id: PaletteLoader.java,v 1.3 2010/09/15 16:01:12 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.palette;

import gov.fnal.controls.applications.syndi.SynopticConfig;
import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.runtime.RuntimeComponent;
import gov.fnal.controls.applications.syndi.util.ImageFactory;
import gov.fnal.controls.tools.resource.ResourceIterator;
import gov.fnal.controls.tools.svg.SVGNamespace;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * 
 * @author Andrey Petrov, Timofei Bolshakov
 * @version $Date: 2010/09/15 16:01:12 $
 */
class PaletteLoader {

    private static final Pattern NAME_PATTERN = Pattern.compile( "^.*\\.(class|xml)$" );

    private static final String DEFAULT_ICON_TYPE = "image/png";

    private static final Logger log = Logger.getLogger( PaletteLoader.class.getName());
    
    private static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    
    static {
        dbf.setValidating( false );
        dbf.setNamespaceAware( true );
    }
    
    private DocumentBuilder builder;

    PaletteLoader() {}

    private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        if (builder == null) {
            builder = dbf.newDocumentBuilder();
        }
        builder.reset();
        return builder;
    }
    
    public PaletteNode load() throws Exception {
        int cnt = 0;
        PaletteNode root = new PaletteDirNode();
        for (String dir : SynopticConfig.getComponentPaths()) {
            cnt += scanDirectory( root, dir );
        }
        log.info(  "Loaded " + cnt + " items into components palette" );
        return root;
    }

    private int scanDirectory( PaletteNode root, String dir ) throws Exception {
        int cnt = 0;
        ResourceIterator iter = new ResourceIterator( dir, NAME_PATTERN );
        String prefix = "/" + iter.getNormalizedDirectory() + "/";
        for (ResourceIterator z = new ResourceIterator( dir, NAME_PATTERN ); z.hasNext();) {
            URL url = z.next();
            String name = url.toString();
            if (name.endsWith( ".class" )) {
                String className = getClassName( name, prefix );
                cnt += loadClass( root, className );
            } else if( name.endsWith( ".xml" ) ) {
                String xmlName = getXmlName( name, prefix );
                cnt += loadXml( root, url, xmlName );
            }
        }
        return cnt;
    }

    private static String getClassName( String fullName, String prefix ) {
        int i = fullName.lastIndexOf( prefix );
        return fullName.substring( i + 1, fullName.length() - 6 ).replace( '/', '.' );
    }

    private static String getXmlName( String fullName, String prefix ) {
        int i = fullName.lastIndexOf( prefix );
        return fullName.substring( i + prefix.length(), fullName.length() - 4 );
    }

    private int loadClass( PaletteNode root, String className ) throws IOException {
        try {

            Class<?> clazz = Class.forName( className );
            if (!clazz.isAnnotationPresent( DisplayElement.class )
                    || clazz.isAnnotationPresent( Deprecated.class )
                    || !RuntimeComponent.class.isAssignableFrom( clazz )) {
                return 0;
            }

            DisplayElement e = clazz.getAnnotation( DisplayElement.class );
            String compName = e.name();
            PaletteNode node = new PaletteClassNode(
                compName,
                e.description(),
                createIcon( e.icon(), compName, DEFAULT_ICON_TYPE ),
                clazz.asSubclass( RuntimeComponent.class )
            );
            
            return appendNode( root, node, e.group());

        } catch (ClassNotFoundException ex) {
            log.warning( "Class not found: " + className );
            return 0;
        } catch (LinkageError ex) {
            log.warning( "Cannot load " + className + ": " + ex.getMessage());
            return 0;
        }
    }
    
    
    private int loadXml( PaletteNode root, URL url, String path ) throws Exception {
        InputStream inp = url.openStream();
        try {
            
            Document doc = getDocumentBuilder().parse( inp );
            Element e0 = doc.getDocumentElement();
            String tag = e0.getLocalName();
            String ns = e0.getNamespaceURI();
            if ( !("element".equals( tag ) && SynopticConfig.DISPLAY_NS.equals( ns )
                    || "svg".equals( tag ) && SVGNamespace.XMLNS_SVG.equals( ns )) ) {
                return 0;
            }

            String title = null;
            String description = null;
            String iconData = null;
            String iconType = null;

            for (Node n = e0.getFirstChild(); n != null; n = n.getNextSibling()) {
                if (!(n instanceof Element)) {
                    continue;
                }
                tag = ((Element)n).getLocalName();
                ns = ((Element)n).getNamespaceURI();
                if ("title".equals( tag ) && SVGNamespace.XMLNS_SVG.equals( ns )) {
                    title = n.getTextContent();
                } else if ("desc".equals( tag ) && SVGNamespace.XMLNS_SVG.equals( ns )) {
                    description = n.getTextContent();
                } else if ("icon".equals( tag ) && SynopticConfig.DISPLAY_NS.equals( ns )) {
                    iconData = n.getTextContent();
                    if (iconData != null) {
                        iconData = iconData.trim();
                        if ("".equals( iconData )) {
                            iconData = null;
                        }
                    }
                    iconType = ((Element)n).getAttribute( "mime-type" );
                    if (iconType != null && iconType.isEmpty()) {
                        iconType = null;
                    }
                }
            }
            
            if (description != null) {
                description = description.trim();
            }
            if (iconData != null) {
                iconData = iconData.trim();
            }

            String name;
            int i = path.lastIndexOf( '/' );
            if (i >= 0) {
                name = path.substring( i + 1 );
                path = path.substring( 0, i );
            } else {
                name = path;
                path = "";
            }
            if (title != null) {
                name = title;
            }

            if (iconType == null) {
                iconType = DEFAULT_ICON_TYPE;
            }

            PaletteNode node = new PaletteXMLNode(
                name, 
                description,
                createIcon( iconData, name, iconType ),
                url 
            );
            return appendNode( root, node, path );

        } catch (SAXException ex) {
            log.warning( "Error parsing " + url );
            return 0;
        } finally {
            inp.close();
        }
    }
    
    private int appendNode( PaletteNode root, PaletteNode node, String path ) {
        if (path == null) {
            path = "";
        }
        PaletteNode n0 = root;
        for (String name : path.split( "/+" )) {
            PaletteNode n1 = n0.getChildByName( name );
            if (n1 == null) {
                n1 = new PaletteDirNode( name );
                n0.add( n1 );
            } else if (n1.isLeaf()) {
                log.fine( "Duplicate item ignored: " + name );
                return 0;
            }
            n0 = n1;
        }
        String name = node.getName();
        if (n0.getChildByName( name ) != null) {
            log.fine( "Duplicate item ignored: " + name );
            return 0;
        }
        n0.add( node );
        log.finer( "Loaded " + node );
        return 1;
    }


    private static Icon createIcon( String iconData, String compName, String type ) {
        if (iconData == null || "".equals( iconData )) {
            return null;
        }
        try {
            return new ImageIcon( ImageFactory.decode( iconData, type ));
        } catch (IOException ex) {
            log.info( "Cannot load image for " + compName );
            return null;
        }
    }
    
}
