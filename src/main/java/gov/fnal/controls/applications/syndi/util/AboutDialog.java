// (c) 2001-2010 Fermi Research Allaince
// $Id: AboutDialog.java,v 1.2 2010/09/15 15:19:09 apetrov Exp $
package gov.fnal.controls.applications.syndi.util;

import java.awt.Frame;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import static java.awt.GridBagConstraints.*;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:19:09 $
 */
public final class AboutDialog extends JDialog implements ActionListener {

    private static final Border BORDER = new LineBorder( Color.GRAY, 1 );

    private static final Color BACKGROUND = Color.WHITE;

    private static final Font TITLE_FONT = new Font( "Dialog", Font.BOLD, 12 );
    private static final Color TITLE_COLOR = Color.BLACK;

    private static final Font TEXT_FONT = new Font( "Dialog", Font.PLAIN, 12 );
    private static final Color TEXT_COLOR = Color.BLACK;
    
    private static final Font LINK_FONT = new Font( "Dialog", Font.PLAIN, 12 );
    private static final Color LINK_COLOR = new Color( 0x0000CC );

    private static final Logger log = Logger.getLogger( AboutDialog.class.getName());

    public AboutDialog( Frame owner ) {

        super( owner, true );

        setDefaultCloseOperation( DISPOSE_ON_CLOSE );
        setResizable( false );
        setTitle( "About Synoptic" );
        getRootPane().setBorder( BORDER );

        Container cp = getContentPane();
        cp.setBackground( BACKGROUND );
        cp.setLayout( new GridBagLayout());

        final JButton buClose = new JButton( "Close" );
        buClose.addActionListener( this );

        cp.add( new LogoPanel(),
                new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0,
                CENTER, NONE, new Insets( 12, 12,  6, 12 ), 0, 0 ));
        cp.add( new Title( "Authors:" ),
                new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0,
                WEST, NONE,   new Insets(  6, 12,  3, 12 ), 0, 0 ));
        cp.add( new Link( "Andrey Petrov", "http://andrey.petrov.cx" ),
                new GridBagConstraints( 0, 2, 1, 1, 0.0, 0.0,
                WEST, NONE,   new Insets(  3, 36,  3, 12 ), 0, 0 ));
        cp.add( new Text( "Timofei Bolshakov" ),
                new GridBagConstraints( 0, 3, 1, 1, 0.0, 0.0,
                WEST, NONE,   new Insets(  3, 36,  6, 12 ), 0, 0 ));
        cp.add( new Title( "Project Home Page:" ),
                new GridBagConstraints( 0, 4, 1, 1, 0.0, 0.0,
                WEST, NONE,   new Insets(  6, 12,  3, 12 ), 0, 0 ));
        cp.add( new Link( "http://synoptic.fnal.gov" ),
                new GridBagConstraints( 0, 5, 1, 1, 0.0, 0.0,
                WEST, NONE,   new Insets(  3, 36,  6, 12 ), 0, 0 ));
        cp.add( new Title( "Legal Information:" ),
                new GridBagConstraints( 0, 6, 1, 1, 0.0, 0.0,
                WEST, NONE,   new Insets(  6, 12,  3, 12 ), 0, 0 ));
        cp.add( new Link( "http://fermitools.fnal.gov/about/terms.html" ),
                new GridBagConstraints( 0, 7, 1, 1, 0.0, 0.0,
                WEST, NONE,   new Insets(  3, 36,  6, 12 ), 0, 0 ));
        cp.add( buClose, 
                new GridBagConstraints( 0, 8, 1, 1, 0.0, 0.0,
                CENTER, NONE, new Insets( 12, 12, 12, 12 ), 0, 0 ));

        pack();
        setLocationRelativeTo( owner );
        setVisible( true );
        
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        dispose();
    }

    private static class Title extends JLabel {

        Title( String text ) {
            super( text );
            setForeground( TITLE_COLOR );
            setFont( TITLE_FONT );
        }

    }

    private static class Text extends JLabel {

        Text( String text ) {
            super( text );
            setForeground( TEXT_COLOR );
            setFont( TEXT_FONT );
        }

    }

    private static class Link extends JLabel implements MouseListener {

        private final String address;

        Link( String text ) {
            this( text, text );
        }

        Link( String text, String address ) {
            super( text );
            this.address = address;
            setForeground( LINK_COLOR );
            setFont( LINK_FONT );
            setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ));
            addMouseListener( this );
            setToolTipText( address );
        }

        @Override
        public void mouseClicked( MouseEvent e ) {
            try {
                Desktop desktop = Desktop.getDesktop();
                URI uri = URI.create( address );
                desktop.browse( uri );
            } catch (Exception ex) {
                log.log( Level.WARNING, "Cannot open web browser", ex );
            }
        }

        @Override
        public void mousePressed( MouseEvent e ) {}

        @Override
        public void mouseReleased( MouseEvent e ) {}

        @Override
        public void mouseEntered( MouseEvent e ) {}

        @Override
        public void mouseExited( MouseEvent e ) {}

    }
    
}
