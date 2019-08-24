// (c) 2001-2010 Fermi Research Alliance
// $Id: RepositoryView.java,v 1.2 2010/09/15 15:53:52 apetrov Exp $
package gov.fnal.controls.applications.syndi.repository;

import java.io.File;
import java.io.IOException;
import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:53:52 $
 */
public class RepositoryView extends FileSystemView {
    
    public RepositoryView() {}

    @Override
    public String getSystemDisplayName( File f ) {
        return (f.getParent() == null) 
            ? "Central Repository" 
            : f.getName();
    }

    @Override
    public String getSystemTypeDescription( File f ) {
        return "Central Repository";
    }

    @Override
    public Icon getSystemIcon( File f ) {
        return UIManager.getIcon( f.isDirectory() 
                    ? "FileView.directoryIcon" 
                    : "FileView.fileIcon" );
    }
        
    @Override
    public boolean isParent( File dir, File f ) {
        File p = f.getParentFile();
        return (p == null && dir == null) || (p != null && p.equals( dir ));
    }

    @Override
    public File getChild( File dir, String fileName ) {
        return new RepositoryFile( dir, fileName );
    }
    
    @Override
    public File createFileObject( File dir, String fileName ) {
        return new RepositoryFile( dir, fileName );
    }

    @Override
    public File createFileObject( String path ) {
        return new RepositoryFile( path );
    }

    @Override
    public boolean isFileSystem( File f ) {
        return true;
    }

    @Override
    public File createNewFolder( File dir ) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRoot( File f ) {
        return f.getParent() == null;
    }

    @Override
    public boolean isFileSystemRoot( File f ) {
        return f.getParent() == null;
    }

    @Override
    public boolean isDrive( File f ) {
        return false;
    }

    @Override
    public boolean isFloppyDrive( File f ) {
        return false;
    }

    @Override
    public boolean isComputerNode( File f ) {
        return false;
    }

    @Override
    public File[] getRoots() {
        return RepositoryFile.listRoots();
    }

    @Override
    public File getHomeDirectory() {
        return new RepositoryFile( "" );
    }

    @Override
    public File getDefaultDirectory() {
        return getHomeDirectory();
    }

    @Override
    public File[] getFiles( File dir, boolean useFileHiding ) {
        return dir.listFiles();
    }

    @Override
    public File getParentDirectory( File dir ) {
        return dir == null ? null : dir.getParentFile();
    }
    
    @Override
    protected File createFileSystemRoot( File f ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean isTraversable( File f ) {
        return f.isDirectory();
    }
    
}
