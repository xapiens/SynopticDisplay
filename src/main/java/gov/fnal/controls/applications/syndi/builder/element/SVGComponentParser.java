// (c) 2001-2010 Fermi Research Allaince
// $Id: SVGComponentParser.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import gov.fnal.controls.applications.syndi.SynopticConfig;
import gov.fnal.controls.applications.syndi.property.ComponentProperty;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.applications.syndi.util.SVGUtil;
import gov.fnal.controls.tools.svg.SVGText;
import gov.fnal.controls.tools.svg.SVGComponent;
import gov.fnal.controls.tools.svg.SVGPath;
import gov.fnal.controls.tools.svg.SVGSvg;
import gov.fnal.controls.tools.svg.SVGSyntaxHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public class SVGComponentParser extends AbstractParser<BuilderComponent> {
    
    private final PropertyCollection props = new PropertyCollection();
    private final BuilderContainer parent;
    private final SVGSyntaxHandler nestedParser;

    private String helpUrl;
    private int depth, originAdjustmentCount;
    
    public SVGComponentParser( boolean namespaceAware ) {
        this( null, namespaceAware );
    }

    public SVGComponentParser( BuilderContainer parent, boolean namespaceAware ) {
        this.parent = parent;
        nestedParser = new SVGSyntaxHandler( namespaceAware );
    }
    
    @Override
    public void startElement( String uri, String name, String qName, Attributes attrs ) {
        if (done) {
            throw new IllegalStateException( "Parsing is completed" );
        }
        if (depth == 0) {
            helpUrl = attrs.getValue( SynopticConfig.DISPLAY_NS, "help-url" );
        }
        depth++;
        if (depth == 2 && SynopticConfig.DISPLAY_NS.equals( uri ) && "property".equals( name )) {
            ComponentProperty<?> p = ComponentProperty.create( attrs );
            props.add( p );
        }
        nestedParser.startElement( uri, name, qName, attrs );
    }
    
    @Override
    public void endElement( String uri, String name, String qName ) throws SAXException {
        if (done) {
            throw new IllegalStateException( "Parsing is completed" );
        }
        nestedParser.endElement( uri, name, qName );
        if (--depth > 0) {
            return;
        }
        SVGComponent comp = nestedParser.getResult();
        if (!(comp instanceof SVGSvg)) {
            throw new SAXException( "Invalid SVG component: " + comp );
        }
        SVGSvg svg = (SVGSvg)comp;
        try {
            originAdjustmentCount = SVGUtil.adjustOrigin( svg );
            SVGUtil.forceMonospaceFonts( svg );
            int x0 = (svg.getX() == null) ? 0 : Math.round( svg.getX().floatValue());
            int y0 = (svg.getY() == null) ? 0 : Math.round( svg.getY().floatValue());
            String title = svg.getTitle();
            String desc = svg.getDescription();
            for (SVGComponent c : svg) {
                if (c instanceof SVGPath) {
                    result = new GenericLine( (SVGPath)c );
                    if (title == null) {
                        title = "Generic Line";
                    }
                } else if (c instanceof SVGText) {
                    result = new GenericText( (SVGText)c );
                    if (title == null) {
                        title = "Generic Text";
                    }
                } else {
                    result = new GenericShape( c );
                    if (title == null) {
                        title = "Generic Shape";
                    }
                }
                result.setProperties( props );
                result.setLocation( x0, y0 );
                result.setName( title );
                result.setDescription( desc );
                if (helpUrl != null) {
                    result.setHelpUrl( helpUrl );
                }
                if (parent != null) {
                    parent.add( result );
                }
            }
            done = true;
        } catch (Exception ex) {
            throw new SAXException( ex );
        }
    }
    
    @Override
    public void characters( char ch[], int start, int length ) throws SAXException {
        if (done) {
            throw new IllegalStateException( "Parsing is completed" );
        }
        nestedParser.characters( ch, start, length );
    }
    
    @Override
    public int getOriginAdjustmentCount() {
        return originAdjustmentCount;
    }

    @Override
    public int getRemovedInvisibleCount() {
        return 0;
    }

}
