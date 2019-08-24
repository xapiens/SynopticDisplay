// (c) 2001-2010 Fermi Research Allaince
// $Id: PaletteNode.java,v 1.2 2010/09/15 16:01:12 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.palette;

import gov.fnal.controls.applications.syndi.builder.element.BuilderComponent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.Icon;
import javax.swing.tree.TreeNode;

/**
 * 
 * @author Andrey Petrov, Timofei Bolshakov
 * @version $Date: 2010/09/15 16:01:12 $
 */
public abstract class PaletteNode implements TreeNode, Comparable<PaletteNode> {
    
    private final List<PaletteNode> items = new ArrayList<PaletteNode>();
    private final String name, desc;
    private final boolean leaf;
    private final Icon icon;
    
    private PaletteNode parent = null;
    
    protected PaletteNode( String name, String desc, Icon icon, boolean leaf ) {
        if (name == null) {
            throw new NullPointerException();
        }
        this.name = name;
        this.desc = desc;
        this.icon = icon;
        this.leaf = leaf;
    }
    
    void add( PaletteNode child ) {
        if (child.parent != null) {
            throw new IllegalStateException();
        }
        child.parent = this;
        int i = 0;
        while (i < items.size() && child.compareTo( items.get( i )) > 0) {
            i++;
        }
        items.add( i, child );
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return desc;
    }
    
    public Icon getIcon() {
        return icon;
    }
    
    public abstract BuilderComponent createBuilderComponent() throws Exception;
    
    @Override
    public Enumeration<PaletteNode> children() {
        return new NodeEnumeration();
    }
    
    @Override
    public boolean getAllowsChildren() {
        return !leaf;
    }

    @Override
    public PaletteNode getChildAt( int idx ) {
        return items.get( idx );
    }

    public PaletteNode getChildByName( String name ) {
        for (PaletteNode n : items) {
            if (n.getName().equals( name )) {
                return n;
            }
        }
        return null;
    }

    @Override
    public int getChildCount() {
        return items.size();
    }

    @Override
    public int getIndex( TreeNode node ) {
        return items.indexOf( node );
    }
    
    @Override
    public TreeNode getParent() {
        return parent;
    }

    @Override
    public boolean isLeaf() {
        return leaf;
    }
    
    // This function is used to visually sort components in a tree. 
    // The case of component names is ignored. Note that the 'equals' function
    // uses case-sensitive comparison.
    @Override
    public int compareTo( PaletteNode node  ) {
        if (this.leaf && !node.leaf) {
            return Integer.MAX_VALUE;
        } else if (!this.leaf && node.leaf) {
            return Integer.MIN_VALUE;
        } else {
            return this.name.compareToIgnoreCase( node.name );
        }
    }

    @Override
    public boolean equals( Object obj ) {
        return (obj instanceof PaletteNode)
                && ((PaletteNode)obj).name.equals( this.name )
                && ((PaletteNode)obj).leaf == this.leaf;
    }

    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode() ^ (leaf ? 0 : 0xffffffff);
    }
    
    class NodeEnumeration implements Enumeration<PaletteNode> {

        private int idx = 0;

        @Override
        public boolean hasMoreElements() {
            return idx < items.size();
        }

        @Override
        public PaletteNode nextElement() {
            return items.get( idx++ );
        }
        
    }

}
