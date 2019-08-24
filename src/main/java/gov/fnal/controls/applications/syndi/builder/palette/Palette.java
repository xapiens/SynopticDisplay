// (c) 2001-2010 Fermi Research Allaince
// $Id: Palette.java,v 1.2 2010/09/15 16:01:12 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.palette;

import java.awt.Component;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:01:12 $
 */
public class Palette extends JTree {

    private static final String DEFAULT_ICON =
            "/gov/fnal/controls/applications/syndi/builder/resources/component.gif";
    
    private static final Logger log = Logger.getLogger( Palette.class.getName());

    private static TreeNode root;
    
    public Palette() {
        super( new DefaultTreeModel( getRoot()));
        setCellRenderer( new PaletteRenderer());
        getSelectionModel().setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
        setRootVisible( false );
        setShowsRootHandles( true );
        ToolTipManager.sharedInstance().registerComponent( this );
    }

    private static synchronized TreeNode getRoot() {
        if (root == null) {
            try {
                root = new PaletteLoader().load();
            } catch (Exception ex) {
                log.log( Level.SEVERE, "Cannot load component palette", ex );
                return new PaletteDirNode();
            }
        }
        return root;
    }

    private class PaletteRenderer extends DefaultTreeCellRenderer {

        private final Icon defaultIcon;

        PaletteRenderer() {
            URL url= getClass().getResource( DEFAULT_ICON );
            defaultIcon = (url == null) ? null : new ImageIcon( url );
        }

        @Override
        public Component getTreeCellRendererComponent( JTree tree, 
                Object value, boolean selected, boolean expanded, boolean leaf, 
                int row, boolean hasFocus ) {
            PaletteNode node = (PaletteNode)value;
            super.getTreeCellRendererComponent( tree, node.getName(), 
                    selected, expanded, leaf, row, hasFocus );
            if (leaf) {
                Icon icon = node.getIcon();
                setIcon( icon == null ? defaultIcon : icon );
                setToolTipText( node.getDescription());
            }
            return this;
        }
        
    }
    
}
