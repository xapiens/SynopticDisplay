// (c) 2001-2010 Fermi Research Allaince
// $Id: SplashScreen.java,v 1.2 2010/09/15 15:19:09 apetrov Exp $
package gov.fnal.controls.applications.syndi.util;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JWindow;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

/**
 * This class implements a splash screen that appears at program startup.
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:19:09 $
 */
public class SplashScreen extends JWindow {

    private static final Border BORDER = new LineBorder( Color.GRAY, 1 );

    public SplashScreen() {
        LogoPanel logo = new LogoPanel();
        logo.setBorder( BORDER );
        getContentPane().setLayout( new BorderLayout());
        getContentPane().add( logo, BorderLayout.CENTER );
        pack();
        setLocationRelativeTo( null );
        setVisible( true );
    }

}
