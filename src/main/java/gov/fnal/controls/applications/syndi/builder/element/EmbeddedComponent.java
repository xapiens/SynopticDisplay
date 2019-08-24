// (c) 2001-2010 Fermi Research Allaince
// $Id: EmbeddedComponent.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import gov.fnal.controls.applications.syndi.property.Alignment;
import gov.fnal.controls.applications.syndi.property.ComponentProperty;
import gov.fnal.controls.applications.syndi.property.PropertyException;
import gov.fnal.controls.applications.syndi.repository.DisplaySource;
import gov.fnal.controls.applications.syndi.repository.DisplayURISource;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public class EmbeddedComponent extends GenericComponent {

    private static final Logger log = Logger.getLogger( EmbeddedComponent.class.getName());

    private final BuilderContainer container = new TempContainer();

    private DisplaySource<?> disp;
    private boolean scale;
    private Alignment align;
    private GenericContainer delegate;
    private String caption;
    private Set<URL> parents;

    public EmbeddedComponent() {}

    @Override
    public ComponentType getType() {
        return ComponentType.VISUAL;
    }

    void setParents( Set<URL> parents ) {
        this.parents = (parents == null) ? null : new HashSet<URL>( parents );
    }

    @Override
    public String getCaption() {
        return caption;
    }

    @Override
    public void setProperties( Collection<ComponentProperty<?>> val ) throws PropertyException {
        super.setProperties( val );
        URI target = props.getValue( URI.class, "target" );
        if (target != null) {
            try {
                disp = new DisplayURISource( target, parents );
            } catch (IllegalArgumentException ex) {
                throw new PropertyException( ex.getMessage(), ex );
            }
        } else {
            disp = null;
        }
        align = props.getValue( Alignment.class, "align", Alignment.CENTER );
        scale = props.getValue( Boolean.class, "scale", false );
        reload();
    }

    @Override
    public boolean isBackgroundImageEnabled() {
        return false;
    }

    @Override
    public void reload() {
        if (disp != null) {
            try {
                delegate = disp.createBuilderComponent();
                delegate.setParent( container );
                caption = null;
            } catch (FileNotFoundException ex) {
                delegate = null;
                caption = "Display Not Found";
            } catch (Exception ex) {
                log.log( Level.INFO, "Cannot load display", ex );
                delegate = null;
                caption = ex.getMessage();
            }
        } else {
            delegate = null;
            caption = "Display Not Specified";
        }
    }

    @Override
    protected void paintContents( Graphics2D g ) {
        if (delegate == null) {
            super.paintContents( g );
            return;
        }
        AffineTransform xform = g.getTransform();
        if (scale) {
            double sx = (double)getWidth() / (double)delegate.getWidth();
            double sy = (double)getHeight() / (double)delegate.getHeight();
            g.scale( sx, sy );
        } else {
            int tx, ty;
            switch (align) {
                case NORTHWEST :
                case WEST :
                case SOUTHWEST :
                    tx = 0;
                    break;
                case NORTHEAST :
                case EAST :
                case SOUTHEAST :
                    tx = getWidth() - delegate.getWidth();
                    break;
                default :
                    tx = (getWidth() - delegate.getWidth()) / 2;
            }
            switch (align) {
                case NORTHWEST :
                case NORTH :
                case NORTHEAST :
                    ty = 0;
                    break;
                case SOUTHWEST :
                case SOUTH :
                case SOUTHEAST :
                    ty = getHeight() - delegate.getHeight();
                    break;
                default :
                    ty = (getHeight() - delegate.getHeight()) / 2;
            }
            g.translate( tx, ty );
        }
        delegate.paintContentsWithBackground( g );
        g.setTransform( xform );
    }

}
