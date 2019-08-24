// (c) 2001-2010 Fermi Research Alliance
// $Id: AbstractInput.java,v 1.2 2010/09/15 15:48:22 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

import gov.fnal.controls.applications.syndi.runtime.RuntimeControl;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:48:22 $
 */
public interface AbstractInput extends RuntimeControl {

    static final int TEXT_HORIZONTAL_ALIGNMENT = SwingConstants.RIGHT;

    static final Border BORDER = new InputBorder();

}
