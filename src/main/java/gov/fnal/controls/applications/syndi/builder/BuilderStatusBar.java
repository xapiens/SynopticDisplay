// (c) 2001-2010 Fermi Research Allaince
// $Id: BuilderStatusBar.java,v 1.2 2010/09/15 15:59:56 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:59:56 $
 */
class BuilderStatusBar extends JPanel {
    
    private static final int ITEM_MIN_WIDTH = 60;

    private final StatusItem message = new StatusItem( JLabel.LEFT );
    private final StatusItem x = new StatusItem( JLabel.CENTER );
    private final StatusItem y = new StatusItem( JLabel.CENTER );
    private final StatusItem zoom = new StatusItem( JLabel.CENTER );

    BuilderStatusBar() {
        super( new GridBagLayout());
        add( message,    new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets( 1, 6, 1, 6 ), 0, 0 ));
        add( new Line(), new GridBagConstraints( 1, 0, 1, 1, 0.0, 1.0,
            GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
            new Insets( 0, 0, 0, 0 ), 0, 0 ));
        add( x,          new GridBagConstraints( 2, 0, 1, 1, 0.0, 1.0,
            GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, 
            new Insets( 1, 6, 1, 6 ), 0, 0 ));
        add( new Line(), new GridBagConstraints( 3, 0, 1, 1, 0.0, 1.0,
            GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
            new Insets( 0, 0, 0, 0 ), 0, 0 ));
        add( y,          new GridBagConstraints( 4, 0, 1, 1, 0.0, 1.0,
            GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
            new Insets( 1, 6, 1, 6 ), 0, 0 ));
        add( new Line(), new GridBagConstraints( 5, 0, 1, 1, 0.0, 1.0,
            GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
            new Insets( 0, 0, 0, 0 ), 0, 0 ));
        add( zoom,       new GridBagConstraints( 6, 0, 1, 1, 0.0, 1.0,
            GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, 
            new Insets( 1, 6, 1, 6 ), 0, 0 ));
    }

    public void setMessage( String val ) {
        message.setText( val );
    }

    public void setPoint( Point val )  {
        if (val == null) {
            x.setText( null );
            y.setText( null );
        } else {
            x.setText( "X=" + val.x );
            y.setText( "Y=" + val.y );
        }
    }
    
    public void setZoom( float val ) {
        String str;
        if (val > 1.0f) {
            str = String.format( "%.0f : 1", val );
        } else {
            str = String.format( "1 : %.0f" , 1.0f / val );
        }
        zoom.setText( str );
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
