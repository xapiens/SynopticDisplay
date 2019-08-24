// (c) 2001-2010 Fermi Research Allaince
// $Id: PreferencesDialog.java,v 1.3 2010/09/15 15:59:56 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder;

import gov.fnal.controls.applications.syndi.property.ColorComboBox;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import static java.awt.GridBagConstraints.*;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:59:56 $
 */
public class PreferencesDialog extends JPanel {
    
    private final JSpinner gridStepE = new JSpinner( new SpinnerNumberModel( 1, 1, 100, 1 ));
    private final JSpinner defWidthE = new JSpinner( new SpinnerNumberModel( 1, 1, 10000, 1 ));
    private final JSpinner defHeightE = new JSpinner( new SpinnerNumberModel( 1, 1, 10000, 1 ));
    private final ColorComboBox defBgColorE = new ColorComboBox();
    
    public PreferencesDialog( int gridStep, int defWidth, int defHeight, Color defBgColor ) {
        super( new GridBagLayout());
        
        gridStepE.setValue( gridStep );
        
        JPanel gridPanel = new JPanel( new GridBagLayout());
        gridPanel.setBorder(  new TitledBorder( "Canvas Grid" ));
        
        JLabel gridStepL = new JLabel( "Grid Step:" );
        gridStepL.setDisplayedMnemonic( 'S' );
        gridStepL.setLabelFor( gridStepE );
        
        gridPanel.add( gridStepL, new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST,   GridBagConstraints.NONE,
            new Insets( 12, 12, 11, 11 ), 0, 0 ));
        gridPanel.add( gridStepE, new GridBagConstraints( 1, 0, 1, 1, 1.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
            new Insets( 12,  0, 11, 11 ), 0, 0 ));
        
        defWidthE.setValue( defWidth );
        defHeightE.setValue( defHeight );
        defBgColorE.setSelectedItem( defBgColor );

        JPanel newDispPanel = new JPanel( new GridBagLayout());
        newDispPanel.setBorder(  new TitledBorder( "New Display" ));
        
        JLabel defWidthL = new JLabel( "Width:" );
        defWidthL.setDisplayedMnemonic( 'W' );
        defWidthL.setLabelFor( defWidthE );

        JLabel defHeightL = new JLabel( "Height:" );
        defHeightL.setDisplayedMnemonic( 'H' );
        defHeightL.setLabelFor( defHeightE );

        JLabel defBgColorL = new JLabel( "Background Color:" );
        defBgColorL.setDisplayedMnemonic( 'B' );
        defBgColorL.setLabelFor( defBgColorE );
        
        newDispPanel.add( defWidthL,   new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0,
            WEST,   NONE,       new Insets( 12, 12, 11, 11 ), 0, 0 ));
        newDispPanel.add( defWidthE,   new GridBagConstraints( 1, 0, 1, 1, 1.0, 0.0,
            CENTER, HORIZONTAL, new Insets( 12,  0, 11, 11 ), 0, 0 ));
        newDispPanel.add( defHeightL,  new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0,
            WEST,   NONE,       new Insets( 12, 12, 11, 11 ), 0, 0 ));
        newDispPanel.add( defHeightE,  new GridBagConstraints( 1, 1, 1, 1, 1.0, 0.0,
            CENTER, HORIZONTAL, new Insets( 12,  0, 11, 11 ), 0, 0 ));
        newDispPanel.add( defBgColorL, new GridBagConstraints( 0, 2, 1, 1, 0.0, 0.0,
            WEST,   NONE,       new Insets( 12, 12, 11, 11 ), 0, 0 ));
        newDispPanel.add( defBgColorE, new GridBagConstraints( 1, 2, 1, 1, 1.0, 0.0,
            CENTER, HORIZONTAL, new Insets( 12,  0, 11, 11 ), 0, 0 ));
        
        this.add( gridPanel,    new GridBagConstraints( 0, 0, 1, 1, 1.0, 0.5,
            CENTER, BOTH, new Insets(  0,  0,  0,  0 ), 0, 0 ));
        this.add( newDispPanel, new GridBagConstraints( 0, 1, 1, 1, 1.0, 0.5,
            CENTER, BOTH, new Insets( 11,  0,  0,  0 ), 0, 0 ));
        
    }
    
    public int getGridStep() {
        return (Integer)gridStepE.getValue();
    }

    public int getDefaultWidth() {
        return (Integer)defWidthE.getValue();
    }

    public int getDefaultHeight() {
        return (Integer)defHeightE.getValue();
    }
    
    public Color getDefaultBgColor() {
        return (Color)defBgColorE.getSelectedItem();
    }
    
}
