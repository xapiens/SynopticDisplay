// (c) 2001-2010 Fermi Research Alliance
// $Id: LookupDisplayWithControl.java,v 1.3 2010/09/15 16:36:30 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.applications.syndi.property.Alignment;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.applications.syndi.property.Tolerator;
import gov.fnal.controls.applications.syndi.property.ValueMap;
import gov.fnal.controls.applications.syndi.runtime.RuntimeControl;
import gov.fnal.controls.tools.timed.TimedDouble;
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
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import static java.awt.GridBagConstraints.*;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:36:30 $
 */
@DisplayElement(

    name            = "Display With Selector",
    description     = "Lookup Display with a capability to set new values in a separate modal dialog.",
    group           = "Controls/Combo",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.VisualComponent",
    helpUrl         = "/DisplayWithSelector",

    properties  = {
        @Property( caption="Width",            name="width",       value="80",     type=Integer.class                 ),
        @Property( caption="Height",           name="height",      value="20",     type=Integer.class                 ),
        @Property( caption="Border",           name="border",      value="",       type=Color.class,   required=false ),
        @Property( caption="Border Width",     name="borderWidth", value="1.0",    type=Double.class                  ),
        @Property( caption="Background",       name="background",  value="",       type=Color.class,   required=false ),
        @Property( caption="Text Color",       name="textColor",   value="navy",   type=Color.class                   ),
        @Property( caption="Font Size",        name="fontSize",    value="12",     type=Integer.class                 ),
        @Property( caption="Italic Font",      name="isItalicFont",value="false",  type=Boolean.class                 ),
        @Property( caption="Bold Font",        name="isBoldFont",  value="false",  type=Boolean.class                 ),
        @Property( caption="Alignment",        name="align",       value="CENTER", type=Alignment.class               ),
        @Property( caption="Value Map",        name="valMap",      value="0=false,1=true",  type=ValueMap.class       ),
        @Property( caption="Input Tolerance",  name="tolerance",   value="0.001",  type=Double.class                  ),
        @Property( caption="'Not Found' Text", name="notFound",    value="",                          required=false  ),
        @Property( caption="String Prefix",    name="prefix",      value="",                          required=false  ),
        @Property( caption="Data Tag",         name="tag",         value="",                          required=false )
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

public class LookupDisplayWithControl extends LookupDisplay
        implements MouseListener, ActionListener, RuntimeControl {

    private static final Border BORDER = new CompoundBorder(
        new EtchedBorder( EtchedBorder.LOWERED ),
        new EmptyBorder( 6, 6, 6, 6 )
    );

    private static final Logger log = Logger.getLogger( LookupDisplayWithControl.class.getName());

    private boolean hover;
    private boolean settingEnabled;
    private JDialog dialog;
    private SortedMap<Double,JRadioButton> buttonMap;
    private String dataTag;

    private final JButton buOk = new JButton( "Ok" );
    private final JButton buCancel = new JButton( "Cancel" );
    private final JPanel controlPanel = new JPanel();
    private final JPanel buttonPanel = new JPanel();
    private final ButtonGroup buttons = new ButtonGroup();

    public LookupDisplayWithControl() {

        addMouseListener( this );
        setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ));

        buttonPanel.setLayout( new BoxLayout( buttonPanel, BoxLayout.Y_AXIS ));
        buttonPanel.setBorder( BORDER );

        controlPanel.setLayout( new GridBagLayout());
        controlPanel.add( buttonPanel, new GridBagConstraints( 0, 0, 2, 1, 1.0, 0.0,
                CENTER, BOTH,    new Insets( 12, 12, 12, 12 ), 0, 0 ));
        controlPanel.add( buOk,        new GridBagConstraints( 0, 1, 1, 1, 0.5, 0.0,
                EAST,   NONE,    new Insets(  6, 12, 12,  6 ), 0, 0 ));
        controlPanel.add( buCancel,    new GridBagConstraints( 1, 1, 1, 1, 0.5, 0.0,
                WEST,   NONE,    new Insets(  6,  6, 12, 12 ), 0, 0 ));

        buOk.setDefaultCapable( false );
        buCancel.setDefaultCapable( true );

        buOk.addActionListener( this );
        buCancel.addActionListener( this );

    }

    @Override
    public void init( PropertyCollection props ) throws Exception {

        super.init( props );

        buttonMap = new TreeMap<Double,JRadioButton>( new Tolerator( tolerance ));

        for (Map.Entry<Double,String> e : textMap.entrySet()) {

            JRadioButton button = new JRadioButton( e.getValue());
            buttons.add( button );
            buttonPanel.add( button );

            double value = e.getKey();
            buttonMap.put( value, button );

            long cmdBits = Double.doubleToLongBits( value );
            button.setActionCommand( String.valueOf( cmdBits ));

        }

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
        if (src == buOk) {
            ButtonModel button = buttons.getSelection();
            if (button != null) {
                long cmdBits = Long.parseLong( button.getActionCommand());
                double value = Double.longBitsToDouble( cmdBits );
                deliver( new TimedDouble( value ));
                log.fine( "New dialog value: " + value );
            }
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
            buttons.clearSelection();
        }
    }

    @Override
    public void setValue( double value ) {
        super.setValue( value );
        if (dialog == null) {
            JRadioButton button = buttonMap.get( value );
            if (button == null) {
                buttons.clearSelection();
            } else {
                button.setSelected( true );
            }
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
