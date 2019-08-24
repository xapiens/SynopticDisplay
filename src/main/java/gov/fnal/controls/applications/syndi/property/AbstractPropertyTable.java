//  (c) 2001-2010 Fermi Research Alliance
//  $Id: AbstractPropertyTable.java,v 1.1 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public abstract class AbstractPropertyTable extends JTable {

    public static final Color GRID_COLOR        = Color.LIGHT_GRAY;
    public static final Color BACKGROUND_COLOR  = Color.WHITE;
    public static final Color HIGHLIGHT_COLOR   = Color.RED;

    private static final int VERTICAL_CELL_MARGIN = 3;

    private static final Logger log = Logger.getLogger( AbstractPropertyTable.class.getName());

    private final Map<Class<? extends ComponentProperty>,TableCellEditor> editors =
            new HashMap<Class<? extends ComponentProperty>,TableCellEditor>();
    private final Map<Class<? extends ComponentProperty>,TableCellRenderer> renderers =
            new HashMap<Class<? extends ComponentProperty>,TableCellRenderer>();

    protected AbstractPropertyTable() {
        setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        setCellSelectionEnabled( false );
        setRowSelectionAllowed( true );
        setColumnSelectionAllowed( false );

        FontMetrics fm = getFontMetrics( getFont());
        Dimension rowSize = new Dimension( 1,
            fm.getMaxAscent() + fm.getMaxDescent() + VERTICAL_CELL_MARGIN * 2 );
        setRowHeight( rowSize.height );

        JTableHeader header = getTableHeader();
        header.setReorderingAllowed( false );
        header.setMinimumSize( rowSize );
        header.setPreferredSize( rowSize );

        setGridColor( GRID_COLOR );
        setBackground( BACKGROUND_COLOR );
    }

    protected TableCellEditor getEditorFor( ComponentProperty prop ) {
        synchronized (editors) {
            Class<? extends ComponentProperty> clazz = prop.getClass();
            TableCellEditor editor = editors.get( clazz );
            if (editor == null) {
                editor = createEditorFor( prop );
                editors.put( clazz, editor );
            }
            return editor;
        }
    }

    private TableCellEditor createEditorFor( ComponentProperty<?> prop ) {
        Class<? extends TableCellEditor> clazz = prop.getEditorImpl();
        try {
            return clazz.newInstance();
        } catch (Throwable ex) {
            log.log( Level.WARNING, "Cannot create property editor", ex );
            return new StringPropertyEditor();
        }
    }

    protected TableCellRenderer getRendererFor( ComponentProperty prop ) {
        synchronized (renderers) {
            Class<? extends ComponentProperty> clazz = prop.getClass();
            TableCellRenderer renderer = renderers.get( clazz );
            if (renderer == null) {
                renderer = createRendererFor( prop );
                renderers.put( clazz, renderer );
            }
            return renderer;
        }
    }

    private TableCellRenderer createRendererFor( ComponentProperty<?> prop ) {
        Class<? extends TableCellRenderer> clazz = prop.getRendererImpl();
        try {
            return clazz.newInstance();
        } catch (Throwable ex) {
            log.log( Level.WARNING, "Cannot create property renderer", ex );
            return new StringPropertyRenderer();
        }
    }

}
