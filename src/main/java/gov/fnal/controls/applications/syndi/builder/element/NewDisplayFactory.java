// (c) 2001-2010 Fermi Research Allaince
// $Id: NewDisplayFactory.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import gov.fnal.controls.applications.syndi.property.ColorProperty;
import gov.fnal.controls.applications.syndi.property.IntegerProperty;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.applications.syndi.property.PropertyException;
import gov.fnal.controls.tools.svg.SVGColor;
import java.awt.Color;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public class NewDisplayFactory {
    
    private static final int DEFAULT_WIDTH  = 600;
    private static final int DEFAULT_HEIGHT = 400;
    private static final Color DEFAULT_BACKGROUND = SVGColor.parseColor( "white" );
    
    private int width = DEFAULT_WIDTH;
    private int height = DEFAULT_HEIGHT;
    private Color bgColor = DEFAULT_BACKGROUND; 
    
    public NewDisplayFactory() {}
    
    public void setDefaultWidth( int val ) {
        if (val <= 0) {
            throw new IllegalArgumentException();
        }
        this.width = val;
    }
    
    public int getDefaultWidth() {
        return width;
    }

    public void setDefaultHeight( int val ) {
        if (val <= 0) {
            throw new IllegalArgumentException();
        }
        this.height = val;
    }
    
    public int getDefaultHeight() {
        return height;
    }
    
    public void setDefaultBackground( Color val ) {
        this.bgColor = val;
    }
    
    public Color getDefaultBackground() {
        return bgColor;
    }
    
    public GenericContainer createNewDisplay() {
        try {
            GenericContainer res = new GenericContainer();
            res.pins().setMaxInputCount( 0 );
            res.pins().setMinInputCount( 0 );
            res.pins().setMaxOutputCount( 0 );
            res.pins().setMinOutputCount( 0 );
            PropertyCollection props = (PropertyCollection)res.getProperties(); // TODO
            props.get( IntegerProperty.class, "width" ).setValue( width );
            props.get( IntegerProperty.class, "height" ).setValue( height );
            props.add( new ColorProperty( "background", "Background Color", false, bgColor ));
            res.setProperties( props );
            return res;
        } catch (PropertyException ex) {
            throw new RuntimeException( ex );
        }
    }

}
