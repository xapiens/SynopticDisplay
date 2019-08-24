// (c) 2001-2010 Fermi Research Allaince
// $Id: PinSelectPanel.java,v 1.2 2010/09/15 16:11:48 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element.pin;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:11:48 $
 */
public class PinSelectPanel extends JPanel {

    private final JLabel laInp = new JLabel( "Number of inputs:" );
    private final JLabel laOut = new JLabel( "Number of outputs:" );
    private final JLabel laInpI = new JLabel();
    private final JLabel laOutI = new JLabel();
    private final JSpinner edInp;
    private final JSpinner edOut;
    private final int inpMaxN, inpMinN, outMaxN, outMinN;

    public PinSelectPanel( PinCollection pins ) {
        
        super( new GridBagLayout());
        
        inpMinN = pins.getMinInputCount();
        inpMaxN = pins.getMaxInputCount();
        outMinN = pins.getMinOutputCount();
        outMaxN = pins.getMaxOutputCount();

        edInp = new JSpinner( new SpinnerNumberModel( pins.getInputCount(), inpMinN, inpMaxN, 1 ));
        edOut = new JSpinner( new SpinnerNumberModel( pins.getOutputCount(), outMinN, outMaxN, 1 ));

        String str;

        if (inpMaxN > -1) {
            if (inpMaxN == inpMinN) {
                str = "equal to " + Integer.toString(inpMinN);
                edInp.setValue(new Integer(inpMinN));
                edInp.setEnabled(false);
            } else {
                str = "between " + Integer.toString(inpMinN) + " and "
                        + Integer.toString(inpMaxN);
            }
        } else {
            str = "not less than " + Integer.toString(inpMinN);
        }
        str = "(must be " + str + ")";
        laInpI.setText(str);
        laInpI.setFont(new Font(laInpI.getFont().getName(), Font.PLAIN, laInpI
                .getFont().getSize()));

        if (outMaxN > -1) {
            if (outMaxN == outMinN) {
                str = "equal to " + Integer.toString(outMinN);
                edOut.setValue(new Integer(outMinN));
                edOut.setEnabled(false);
            } else {
                str = "between " + Integer.toString(outMinN) + " and "
                        + Integer.toString(outMaxN);
            }
        } else {
            str = "not less than " + Integer.toString(outMinN);
        }
        
        str = "(must be " + str + ")";
        laOutI.setText(str);
        laOutI.setFont(new Font(laOutI.getFont().getName(), Font.PLAIN, laOutI
                .getFont().getSize()));

        add(laInp, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
                        0, 3, 6), 0, 0));
        add(edInp, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 3, 0), 0, 0));
        add(laInpI, new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
                        0, 12, 0), 0, 0));
        add(laOut, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
                        0, 3, 6), 0, 0));
        add(edOut, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 3, 0), 0, 0));
        add(laOutI, new GridBagConstraints(0, 3, 2, 1, 1.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
                        0, 12, 0), 0, 0));
    }

    public int getInputs() throws NumberFormatException {
        return ((Number)edInp.getValue()).intValue();
    }

    public int getOutputs() throws NumberFormatException {
        return ((Number) edOut.getValue()).intValue();
    }

}
