// (c) 2001-2010 Fermi Research Allaince
// $Id: TempContainer.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import gov.fnal.controls.applications.syndi.builder.CanvasAction;
import gov.fnal.controls.applications.syndi.property.ComponentProperty;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public class TempContainer extends ArrayList<BuilderComponent> implements BuilderContainer, BuilderComponent {

    private final EnumSet<ComponentType> visibleTypes = EnumSet.of(
        ComponentType.VISUAL,
        ComponentType.GRAPHICS
    );

    public TempContainer() {}

    @Override
    public void repaintComponent( int x, int y, int w, int h ) {}

    @Override
    public void doAction( CanvasAction action ) {}

    @Override
    public void setCursor( Cursor cursor ) {}

    @Override
    public GridAttributes getGridAttributes() {
        return null;
    }

    @Override
    public EnumSet<ComponentType> getVisibleTypes() {
        return visibleTypes;
    }

    @Override
    public void setGlobalProperties( Collection<ComponentProperty<?>> props ) {}

    @Override
    public Collection<ComponentProperty<?>> getGlobalProperties() {
        return null;
    }

    @Override
    public void setParent( BuilderContainer parent ) {}

    @Override
    public BuilderContainer getParent() {
        return null;
    }

    @Override
    public ComponentType getType() {
        return ComponentType.TEMP;
    }

    @Override
    public void setName( String name ) {}

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getCaption() {
        return null;
    }

    @Override
    public void setDescription( String desc ) {}

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void setHelpUrl( String helpUrl ) {}

    @Override
    public String getHelpUrl() {
        return null;
    }

    @Override
    public void setProperties( Collection<ComponentProperty<?>> val ) {}

    @Override
    public Collection<ComponentProperty<?>> getProperties() {
        return null;
    }

    @Override
    public Element getXML( Document doc ) {
        return null;
    }

    @Override
    public void setSelected( boolean val ) {}

    @Override
    public boolean isSelected() {
        return false;
    }

    @Override
    public void setLocation( int x, int y ) {}

    @Override
    public int getX() {
        return 0;
    }

    @Override
    public int getY() {
        return 0;
    }

    @Override
    public Rectangle getBounds() {
        return null;
    }

    @Override
    public boolean contains( Point p ) {
        return false;
    }

    @Override
    public Shape getMovingShape( Point dp ) {
        return null;
    }

    @Override
    public void paint( Graphics2D g ) {}

    @Override
    public void repaintComponent() {}

    @Override
    public void reload() {}

}
