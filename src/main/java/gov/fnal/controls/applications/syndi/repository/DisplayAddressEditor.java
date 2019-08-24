// (c) 2001-2010 Fermi Research Alliance
// $Id: DisplayAddressEditor.java,v 1.2 2010/09/15 15:53:51 apetrov Exp $
package gov.fnal.controls.applications.syndi.repository;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import static java.awt.GridBagConstraints.*;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:53:51 $
 */
public class DisplayAddressEditor extends JPanel implements ActionListener {
    
    private static final String TITLE = "Display Address Editor";

    private static final Dimension DEFAULT_SIZE = new Dimension( 350, 250 );

    private static final Logger log = Logger.getLogger( DisplayAddressEditor.class.getName());

    private final JLabel dispLabel = new JLabel( "Display Name:" );
    private final JTextField disp = new JTextField();
    private final JButton buOk = new OkButton();
    private final JButton buCancel = new CancelButton();
    private final JTable table = new JTable();
    private final JLabel paramLabel = new JLabel( "Parameters:" );
    private final JOptionPane optionPane = new JOptionPane( this, JOptionPane.PLAIN_MESSAGE );
    private final ParameterTableModel model = new ParameterTableModel();
    private final JButton buBrowse = new JButton( "Browse..." );
    private final JButton buAdd = new JButton( "Add" );
    private final JButton buRemove = new JButton( "Remove" );

    private JFileChooser repoDialog;

    public DisplayAddressEditor() {
        
        super( new GridBagLayout());
        
        table.setModel( model );
        table.getTableHeader().setReorderingAllowed( false );
        table.getSelectionModel().setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        table.setColumnSelectionAllowed( false );

        dispLabel.setDisplayedMnemonic( 'D' );
        dispLabel.setLabelFor( disp );

        paramLabel.setDisplayedMnemonic( 'P' );
        paramLabel.setLabelFor( table );

        buBrowse.setDisplayedMnemonicIndex( 0 );
        buAdd.setDisplayedMnemonicIndex( 0 );
        buRemove.setDisplayedMnemonicIndex( 0 );

        JScrollPane scroll = new JScrollPane( table,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        scroll.setPreferredSize( DEFAULT_SIZE );

        add( dispLabel,  new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0,
                WEST, NONE,         new Insets( 0, 6, 6, 6 ), 0, 0 ));
        add( disp,       new GridBagConstraints( 1, 0, 1, 1, 1.0, 0.0,
                CENTER, HORIZONTAL, new Insets( 0, 6, 6, 6 ), 0, 0 ));
        add( buBrowse,     new GridBagConstraints( 2, 0, 1, 1, 0.0, 0.0,
                CENTER, NONE,       new Insets( 0, 6, 6, 6 ), 0, 0 ));
        add( paramLabel, new GridBagConstraints( 0, 1, 3, 1, 0.0, 0.0,
                WEST,   NONE,       new Insets( 6, 6, 0, 6 ), 0, 0 ));
        add( scroll,     new GridBagConstraints( 0, 2, 2, 2, 1.0, 1.0,
                CENTER, BOTH,       new Insets( 6, 6, 6, 6 ), 0, 0 ));
        add( buAdd,        new GridBagConstraints( 2, 2, 1, 1, 0.0, 0.0,
                NORTH,  HORIZONTAL, new Insets( 6, 6, 6, 6 ), 0, 0 ));
        add( buRemove,     new GridBagConstraints( 2, 3, 1, 1, 0.0, 0.0,
                NORTH,  HORIZONTAL, new Insets( 6, 6, 6, 6 ), 0, 0 ));

        buBrowse.addActionListener( this );
        buAdd.addActionListener( this );
        buRemove.addActionListener( this );

        optionPane.setOptions( new Object[]{ buOk, buCancel });

    }

    private void reset() {
        model.clear();
        disp.setText( "" );
    }

    private void setValue( String value ) throws Exception {
        DisplayAddress addr = DisplayAddress.parse( value );
        disp.setText( addr.getPath());
        model.setParameters( addr.getParams());
    }

    private String getValue() throws Exception {
        if (table.isEditing()) {
            TableCellEditor editor = table.getCellEditor();
            if (editor != null) {
                editor.stopCellEditing();
            }
        }
        DisplayAddress addr = DisplayAddress.create(
            null,
            disp.getText().trim(),
            model.getParameters()
        );
        return addr.toString();
    }

    public String showDialog( Component parent ) {
        try {
            return showDialog( parent, null );
        } catch (Exception ex) {
            log.throwing( DisplayAddressEditor.class.getName(), "showDialog", ex );
            return null;
        }
    }

    public String showDialog( Component parent, String defaultValue ) throws Exception {
        JDialog dialog = optionPane.createDialog( parent, TITLE );
        try {
            reset();
            if (defaultValue != null && !defaultValue.trim().isEmpty()) {
                setValue( defaultValue );
            }
            dialog.setVisible( true );
            return (String)optionPane.getValue();
        } finally {
            dialog.dispose();
        }
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        Object obj = e.getSource();
        if (obj == buBrowse) {
            browse();
        } else if (obj == buAdd) {
            addRow();
        } else if (obj == buRemove) {
            removeRow();
        }
    }

    private JFileChooser initRepoDialog() {
        if (repoDialog == null) {
            repoDialog = new JFileChooser( new RepositoryView());
            repoDialog.setFileView( new RepositoryFileView());
            repoDialog.setMultiSelectionEnabled( false );
            repoDialog.setDialogTitle( "Download & Open Display" );
        }
        RepositoryFile.invalidate();
        return repoDialog;
    }

    private void browse() {
        JFileChooser chooser = initRepoDialog();
        if (chooser.showOpenDialog( this ) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        disp.setText( file.getAbsolutePath());
    }

    private void addRow() {
        int row = table.getSelectedRow() + 1;
        model.addRow( row );
        table.setRowSelectionInterval( row, row );
    }

    private void removeRow() {
        int row = table.getSelectedRow();
        if (row == -1) {
            return;
        }
        model.removeRow( row );
        int rowCount = model.getRowCount();
        if (rowCount == 0) {
            return;
        }
        if (row == rowCount) {
            row = rowCount - 1;
        }
        table.setRowSelectionInterval( row, row );
    }

    private class OkButton extends JButton implements ActionListener {

        OkButton() {
            super( "Ok" );
            addActionListener( this );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            try {
                optionPane.setValue( getValue());
                SwingUtilities.getWindowAncestor( optionPane ).dispose();
            } catch (Exception ex) {
                String message = ex.getMessage();
                if (message == null) {
                    message = ex.getClass().getSimpleName();
                }
                JOptionPane.showMessageDialog(
                    DisplayAddressEditor.this,
                    message,
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }

    }

    private class CancelButton extends JButton implements ActionListener {

        CancelButton() {
            super( "Cancel" );
            addActionListener( this );
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            optionPane.setValue( null );
            SwingUtilities.getWindowAncestor( optionPane ).dispose();
        }

    }

}
