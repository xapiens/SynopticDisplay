// (c) 2001-2010 Fermi Research Allaince
// $Id: BuilderContainer.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import gov.fnal.controls.applications.syndi.builder.CanvasAction;
import gov.fnal.controls.applications.syndi.property.ComponentProperty;
import gov.fnal.controls.applications.syndi.property.PropertyException;
import java.awt.Cursor;
import java.util.Collection;
import java.util.EnumSet;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public interface BuilderContainer extends Iterable<BuilderComponent> {
    
    void repaintComponent( int x, int y, int w, int h );
    
    void doAction( CanvasAction action );
    
    void setCursor( Cursor cursor );
    
    GridAttributes getGridAttributes();
    
    EnumSet<ComponentType> getVisibleTypes();

    void setGlobalProperties( Collection<ComponentProperty<?>> props ) throws PropertyException;

    Collection<ComponentProperty<?>> getGlobalProperties();

    boolean add( BuilderComponent comp );
    
}
