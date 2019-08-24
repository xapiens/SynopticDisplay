// (c) 2001-2010 Fermi Research Alliance
// $Id: RecentDisplays.java,v 1.2 2010/09/15 15:53:51 apetrov Exp $
package gov.fnal.controls.applications.syndi.repository;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:53:51 $
 */
public class RecentDisplays {

    private static final int MAX_SIZE = 10;

    private static final String PREF_KEY = "display.uri";

    private static final Logger log = Logger.getLogger( RecentDisplays.class.getName());

    private static boolean shouldAccept( DisplaySource disp ) {
        if (!(disp instanceof DisplayURISource)) {
            return false;
        }
        String name = disp.getSimpleName();
        return !name.startsWith( "_" );
    }

    private static DisplaySource toDisplaySource( String str ) {
        try {
            return new DisplayURISource( new URI( str ));
        } catch (Exception ex) {
            //log.warning( "Cannot restore recent display address: " + str );
            log.log( Level.WARNING, "Cannot restore recent display address: " + str, ex );
            return null;
        }
    }

    private static String toString( DisplaySource disp ) {
        if (!(disp instanceof DisplayURISource)) {
            return null;
        }
        return ((DisplayURISource)disp).getSource().toString();
    }

    private final Preferences prefs = Preferences.userNodeForPackage( getClass());
    private final List<DisplaySource> displays = new LinkedList<DisplaySource>();
    private final Map<DisplayHandler,HandlerEntry> handlers = new HashMap<DisplayHandler,HandlerEntry>();
    private final String name;

    public RecentDisplays( String name ) {
        this.name = name;
        load();
    }

    private String createKeyName( int index ) {
        return name + "." + PREF_KEY + "." + index;
    }

    private void load() {
        int i = 0;
        String str;
        while ((str = prefs.get( createKeyName( i++ ), null )) != null) {
            DisplaySource<?> disp = toDisplaySource( str );
            if (disp != null) {
                displays.add( disp );
            }
        }
    }

    public void save() {
        int i = 0;
        for (DisplaySource disp : displays) {
            String str = toString( disp );
            if (str != null) {
                prefs.put( createKeyName( i++ ), str );
            }
        }
    }

    public synchronized void put( DisplaySource disp ) {
        if (!shouldAccept( disp )) {
            return;
        }
        int i = displays.indexOf( disp );
        if (i != -1) {
            bringToTop( i );
        } else {
            append( disp );
        }
    }

    public synchronized void register( DisplayHandler handler ) {
        HandlerEntry entry = new HandlerEntry( handler );
        handlers.put( handler, entry );
        fill( entry );
    }

    public synchronized void unregister( DisplayHandler handler ) {
        handlers.remove( handler );
    }

    public void showDialog( DisplayHandler handler ) {
        List<DisplaySource> displaysCopy;
        synchronized (this) {
            displaysCopy = new ArrayList<DisplaySource>( displays );
        }
        if (displaysCopy.size() == 0) {
            return;
        }
        RecentDisplayDialog dialog = new RecentDisplayDialog( displays, handler );
        dialog.setVisible( true );
    }

    private void bringToTop( int index ) {
        DisplaySource disp = displays.remove( index );
        displays.add( 0, disp );
        for (HandlerEntry e : handlers.values()) {
            bringToTop( e, index );
        }
    }

    private void bringToTop( HandlerEntry entry, int index ) {
        if (entry.menu == null) {
            return;
        }
        JMenuItem item = (JMenuItem)entry.menu.getMenuComponent( index );
        entry.menu.remove( item );
        entry.menu.insert( item, 0 );
    }

    private void append( DisplaySource disp ) {
        boolean trim = displays.size() == MAX_SIZE;
        displays.add( 0, disp );
        if (trim) {
            displays.remove( MAX_SIZE );
        }
        for (HandlerEntry e : handlers.values()) {
            append( e, disp, trim );
        }
    }

    private void append( HandlerEntry entry, DisplaySource disp, boolean trim ) {
        if (entry.menu == null) {
            return;
        }
        Action a = new RecentDisplayAction( entry.handler, disp );
        entry.menu.insert( a, 0 );
        if (trim) {
            entry.menu.remove( MAX_SIZE );
        }
    }

    private void fill( HandlerEntry entry ) {
        if (entry.menu == null) {
            return;
        }
        for (DisplaySource disp : displays) {
            Action a = new RecentDisplayAction( entry.handler, disp );
            entry.menu.add( a );
        }
    }

    private static class HandlerEntry {

        final DisplayHandler handler;
        final JMenu menu;

        HandlerEntry( DisplayHandler handler ) {
            this.handler = handler;
            this.menu = handler.getRecentDisplaysMenu();
        }

    }

}
