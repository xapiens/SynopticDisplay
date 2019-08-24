// (c) 2001-2010 Fermi Research Alliance
// $Id: DisplayAddressField.java,v 1.2 2010/09/15 15:53:52 apetrov Exp $
package gov.fnal.controls.applications.syndi.repository;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:53:52 $
 */
public class DisplayAddressField extends JComponent {

    private static Color BORDER_COLOR = new Color( 122, 138, 153 );

    private static final Logger log = Logger.getLogger( DisplayAddressField.class.getName());

    private static DisplayAddressEditor sharedEditor;
    private static ReentrantLock editorLock = new ReentrantLock();

    private static DisplayAddressEditor getSharedEditor() {
        if (sharedEditor == null) {
            sharedEditor = new DisplayAddressEditor();
        }
        return sharedEditor;
    }

    private final JTextField field = new JTextField();
    private final JButton button = new JButton( "\u2666" );

    public DisplayAddressField() {

        field.setBorder( new MatteBorder( 1, 1, 1, 0, BORDER_COLOR ));

        button.setBorder( new CompoundBorder(
            new MatteBorder( 1, 1, 1, 1, BORDER_COLOR ),
            new EmptyBorder( 2, 4, 2, 4 )
        ));
        button.setFocusPainted( false );
        button.setFocusable( false );

        setLayout( new BorderLayout());
        add( field, BorderLayout.CENTER );
        add( button, BorderLayout.EAST );

        button.addActionListener( new ActionListener() {

            @Override
            public void actionPerformed( ActionEvent e ) {
                useEditor();
            }

        });

    }

    private void useEditor() {
        if (!editorLock.tryLock()) {
            return;
        }
        try {
            DisplayAddressEditor editor = getSharedEditor();
            if (editor == null) {
                return;
            }
            String value = editor.showDialog( DisplayAddressField.this, getText());
            if (value == null) {
                return;
            }
            setText( value );
        } catch (DisplayAddressSyntaxException ex) {
            JOptionPane.showMessageDialog(
                this,
                "CAN NOT OPEN ADDRESS EDITOR.\n\n" +
                "The text you've entered doesn't look like a valid display address.\n" +
                "Perhaps, this is a web page URL. The editor can deal only with\n" +
                "Synoptic Display addresses.",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "CAN NOT OPEN ADDRESS EDITOR:\n\n" + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        } finally {
            editorLock.unlock();
        }
    }

    @Override
    public void setBackground( Color bg ) {
        field.setBackground( bg );
    }

    @Override
    public Color getBackground() {
        return field.getBackground();
    }

    @Override
    public void setForeground( Color fg ) {
        field.setForeground( fg );
    }

    @Override
    public Color getForeground() {
        return field.getForeground();
    }

    @Override
    public void setFont( Font font ) {
        field.setFont( font );
    }

    @Override
    public Font getFont() {
        return field.getFont();
    }

    public void setText( String text ) {
        field.setText( text );
    }

    public String getText() {
        return field.getText();
    }

    public void setURI( URI uri ) {
        setText( uri.toString());
    }

    public URI getURI() {
        String text = getText().trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return new URI( text );
        } catch (URISyntaxException ex) {
            log.throwing( DisplayAddressField.class.getName(), "getURI", ex );
            return null;
        }
    }

    public JTextField getTextField() {
        return field;
    }

}
