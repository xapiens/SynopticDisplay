//  (c) 2001-2010 Fermi Research Alliance
//  $Id: PropertyList.java,v 1.1 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public class PropertyList extends ArrayList<ComponentProperty<?>> implements Cloneable {

    public PropertyList() {
        super();
    }

    public PropertyList( Collection<ComponentProperty<?>> cc ) {
        super( cc );
    }

    @Override
    public Object clone() {
        PropertyList res = new PropertyList();
        for (ComponentProperty<?> p : this) {
            res.add( (ComponentProperty<?>)p.clone());
        }
        return res;
    }

}
