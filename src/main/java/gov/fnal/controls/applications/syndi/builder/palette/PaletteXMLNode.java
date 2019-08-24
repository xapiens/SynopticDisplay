// (c) 2001-2010 Fermi Research Allaince
// $Id: PaletteXMLNode.java,v 1.2 2010/09/15 16:01:12 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.palette;

import gov.fnal.controls.applications.syndi.builder.element.BuilderComponent;
import gov.fnal.controls.applications.syndi.builder.element.BuilderComponentLoader;
import java.io.InputStreamReader;
import java.net.URL;
import javax.swing.Icon;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:01:12 $
 */
class PaletteXMLNode extends PaletteNode {

    private final URL url;
    
    PaletteXMLNode( String name, String description, Icon icon, URL url ) {
        super( name, description, icon, true );
        this.url = url;
    }

    @Override
    public BuilderComponent createBuilderComponent() throws Exception {
        InputStreamReader reader = new InputStreamReader( url.openStream(), "UTF-8" );
        try {
            return BuilderComponentLoader.getInstance().load( reader );
        } finally {
            reader.close();
        }
    }

    @Override
    public String toString() {
        return "XMLNode[name=" + getName() + ";url=" + url + "]";
    }

}
