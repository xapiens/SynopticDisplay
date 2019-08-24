// (c) 2001-2010 Fermi Research Allaince
// $Id: ViewerStatusBar.java,v 1.2 2010/09/15 15:25:15 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:25:15 $
 */
class ViewerStatusBar extends JPanel {
    
    private static final int ITEM_MIN_WIDTH = 60;

    private final StatusItem message = new StatusItem( JLabel.LEFT );
    private final ControlWidgetWrapper widgetWrapper = new ControlWidgetWrapper();

    ViewerStatusBar() {
        super( new GridBagLayout());
        add( message,       new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets( 1, 6, 1, 6 ), 0, 0 ));
        add( new Line(),    new GridBagConstraints( 1, 0, 1, 1, 0.0, 1.0,
            GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
            new Insets( 0, 0, 0, 0 ), 0, 0 ));
        add( widgetWrapper, new GridBagConstraints( 2, 0, 1, 1, 0.0, 1.0,
            GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, 
            new Insets( 0, 0, 0, 0 ), 0, 0 ));
    }

    public void setMessage( String val ) {
        message.setText( val );
    }

    public ControlWidgetWrapper getWidgetWrapper() {
        return widgetWrapper;
    }

    private class StatusItem extends JLabel {

        StatusItem( int alignment ) {
            setHorizontalAlignment( alignment );
        }

        @Override
        public void setText( String text ) {
            super.setText( (text == null) ? "" : text );
        }

        @Override
        public Dimension getMinimumSize() {
            return new Dimension( ITEM_MIN_WIDTH, 0 );
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension( ITEM_MIN_WIDTH, super.getPreferredSize().height );
        }

    }
    
    private class Line extends JSeparator {
        
        Line() {
            super( JSeparator.VERTICAL );
        }
        
    }

}
