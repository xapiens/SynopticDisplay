// (c) 2001-2010 Fermi Research Allaince
// $Id: PaletteDirNode.java,v 1.2 2010/09/15 16:01:12 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.palette;

import gov.fnal.controls.applications.syndi.builder.element.BuilderComponent;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:01:12 $
 */
class PaletteDirNode extends PaletteNode {
    
    PaletteDirNode() {
        this( "" );
    }

    PaletteDirNode( String name ) {
        super( name, null, null, false );
    }
    
    @Override
    public BuilderComponent createBuilderComponent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "DirNode[name=" + getName() + "]";
    }
    
}
