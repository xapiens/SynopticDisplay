// (c) 2001-2010 Fermi Research Alliance
// $Id: AbstractButton.java,v 1.2 2010/09/15 15:48:22 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

import gov.fnal.controls.applications.syndi.runtime.RuntimeComponent;
import gov.fnal.controls.applications.syndi.runtime.RuntimeComponentSupport;
import gov.fnal.controls.applications.syndi.runtime.RuntimeControl;
import gov.fnal.controls.tools.timed.TimedNumber;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:48:22 $
 */
public abstract class AbstractButton extends JButton implements RuntimeControl {

    private static final Border BORDER_OFF = new BevelBorder( BevelBorder.RAISED );
    private static final Border BORDER_ON = new BevelBorder( BevelBorder.LOWERED );

    static final int CROSS_INDENT = 2;

    private final RuntimeComponentSupport sup = new RuntimeComponentSupport();

    private boolean settingEnabled = true;
    private boolean state;

    public AbstractButton() {
        setFocusable( false );
        setFocusPainted( false );
        setRolloverEnabled( false );
        setContentAreaFilled( false );
        setMargin( new Insets( 1, 1, 1, 1 ));
        updateView();
    }

    @Override
    public void setInput( int index, RuntimeComponent comp, int reverseIndex ) {
        sup.setInput( index, comp, reverseIndex );
    }

    @Override
    public RuntimeComponent getInput( int index ) {
        return sup.getInput( index );
    }

    @Override
    public void setOutput( int index, RuntimeComponent comp, int reverseIndex ) {
        sup.setOutput( index, comp, reverseIndex );
    }

    @Override
    public RuntimeComponent getOutput( int index ) {
        return sup.getOutput( index );
    }

    @Override
    public boolean doesSetting() {
        return false;
    }

    public void deliver( TimedNumber data ) {
        sup.deliver( data );
    }

    public void setState( boolean state ) {
        synchronized (this) {
            if (state == this.state) {
                return;
            }
            this.state = state;
        }
        stateChanged( state );
    }

    public boolean getState() {
        return state;
    }

    public void updateView() {
        stateChanged( state );
    }

    protected void stateChanged( boolean state ) {}

    @Override
    public void setSettingEnabled( boolean settingEnabled ) {
        settingEnabled |= !sup.doesSetting(); // always enabled if no actual settings is done
        if (this.settingEnabled == settingEnabled) {
            return;
        }
        this.settingEnabled = settingEnabled;
        repaint();
    }

    public boolean isSettingEnabled() {
        return settingEnabled;
    }

    @Override
    public void paint( Graphics g ) {
        super.paint( g );
        if (!settingEnabled) {
            g.setColor( DISABLED_CROSS_COLOR );
            int x0 = CROSS_INDENT;
            int y0 = CROSS_INDENT;
            int x1 = getWidth() - CROSS_INDENT - 1;
            int y1 = getHeight() - CROSS_INDENT - 1;
            g.drawLine( x0, y0, x1, y1 );
            g.drawLine( x0, y1, x1, y0 );
        }
    }

    @Override
    public boolean isBorderPainted() {
        return true;
    }

    @Override
    protected void paintBorder( Graphics g ) {
        Border border = state ? BORDER_ON : BORDER_OFF;
        border.paintBorder( this, g, 0, 0, getWidth(), getHeight());
    }

    protected static boolean isValidButton( MouseEvent e ) {
        return e.getButton() == MouseEvent.BUTTON1;
    }

}
