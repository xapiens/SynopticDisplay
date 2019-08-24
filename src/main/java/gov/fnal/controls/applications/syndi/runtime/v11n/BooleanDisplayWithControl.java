// (c) 2001-2010 Fermi Research Alliance
// $Id: BooleanDisplayWithControl.java,v 1.3 2010/09/15 16:36:30 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.property.Alignment;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.applications.syndi.runtime.RuntimeControl;
import gov.fnal.controls.tools.timed.TimedBoolean;
import gov.fnal.controls.tools.timed.TimedError;
import gov.fnal.controls.tools.timed.TimedNumber;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import static java.awt.GridBagConstraints.*;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:36:30 $
 */
@DisplayElement(

    name            = "Display With Toggle Switch",
    description     = "A display showing boolean values with a capability to set new values in a separate modal dialog.",
    group           = "Controls/Combo",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.VisualComponent",
    helpUrl         = "/DisplayWithToggleSwitch",
    
    properties = {
        @Property( caption="Width",                       name="width",         value="80",     type=Integer.class                 ),
        @Property( caption="Height",                      name="height",        value="20",     type=Integer.class                 ),
        @Property( caption="Border",                      name="border",        value="",       type=Color.class,   required=false ),
        @Property( caption="Border Width",                name="borderWidth",   value="1.0",    type=Double.class                  ),
        @Property( caption="Font Size",                   name="fontSize",      value="12",     type=Integer.class                 ),
        @Property( caption="Italic Font",                 name="isItalicFont",  value="false",  type=Boolean.class                 ),
        @Property( caption="Bold Font",                   name="isBoldFont",    value="false",  type=Boolean.class                 ),
        @Property( caption="Alignment",                   name="align",         value="CENTER", type=Alignment.class               ),
        @Property( caption="Minimum Normal Value",        name="minValue",      value="0.0",    type=Double.class,  required=false ),
        @Property( caption="Maximum Normal Value",        name="maxValue",      value="0.0",    type=Double.class,  required=false ),
        @Property( caption="Invert State",                name="invert",        value="false",  type=Boolean.class, required=false ),
        @Property( caption="Background FALSE/OFF/NORMAL", name="offBackground", value="",       type=Color.class,   required=false ),
        @Property( caption="Background TRUE/ON/ALARM",    name="onBackground",  value="",       type=Color.class,   required=false ),
        @Property( caption="Color FALSE/OFF/NORMAL",      name="offColor",      value="navy",   type=Color.class                   ),
        @Property( caption="Color TRUE/ON/ALARM",         name="onColor",       value="navy",   type=Color.class                   ),
        @Property( caption="Text FALSE/OFF/NORMAL",       name="offString",     value="false",                      required=false ),
        @Property( caption="Text TRUE/ON/ALARM",          name="onString",      value="true",                       required=false ),
        @Property( caption="Data Tag",                    name="tag",           value="",                           required=false )
    },

    minInputs = 1,
    maxInputs = 1,
    minOutputs = 1,
    maxOutputs = 64,

    inputs = {
        @Pin( number=0, x=0, y=0.5, name="Adjust" )
    },

    outputs = {
        @Pin( number=1, x=1, y=0.5 )
    }
        
)
        
