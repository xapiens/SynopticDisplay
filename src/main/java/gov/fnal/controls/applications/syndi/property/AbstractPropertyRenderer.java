//  (c) 2001-2010 Fermi Research Alliance
//  $Id: AbstractPropertyRenderer.java,v 1.1 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

import java.awt.Color;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public abstract class AbstractPropertyRenderer<T extends JComponent> 
        implements TableCellRenderer {

    public static final Color SELECTION_COLOR = new Color( 0xccccff );
    
    public static final Border PLAIN_BORDER = new EmptyBorder( 1, 3, 1, 3 );

    public static final Border FOCUS_BORDER = new CompoundBorder(
            new LineBorder( new Color( 0x666699 ), 1 ),
            new EmptyBorder( 0, 2, 0, 2 )
    );

    protected final T delegate;

    protected AbstractPropertyRenderer( T delegate ) {
        this.delegate = delegate;
        delegate.setOpaque( true );
    }

    protected T getCellRendererComponent( JComponent owner, boolean selected, boolean focused ) {
        delegate.setFont( owner.getFont());
        delegate.setForeground( owner.getForeground());
        delegate.setBackground( selected ? SELECTION_COLOR : owner.getBackground());
        delegate.setBorder( focused ? FOCUS_BORDER : PLAIN_BORDER );
        return delegate;
    }

}
