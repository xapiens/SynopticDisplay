//  (c) 2001-2010 Fermi Research Alliance
//  $Id: PropertyCollection.java,v 1.3 2010/09/15 15:56:19 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:56:19 $
 */
public class PropertyCollection implements Collection<ComponentProperty<?>>, Cloneable {
    
    public static PropertyCollection create( Element e ) {
        PropertyCollection res = new PropertyCollection();
        for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() != Node.ELEMENT_NODE || !"property".equals( n.getNodeName())) {
                continue;
            }
            res.add( ComponentProperty.create( ((Element)n).getAttributes() ));
        }
        return res;
    }
    
    private final Map<String,ComponentProperty<?>> items =
            new LinkedHashMap<String,ComponentProperty<?>>();
    
    public PropertyCollection() {}

    public PropertyCollection( Collection<ComponentProperty<?>> cc ) {
        addAll( cc );
    }

    @Override
    public Object clone() {
        PropertyCollection res = new PropertyCollection(); 
        for (ComponentProperty<?> p : this) {
            res.add( (ComponentProperty<?>)p.clone());
        }
        return res;
    }
    
    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public boolean contains( Object o ) {
        return items.containsValue( (ComponentProperty)o );
    }

    @Override
    public Iterator<ComponentProperty<?>> iterator() {
        return items.values().iterator();
    }

    @Override
    public Object[] toArray() {
        return items.values().toArray();
    }

    @Override
    public <T> T[] toArray( T[] a ) {
        return items.values().toArray( a );
    }

    @Override
    public boolean add( ComponentProperty<?> p ) {
        if (p == null) {
            throw new NullPointerException();
        }
        items.put( p.getName(), p );
        return true;
    }

    @Override
    public boolean addAll( Collection<? extends ComponentProperty<?>> pp ) {
        boolean res = false;
        for (ComponentProperty<?> p : pp) {
            res |= add( p );
        }
        return res;
    }

    @Override
    public boolean remove( Object o ) {
        return items.values().remove( (ComponentProperty)o );
    }
    
    public boolean removeByName( String name ) {
        return items.remove( name ) != null;
    }

    @Override
    public boolean containsAll( Collection<?> cc ) {
        return items.values().containsAll( cc );
    }

    @Override
    public boolean removeAll( Collection<?> cc ) {
        return items.values().removeAll( cc );
    }

    @Override
    public boolean retainAll( Collection<?> cc ) {
        return items.values().retainAll( cc );
    }

    @Override
    public void clear() {
        items.clear();
    }
    
    public ComponentProperty<?> get( String name ) {
        return items.get( name );
    }
    
    public <T extends ComponentProperty> T get( Class<T> propType, String name ) throws PropertyException {
        if (propType == null || name == null) {
            throw new NullPointerException();
        }
        ComponentProperty<?> prop = get( name );
        if (prop == null) {
            return null;
        }
        try {
            return propType.cast( prop );
        } catch (ClassCastException ex) {
            throw new PropertyException( "Cannot cast " + prop.getDataTypeName() +
                    " property \"" + name + "\" to " + propType.getSimpleName());
        }
    }
    
    public <T> T getValue( Class<T> dataType, String name ) throws PropertyException {
        return getValue( dataType, name, null );
    }

    public <T> T getValue( Class<T> dataType, String name, T defValue ) throws PropertyException {
        if (dataType == null || name == null) {
            throw new NullPointerException();
        }
        ComponentProperty<?> prop = get( name );
        if (prop == null) {
            return defValue;
        }
        Object val = prop.getValue();
        if (val == null) {
            return defValue;
        }
        if (dataType == String.class) {
            val = val.toString();
        }
        try {
            return dataType.cast( val );
        } catch (ClassCastException ex) {
            throw new PropertyException( "Cannot cast " + prop.getDataTypeName() +
                    " property \"" + name + "\" to " + dataType.getSimpleName());
        }
    }

    public boolean hasValue( String name ) {
        if (name == null) {
            throw new NullPointerException();
        }
        ComponentProperty<?> prop = get( name );
        if (prop == null) {
            return false;
        }
        return prop.getValue() != null;
    }
    
    public <T> T findValue( Class<T> dataType, String... names ) throws PropertyException {
        for (String n : names) {
            T res = getValue( dataType, n );
            if (res != null) {
                return res;
            }
        }
        return null;
    }

    public void setComponent( Object component ) {
        for (ComponentProperty<?> p : this) {
            p.setComponent( component );
        }
    }
    
}
