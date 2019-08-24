// (c) 2001-2010 Fermi Research Allaince
// $Id: RuntimeControl.java,v 1.2 2010/09/15 15:25:14 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime;

import java.awt.Color;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:25:14 $
 */
public interface RuntimeControl extends RuntimeComponent {

    static final Color DISABLED_CROSS_COLOR = Color.GRAY;

    void setSettingEnabled( boolean settingEnabled );

}
