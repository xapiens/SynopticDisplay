// (c) 2001-2010 Fermi Research Allaince
// $Id: Config.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import gov.fnal.controls.tools.svg.SVGColor;
import java.awt.Color;
import java.awt.Font;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public interface Config {
    
    final int PIN_SIZE                  = 7;
    final int ANCHOR_SIZE               = 5;
    final int BORDER_SIZE               = 1;
    final int MARGIN_SIZE               = 5;
    final int LINK_SIZE                 = 1;
    final int LINK_HIGHLIGHT_SIZE       = 3;
    final int HIT_TOLERANCE             = 5;
    final int PIN_CAPTION_SPACING       = 3;
    
    final Color BORDER_COLOR            = SVGColor.parseColor( "dimgray" );
    final Color ANCHOR_COLOR            = SVGColor.parseColor( "dimgray" );
    final Color PIN_BORDER_COLOR        = SVGColor.parseColor( "black" );
    final Color INPUT_PIN_COLOR         = SVGColor.parseColor( "cyan" );
    final Color OUTPUT_PIN_COLOR        = SVGColor.parseColor( "lime" );
    final Color OUTLINE_COLOR           = SVGColor.parseColor( "blue" );
    final Color COMPONENT_CAPTION_COLOR = SVGColor.parseColor( "black" );
    final Color PIN_CAPTION_COLOR       = SVGColor.parseColor( "maroon" );
    final Color LINK_COLOR              = SVGColor.parseColor( "olive" );
    final Color LINK_HIGHLIGHT_COLOR    = SVGColor.parseColor( "red" );
    final Color GRID_COLOR              = SVGColor.parseColor( "gray" );
    
    final Font CAPTION_FONT             = new Font( "Dialog", Font.PLAIN, 9 );
    
}
