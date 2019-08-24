// (c) 2001-2010 Fermi Research Allaince
//  $Id: DisplayFilter.java,v 1.2 2010/09/15 15:19:09 apetrov Exp $
package gov.fnal.controls.applications.syndi.util;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:19:09 $
 */
public class DisplayFilter extends FileFilter {

    @Override
    public boolean accept( File pathname ) {
        if (pathname.isDirectory()) {
            return true;
        }
        return pathname.getName().toLowerCase().indexOf( ".xml" ) != -1;
    }

    @Override
    public String getDescription() {
        return "XML files (*.xml)";
    }

}
