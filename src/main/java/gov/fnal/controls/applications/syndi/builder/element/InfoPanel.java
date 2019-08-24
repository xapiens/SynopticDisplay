// (c) 2001-2010 Fermi Research Allaince
// $Id: InfoPanel.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import javax.swing.JTextArea;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public class InfoPanel extends JTextArea {
    
    public InfoPanel( String text, boolean editable ) {
        super( text, 10, 40 );
        setLineWrap( true );
        setWrapStyleWord( true );
        setEditable( editable );
    }

}
