// (c) 2001-2010 Fermi Research Allaince
// $Id: ImageFileFilter.java,v 1.2 2010/09/15 15:59:56 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:59:56 $
 */
public class ImageFileFilter extends FileFilter {
    
    public ImageFileFilter() {}

    @Override
    public boolean accept( File file ) {
        if (file.isDirectory()) {
            return true;
        }
        String str = file.getName().toLowerCase();
        return str.endsWith( ".png" )
            || str.endsWith( ".gif" )
            || str.endsWith( ".jpg" )
            || str.endsWith( ".jpeg" );
    }

    @Override
    public String getDescription() {
        return "All Image Files (*.png, *.gif, *.jpg, *.jpeg)";
    }

}
