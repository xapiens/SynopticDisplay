// (c) 2001-2010 Fermi Research Alliance
// $Id: RepositoryFileView.java,v 1.2 2010/09/15 15:53:51 apetrov Exp $
package gov.fnal.controls.applications.syndi.repository;

import java.io.File;
import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.filechooser.FileView;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:53:51 $
 */
public class RepositoryFileView extends FileView {
    
    public RepositoryFileView() {
    }

    @Override
    public Boolean isTraversable( File f ) {
        return f.isDirectory();
    }

    @Override
    public Icon getIcon( File f ) {
        return UIManager.getIcon( f.isDirectory()
                ? "FileView.directoryIcon" : "FileView.fileIcon" );
    }

    @Override
    public String getName( File f ) {
        return (f.getParent() == null) ? "Central Repository" : f.getName();
    }

}
