//  (c) 2001-2010 Fermi Research Alliance
//  $Id: DeviceName.java,v 1.2 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public class DeviceName implements CharSequence {
    
    private final String name;

    public DeviceName( String name ) {
        if (name == null) {
            throw new NullPointerException();
        }
        this.name = name;
    }

    @Override
    public int length() {
        return name.length();
    }

    @Override
    public char charAt( int index ) {
        return name.charAt( index );
    }

    @Override
    public CharSequence subSequence( int start, int end ) {
        return name.subSequence( start, end );
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals( Object obj ) {
        return (obj instanceof DeviceName) || name.equals( ((DeviceName)obj).name );
    }

    @Override
    public String toString() {
        return name;
    }

}
