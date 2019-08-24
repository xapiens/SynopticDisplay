// (c) 2001-2010 Fermi Research Alliance
// $Id: VisualComponent.java,v 1.3 2010/09/15 15:48:22 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.applications.syndi.property.PropertyException;
import gov.fnal.controls.applications.syndi.runtime.DisplayFormatException;
import gov.fnal.controls.applications.syndi.runtime.RuntimeComponent;
import gov.fnal.controls.applications.syndi.runtime.RuntimeComponentSupport;
import gov.fnal.controls.applications.syndi.util.ImageFactory;
import gov.fnal.controls.tools.timed.TimedNumber;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import javax.swing.JComponent;
import org.w3c.dom.Element;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import org.w3c.dom.Node;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:48:22 $
 */
public abstract class VisualComponent extends JComponent implements RuntimeComponent {
    
    public static final Color ERROR_CROSS_COLOR = Color.GRAY;
    
    public static final int DEFAULT_WIDHT = 10;
    public static final int DEFAULT_HEIGHT = 10;

    public static final Color DEFAULT_FOREGROUND = Color.BLACK; // must not be null

    public static final int DEFAULT_FONT_SIZE = 12;

    public static void handleStandartProps( PropertyCollection props, JComponent comp )
            throws PropertyException {

        int x = props.getValue( Integer.class, "x", 0 );
        int y = props.getValue( Integer.class, "y", 0 );
        int w = props.getValue( Integer.class, "width", DEFAULT_WIDHT );
        int h = props.getValue( Integer.class, "height", DEFAULT_HEIGHT );

        comp.setBounds( x, y, w, h );
        comp.setMinimumSize( new Dimension( w, h ));
        comp.setMaximumSize( new Dimension( w, h ));
        comp.setPreferredSize( new Dimension( w, h ));

        Color fgColor = props.getValue( Color.class, "textColor", DEFAULT_FOREGROUND );
        if (fgColor != null) {
            comp.setForeground( fgColor );
            if (comp instanceof JTextComponent) {
                ((JTextComponent)comp).setSelectedTextColor( fgColor );
            }
        }

        Color bgColor = props.getValue( Color.class, "background" );
        comp.setOpaque( bgColor != null );
        comp.setBackground( bgColor );

        Font font = new UniversalFont(
            props.getValue( Integer.class, "fontSize", DEFAULT_FONT_SIZE ),
            props.getValue( Boolean.class, "isBoldFont", false ),
            props.getValue( Boolean.class, "isItalicFont", false )
        );
        comp.setFont( font );
    }

    private static Image createBgImage( Element source ) throws DisplayFormatException {
        for (Node n = source.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() != Node.ELEMENT_NODE || !"bkimage".equals( n.getNodeName())) {
                continue;
            }
            String data = n.getTextContent();
            if (data == null) {
                throw new DisplayFormatException( "Invalid background image" );
            }
            data = data.trim();
            String type = ((Element)n).getAttribute( "type" );
            if ("".equals( type )) {
                type = null;
            }
            try {
                return ImageFactory.decode( data, type );
            } catch (Exception ex) {
                throw new DisplayFormatException( "Cannot create background", ex );
            }
        }
        return null;
    }

    private static final Logger log = Logger.getLogger( VisualComponent.class.getName());

    private final RuntimeComponentSupport sup = new RuntimeComponentSupport();

    protected Image bgImage;
    
    protected VisualComponent() {
        super.setOpaque( false );
    }
    
    @Override
    public final void init( Element source ) throws Exception {
        PropertyCollection props = PropertyCollection.create( source );
        handleStandartProps( props, this );
        bgImage = createBgImage( source );
        init( props );
    }

    protected abstract void init( PropertyCollection props ) throws Exception;

    @Override
    public void setInput( int index, RuntimeComponent comp, int reverseIndex ) {
        sup.setInput( index, comp, reverseIndex );
    }

    @Override
    public RuntimeComponent getInput( int index ) {
        return sup.getInput( index );
    }

    @Override
    public void setOutput( int index, RuntimeComponent comp, int reverseIndex ) {
        sup.setOutput( index, comp, reverseIndex );
    }

    @Override
    public RuntimeComponent getOutput( int index ) {
        return sup.getOutput( index );
    }

    @Override
    public String getDataTag( int outIndex ) {
        return sup.getDataTag( outIndex );
    }

    @Override
    public boolean doesSetting() {
        return sup.doesSetting();
    }

    @Override
    public void offer( TimedNumber data, int inputIndex ) {
        try {
            process( data );
        } catch (Throwable ex) {
            log.log( Level.SEVERE, "Error processing data", ex );
        }
        deliver( data );
    }

    public void deliver( TimedNumber data ) {
        sup.deliver( data );
    }

    public void deliver( TimedNumber data, int index ) {
        sup.deliver( data, index );
    }

    protected abstract void process( TimedNumber data );

    @Override
    public void setOpaque( boolean opaque ) {}

}
