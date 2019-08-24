// (c) 2001-2010 Fermi Research Allaince
// $Id: GridAttributes.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import java.util.prefs.Preferences;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public class GridAttributes implements Cloneable {

    private static final int DEFAULT_STEP = 10;
    private static final boolean DEFAULT_VISIBLE = true;
    private static final boolean DEFAULT_ENABLED = true;

    private int step;
    private boolean visible, enabled;
    
    public GridAttributes() {
        this( DEFAULT_STEP, DEFAULT_VISIBLE, DEFAULT_ENABLED );
    }

    public GridAttributes( int step, boolean visible, boolean enabled ) {
        if (step < 1) {
            throw new IllegalArgumentException();
        }
        this.step = step;
        this.visible = visible;
        this.enabled = enabled;
    }
    
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException( ex );
        }
    }
    
    public void setStep( int step ) {
        if (step <= 0) {
            throw new IllegalArgumentException();
        }
        this.step = step;
    }
    
    public int getStep() {
        return step;
    }
    
    public void setVisible( boolean visible ) {
        this.visible = visible;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public void setEnabled( boolean enabled ) {
        this.enabled = enabled;
    }
    
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public int hashCode() {
        return step ^ (visible ? 0xff00 : 0x0000) ^ (enabled ? 0x00ff : 0x0000);
    }
    
    @Override
    public boolean equals( Object obj ) {
        return (obj instanceof GridAttributes)
                && ((GridAttributes)obj).step == this.step
                && ((GridAttributes)obj).enabled == this.enabled
                && ((GridAttributes)obj).visible == this.visible;
    }

    public void save( Preferences prefs ) {
        prefs.putInt( "grid-step", step );
        prefs.putBoolean( "grid-enabled", enabled );
        prefs.putBoolean( "grid-visible", visible );
    }

    public void restore( Preferences prefs ) {
        int newStep = prefs.getInt( "grid-step", DEFAULT_STEP );
        if (newStep <= 0) {
            newStep = DEFAULT_STEP;
        }
        setStep( newStep );
        setVisible( prefs.getBoolean( "grid-visible", DEFAULT_VISIBLE ));
        setEnabled( prefs.getBoolean( "grid-enabled", DEFAULT_ENABLED ));
    }

}
