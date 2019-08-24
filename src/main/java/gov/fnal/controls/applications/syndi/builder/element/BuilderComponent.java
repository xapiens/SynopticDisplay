// (c) 2001-2010 Fermi Research Allaince
// $Id: BuilderComponent.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import gov.fnal.controls.applications.syndi.property.ComponentProperty;
import gov.fnal.controls.applications.syndi.property.PropertyException;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.Collection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public interface BuilderComponent {

    static final int MIN_SIZE = 10;

    void setParent( BuilderContainer parent );

    BuilderContainer getParent();
    
    ComponentType getType();

    void setName( String name );
    
    String getName();

    String getCaption();
    
    void setDescription( String desc );
    
    String getDescription();
    
    void setHelpUrl( String helpUrl );
    
    String getHelpUrl();
    
    void setProperties( Collection<ComponentProperty<?>> val ) throws PropertyException;
    
    Collection<ComponentProperty<?>> getProperties();
    
    Element getXML( Document doc );

    void setSelected( boolean val );

    boolean isSelected();
    
    void setLocation( int x, int y );

    int getX();
    
    int getY();

    Rectangle getBounds();
    
    boolean contains( Point p ); // Relatively to the container

    Shape getMovingShape( Point dp );
    
    void paint( Graphics2D g );
    
    void repaintComponent();

    void reload();

}