public class BooleanDisplayWithControl extends BooleanDisplay
        implements MouseListener, ActionListener, RuntimeControl {

    private static final Logger log = Logger.getLogger( BooleanDisplayWithControl.class.getName());

    private boolean hover;
    private boolean settingEnabled;
    private JDialog dialog;
    private String dataTag;

    private final JButton button0 = new JButton();
    private final JButton button1 = new JButton();
    private final JButton buCancel = new JButton( "Cancel" );
    private final JPanel controlPanel = new JPanel();

    public BooleanDisplayWithControl() {

        addMouseListener( this );
        setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ));

        controlPanel.setLayout( new GridBagLayout());
        controlPanel.add( button0,  new GridBagConstraints( 0, 0, 1, 1, 0.33, 0.0,
                CENTER, NONE,    new Insets( 12, 12, 12,  3 ), 0, 0 ));
        controlPanel.add( button1,  new GridBagConstraints( 1, 0, 1, 1, 0.33, 0.0,
                CENTER, NONE,    new Insets( 12,  3, 12,  3 ), 0, 0 ));
        controlPanel.add( buCancel, new GridBagConstraints( 2, 0, 1, 1, 0.33, 0.0,
                CENTER, NONE,    new Insets( 12,  3, 12, 12 ), 0, 0 ));

        button0.setDefaultCapable( false );
        button1.setDefaultCapable( false );
        buCancel.setDefaultCapable( true );

        button0.addActionListener( this );
        button1.addActionListener( this );
        buCancel.addActionListener( this );

    }

    @Override
    public void init( PropertyCollection props ) throws Exception {

        super.init( props );

        button0.setText( text0 );
        button1.setText( text1 );

        dataTag = props.getValue( String.class, "tag" );

    }

    /*
     * This method is overriden to disable morroring input data to the output.
     */
    @Override
    public void offer( TimedNumber data, int inputIndex ) {
        try {
            process( data );
        } catch (Throwable ex) {
            log.log( Level.SEVERE, "Error processing data", ex );
        }
    }

    @Override
    public String getDataTag( int outIndex ) {
        return dataTag;
    }

    @Override
    public void mouseClicked( MouseEvent e ) {
        if (settingEnabled || !super.doesSetting()) {
            showDialog();
        }
    }

    @Override
    public void mousePressed( MouseEvent e ) {}

    @Override
    public void mouseReleased( MouseEvent e ) {}

    @Override
    public void mouseEntered( MouseEvent e ) {
        hover = true;
        repaintEffectiveBounds();
    }

    @Override
    public void mouseExited( MouseEvent e ) {
        hover = false;
        repaintEffectiveBounds();
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        Object src = e.getSource();
        if (src == button0) {
            deliver( new TimedBoolean( false ));
            log.fine( "New dialog value: false" );
        } else if (src == button1) {
            deliver( new TimedBoolean( true ));
            log.fine( "New dialog value: true" );
        }
        if (dialog != null) {
            dialog.setVisible( false );
        }
    }

    @Override
    public void setSettingEnabled( boolean settingEnabled ) {
        this.settingEnabled = settingEnabled;
        repaintEffectiveBounds();
    }

    @Override
    public void paint( Graphics g ) {
        super.paint( g );
        if (hover && !settingEnabled && super.doesSetting()) {
            g.setColor( DISABLED_CROSS_COLOR );
            g.drawLine( 0, 0, getWidth() - 1, getHeight() - 1 );
            g.drawLine( 0, getHeight() - 1, getWidth() - 1, 0 );
        }
    }

    @Override
    public boolean doesSetting() {
        return false;
    }

    @Override
    public void setError( TimedError error ) {
        super.setError( error );
        if (dialog == null) {
            button0.setEnabled( true );
            button1.setEnabled( true );
        }
    }

    @Override
    public void setValue( boolean value ) {
        super.setValue( value );
        if (dialog == null) {
            button0.setEnabled( value );
            button1.setEnabled( !value );
        }
    }

    private void showDialog() {
        Window win = SwingUtilities.getWindowAncestor( this );
        dialog = new JDialog( win, JDialog.DEFAULT_MODALITY_TYPE );
        try {
            dialog.getRootPane().setDefaultButton( buCancel );
            dialog.setTitle( getDataTag( 0 ));
            dialog.setResizable( false );
            dialog.getContentPane().add( controlPanel );
            dialog.pack();
            dialog.setLocationRelativeTo( this );
            dialog.setVisible( true );
        } finally {
            dialog.dispose();
            dialog = null;
        }
    }
    
}
