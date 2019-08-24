// (c) 2001-2010 Fermi Research Allaince
// $Id: ComponentParser.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import gov.fnal.controls.applications.syndi.builder.element.pin.Pin;
import gov.fnal.controls.applications.syndi.builder.element.pin.PinAddress;
import gov.fnal.controls.applications.syndi.builder.element.pin.PinCollection;
import gov.fnal.controls.applications.syndi.builder.element.pin.PinType;
import gov.fnal.controls.applications.syndi.property.ComponentProperty;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.applications.syndi.util.SVGUtil;
import java.awt.geom.Point2D;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public class ComponentParser extends AbstractParser<BuilderComponent> {
    
    private static final Logger log = Logger.getLogger( ComponentParser.class.getName());
    
    private final PropertyCollection props = new PropertyCollection();
    private final BuilderContainer parent;
    private final Set<URL> parents;

    private Integer minInputs, maxInputs, minOutputs, maxOutputs;
    private AbstractParser nestedParser;
    private int originAdjustmentCount, removedInvisibleCount;
    
    public ComponentParser() {
        this( null, null );
    }
    
    public ComponentParser( Set<URL> parents ) {
        this( null, parents );
    }

    public ComponentParser( BuilderContainer parent, Set<URL> parents ) {
        this.parent = parent;
        this.parents = (parents == null) ? null : new HashSet<URL>( parents );
    }

    @Override
    public void startElement( String uri, String name, String qName, Attributes attrs ) 
            throws SAXException {
        if (done) {
            throw new IllegalStateException( "Parsing is completed" );
        }
        try {
            boolean accepted = false;
            if (nestedParser == null) {
                if (result == null) {
                    if ("element".equals( name )) {
                        String id = attrs.getValue( "id" );
                        result = BuilderComponentFactory.createComponent( 
                            (id == null) ? null : new Integer( id ),
                            parseStr( attrs.getValue( "name" )),
                            parseStr( attrs.getValue( "designTimeView" )), 
                            parseStr( attrs.getValue( "implementation" ))
                        );
                        if (result instanceof AbstractComponent) {
                            minInputs = parseInt( attrs.getValue( "minInputs" ));
                            maxInputs = parseInt( attrs.getValue( "maxInputs" ));
                            minOutputs = parseInt( attrs.getValue( "minOutputs" ));
                            maxOutputs = parseInt( attrs.getValue( "maxOutputs" ));
                        }
                        String helpUrl = parseStr( attrs.getValue( "help-url" ));
                        if (helpUrl != null) {
                            result.setHelpUrl( helpUrl );
                        }
                        if (result instanceof EmbeddedComponent) {
                            ((EmbeddedComponent)result).setParents( parents );
                        }
                        accepted = true;
                    } else if ("svg".equals( name )) {
                        boolean namespaceAware = !"".equals( uri );
                        nestedParser = new SVGComponentParser( namespaceAware );
                    }
                } else if ("desc".equals( name ) || "description".equals( name )) {
                    nestedParser = new DescriptionParser( result );
                } else if ("icon".equals( name )) {
                    // Deprecated element -- do nothing.
                    accepted = true;
                } else if ("property".equals( name )) {
                    ComponentProperty<?> p = ComponentProperty.create( attrs );
                    props.add( p );
                    accepted = true;
                } else { 
                    if (result instanceof BuilderContainer) {
                        BuilderContainer cont = (BuilderContainer)result;
                        if ("element".equals( name )) {
                            nestedParser = new ComponentParser( cont, parents );
                        } else if ("svg".equals( name )) {
                            boolean namespaceAware = !"".equals( uri );
                            nestedParser = new SVGComponentParser( cont, namespaceAware );
                        } else if ("link".equals( name )) {
                            ComponentLink link = createLink( attrs );
                            cont.add( link );
                            accepted = true;
                        }
                    }
                    if (result instanceof AbstractComponent) {
                        AbstractComponent aco = (AbstractComponent)result;
                        if ("bkimage".equals( name )) {
                            nestedParser = new ImageParser( aco );
                        } else if ("input".equals( name )) {
                            createPin( aco.pins(), PinType.INPUT, attrs );
                            accepted = true;
                        } else if ("output".equals( name )) {
                            createPin( aco.pins(), PinType.OUTPUT, attrs );
                            accepted = true;
                        }
                    }
                }
            }
            if (nestedParser != null) {
                nestedParser.startElement( uri, name, qName, attrs );
            } else if (!accepted) {
                log.warning( "Invalid element: " + name );
            }
        } catch (SAXException ex) {
            throw ex;
        } catch (Exception ex) {
            log.log( Level.SEVERE, "Error parsing a component", ex ); 
            throw new SAXException( ex );
        }
    }
    
    @Override
    public void endElement( String uri, String name, String qName ) throws SAXException {
        if (done) {
            throw new IllegalStateException( "Parsing is completed" );
        }
        try {
            if (nestedParser != null) {
                nestedParser.endElement( uri, name, qName );
                if (nestedParser.isDone()) {
                    if (result == null && nestedParser instanceof SVGComponentParser) {
                        result = ((SVGComponentParser)nestedParser).getResult();
                        done = true;
                    }
                    originAdjustmentCount += nestedParser.getOriginAdjustmentCount();
                    removedInvisibleCount += nestedParser.getRemovedInvisibleCount();
                    nestedParser = null;
                }
            } else if ("element".equals( name )) {
                result.setProperties( props );
                if (parent != null) {
                    parent.add( result );
                }
                if (result instanceof AbstractComponent) {
                    PinCollection pins = ((AbstractComponent)result).pins();
                    if (minInputs != null) {
                        pins.setMinInputCount( minInputs );
                    }
                    if (maxInputs != null) {
                        pins.setMaxInputCount( maxInputs );
                    }
                    if (minOutputs != null) {
                        pins.setMinOutputCount( minOutputs );
                    }
                    if (maxOutputs != null) {
                        pins.setMaxOutputCount( maxOutputs );
                    }
                }
                if (result instanceof GenericContainer) {
                    removedInvisibleCount += SVGUtil.removeInvisibleImages( (GenericContainer)result );
                }
                done = true;
            }
        } catch (SAXException ex) {
            throw ex;
        } catch (Exception ex) {
            log.log( Level.SEVERE, "Error parsing component", ex ); 
            throw new SAXException( ex );
        }
    }

    @Override
    public void characters( char ch[], int start, int length ) throws SAXException {
        if (done) {
            throw new IllegalStateException( "Parsing is completed" );
        }
        if (nestedParser != null) {
            nestedParser.characters( ch, start, length );
        }
    }
    
    private static ComponentLink createLink( Attributes attrs ) {
        PinAddress source = new PinAddress( 
            Integer.parseInt( attrs.getValue( "source" )),
            Integer.parseInt( attrs.getValue( "source_pin" ))
        );
        PinAddress target = new PinAddress( 
            Integer.parseInt( attrs.getValue( "target" )),
            Integer.parseInt( attrs.getValue( "target_pin" ))
        );
        ComponentLink res = new ComponentLink( source, target );
        String path = attrs.getValue( "path" );
        if (path != null && path.length() > 0) {
            res.setPathString( path );
        }
        return res;
    }
    
    private static void createPin( PinCollection pins, PinType type, Attributes attrs ) {
        int index = Integer.parseInt( attrs.getValue( "number" ));
        Pin p = pins.addPin( index, type, attrs.getValue( "name" ));
        float x = Float.parseFloat( attrs.getValue( "x" ));
        float y = Float.parseFloat( attrs.getValue( "y" ));
        p.setLocation( new Point2D.Float( x, y ));
    }

    private static String parseStr( String str ) {
        if (str != null) {
            str = str.trim();
            if (!"".equals( str )) {
                return str;
            }
        }
        return null;
    }

    private static Integer parseInt( String str ) throws NumberFormatException {
        if (str != null) {
            str = str.trim();
            if (!"".equals( str )) {
                return new Integer( str );
            }
        }
        return null;
    }

    @Override
    public int getOriginAdjustmentCount() {
        return originAdjustmentCount;
    }

    @Override
    public int getRemovedInvisibleCount() {
        return removedInvisibleCount;
    }


}
