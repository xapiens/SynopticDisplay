//  (c) 2001-2010 Fermi Research Alliance
//  $Id: ComponentProperty.java,v 1.3 2010/09/15 15:56:19 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

import gov.fnal.controls.applications.syndi.SynopticConfig;
import gov.fnal.controls.applications.syndi.markup.Property;
import java.lang.reflect.Constructor;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:56:19 $
 */
public abstract class ComponentProperty<T> implements Cloneable {

    private static final String DEFAULT_IMPL = 
            "gov.fnal.controls.applications.syndi.property.StringProperty";

    public static ComponentProperty create( Attributes attrs )
            throws IllegalArgumentException {
        Class<? extends ComponentProperty> impl = getImplClass( attrs.getValue( "type" ));
        try {
            Constructor<? extends ComponentProperty> c = impl.getConstructor(
                String.class,
                String.class,
                boolean.class
            );
            ComponentProperty prop = c.newInstance(
                attrs.getValue( "name" ),
                attrs.getValue( "caption" ),
                !"false".equalsIgnoreCase( attrs.getValue( "required" ))
            );
            prop.setValueAsString( attrs.getValue( "value" ));
            prop.setGlobal( "true".equalsIgnoreCase( attrs.getValue( "global" )));
            prop.setGlobalName( attrs.getValue( "global-name" ));
            return prop;
        } catch (Exception ex) {
            throw new RuntimeException( ex );
        }
    }

    public static ComponentProperty create( NamedNodeMap attrs )
            throws IllegalArgumentException {
        Class<? extends ComponentProperty> impl = getImplClass( getAttrValue( attrs, "type" ));
        try {
            Constructor<? extends ComponentProperty> c = impl.getConstructor(
                String.class,
                String.class,
                boolean.class
            );
            ComponentProperty prop = c.newInstance(
                getAttrValue( attrs, "name" ),
                getAttrValue( attrs, "caption" ),
                !"false".equalsIgnoreCase( getAttrValue( attrs, "required" ))
            );
            prop.setValueAsString( getAttrValue( attrs, "value" ));
            prop.setGlobal( "true".equalsIgnoreCase( getAttrValue( attrs, "global" )));
            prop.setGlobalName( getAttrValue( attrs, "global-name" ));
            return prop;
        } catch (Exception ex) {
            throw new RuntimeException( ex );
        }
    }

    public static ComponentProperty create( Property anno )
            throws IllegalArgumentException {
        Class<? extends ComponentProperty> impl = getImplClass( anno.type().getSimpleName());
        try {
            Constructor<? extends ComponentProperty> c = impl.getConstructor(
                String.class,
                String.class,
                boolean.class
            );
            ComponentProperty prop = c.newInstance(
                anno.name(),
                anno.caption(), // TODO
                anno.required()
            );
            prop.setValueAsString( anno.value());
            prop.setGlobal( false );
            prop.setGlobalName( null );
            return prop;
        } catch (Exception ex) {
            throw new RuntimeException( ex );
        }
    }

    private static Class<? extends ComponentProperty> getImplClass( String type ) 
            throws IllegalArgumentException {
        String impl = SynopticConfig.getPropertyMap().get( type.toLowerCase() );
        if (impl == null) {
            impl = DEFAULT_IMPL;
        }
        try {
            return Class.forName( impl ).asSubclass( ComponentProperty.class );
        } catch (Throwable ex) {
            throw new IllegalArgumentException( "Invalid type: " + type, ex );
        }
    }

    private static String getAttrValue( NamedNodeMap attrs, String name ) {
        Node n = attrs.getNamedItem( name );
        return (n == null) ? null : n.getNodeValue();
    }

    private final String name;
    private final Class<T> dataType;
    private final boolean required;

    private T value;
    private boolean global, localOnly;
    private String caption, globalName;
    private Object component;

    protected ComponentProperty( Class<T> dataType, String name, String caption, boolean required ) {
        if (dataType == null || name == null) {
            throw new NullPointerException();
        }
        this.dataType = dataType;
        this.name = name;
        this.caption = caption;
        this.required = required;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException( ex ); // shouldn't happen
        }
    }

    public Class<T> getDataType() {
        return dataType;
    }

    public String getDataTypeName() {
        return dataType.getSimpleName().toLowerCase();
    }

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return required;
    }

    public void setGlobal( boolean global ) {
        this.global = global;
    }

    public boolean isGlobal() {
        return global && !localOnly;
    }

    public String getGlobalName() {
        return globalName;
    }

    public void setGlobalName( String globalName ) {
        this.globalName = globalName;
    }

    public void setLocalOnly( boolean localOnly ) {
        this.localOnly = localOnly;
    }

    public boolean isLocalOnly() {
        return localOnly;
    }

    public void setCaption( String caption ) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public void setComponent( Object component ) {
        this.component = component;
    }

    public Object getComponent() {
        return component;
    }
    
    public void setValue( T value ) {
        this.value = value;
    }
    
    public void setValueAsObject( Object obj ) throws PropertyException {
        if (obj == null) {
            setValue( null );
        } else {
            try {
                setValue( dataType.cast( obj ));
            } catch (ClassCastException ex) {
                throw new PropertyException( "Cannot cast " + obj + " to " + getDataTypeName());
            }
        }
    }

    public abstract void setValueAsString( String str ) throws PropertyException;

    public T getValue() {
        return value;
    }

    public String getValueAsString() {
        return (value == null) ? null : value.toString();
    }

    public Element getXML( Document doc ) {
        Element e = doc.createElement( "property" );
        setAttributes( e );
        return e;
    }

    public Element getXML_NS( Document doc ) {
        Element e = doc.createElementNS( SynopticConfig.DISPLAY_NS, "syndi:property" );
        setAttributes( e );
        return e;
    }

    private void setAttributes( Element e ) {
        e.setAttribute( "name", name );
        e.setAttribute( "type", getDataTypeName());
        if (caption != null) {
            e.setAttribute( "caption", caption );
        }
        if (!required) {
            e.setAttribute( "required", "false" );
        }
        if (value != null) {
            e.setAttribute( "value", getValueAsString());
        }
        if (global) {
            e.setAttribute( "global", "true" );
        }
        if (globalName != null && !"".equals( globalName )) {
            e.setAttribute( "global-name", globalName );
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "[name=" + name +
                ";required=" + required +
                ";global=" + global +
                ";global-name=" + globalName +
                ";caption=" + caption +
                ";value=" + value + "]";
    }

    protected abstract Class<? extends TableCellEditor> getEditorImpl();

    protected abstract Class<? extends TableCellRenderer> getRendererImpl();

}
