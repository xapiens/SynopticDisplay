//  (c) 2001-2010 Fermi Research Alliance
//  $Id: Synoptic.java,v 1.6 2010/09/15 15:15:16 apetrov Exp $
package gov.fnal.controls.applications.syndi;

import gov.fnal.controls.applications.syndi.builder.Builder;
import gov.fnal.controls.applications.syndi.runtime.Viewer;
import gov.fnal.controls.applications.syndi.util.SplashScreen;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:15:16 $
 */
public class Synoptic {

    protected Synoptic() {
        try {
            URL u = Synoptic.class.getResource( "config.xml" );
            if (u == null) {
                throw new FileNotFoundException();
            }
            SynopticConfig.getInstance().load( u );
        } catch (IOException ex) {
            throw new Error( "Cannot load base configuration file", ex );
        }
    }

    public void openViewer( String displayUri ) {
        Viewer viewer;
        SplashScreen splash = new SplashScreen();
        try {
            viewer = createViewer();
            if (displayUri != null) {
                viewer.openDisplay( URI.create( displayUri ));
            }
        } finally {
            splash.dispose();
        }
        if (displayUri == null) {
            viewer.showRecentDisplayDialog();
        }
    }
    
    public void openBuilder( String displayUri ) {
        Builder builder;
        SplashScreen splash = new SplashScreen();
        try {
            builder = createBuilder();
            if (displayUri != null) {
                builder.openDisplay( URI.create( displayUri ));
            }
        } finally {
            splash.dispose();
        }
    }

    public void showHelp() {
        System.out.println(
            "\n" +
            "SYNOPTIC -- http://synoptic.fnal.gov\n" +
            "\n" +
            "2001-2010 (c) Fermilab\n" +
            "\n" +
            "Authors: Andrey Petrov <apetrov@fnal.gov>,\n" +
            "         Timofei Bolshakov <tbolsh@fnal.gov>\n" +
            "\n" +
            "Usage:\n" +
            "    Synoptic [<mode>] [<file-uri>]\n" +
            "\n" +
            "Modes:\n" +
            "    -v or --viewer       Open display viewer (default)\n" +
            "    -b or --builder      Open display builder\n" +
            "    -h or --help         Show this message\n" +
            "\n" +
            "The second argument may specify a display to open. The URI starts\n" +
            "with either 'file:/' or 'repo:/' to distinguish between a local\n" +
            "file system and the central repository.\n"
        );
        System.exit( 0 );
    }

    protected Viewer createViewer() {
        return new Viewer( true );
    }

    protected Builder createBuilder() {
        return new Builder();
    }

    protected void processArguments( String[] args ) {
        String mode = null;
        String file = null;
        for (String s : args) {
            if (s == null) {
                continue;
            }
            if (s.startsWith( "-" )) {
                if (mode != null || file != null) {
                    mode = "-h";
                    break;
                }
                mode = s;
            } else {
                if (file != null) {
                    mode = "-h";
                    break;
                }
                file = s;
            }
        }
        if (mode == null || "-v".equals( mode ) || "--viewer".equals( mode )) {
            openViewer( file );
        } else if ("-b".equals( mode ) || "--builder".equals( mode )) {
            openBuilder( file );
        } else {
            showHelp();
        }
    }

    public static void main( String[] args ) {
        new Synoptic().processArguments( args );
    }
    
}
