// (c) 2001-2010 Fermi Research Alliance
// $Id: UniversalFont.java,v 1.2 2010/09/15 15:48:22 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

import java.awt.Font;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:48:22 $
 */
public class UniversalFont extends Font {
    
    public static final String FONT_NAME = "Monospaced";
    
    public static final Font DEFAULT = new UniversalFont( 12, false, false );

    public UniversalFont( int size, boolean bold, boolean italic ) {
        super( 
            FONT_NAME,
            (bold ? BOLD : PLAIN) | (italic ? ITALIC : PLAIN),
            size
        );
    }
    
}
