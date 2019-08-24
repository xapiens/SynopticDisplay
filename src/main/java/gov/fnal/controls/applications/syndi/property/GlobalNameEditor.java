//  (c) 2001-2010 Fermi Research Alliance
//  $Id: GlobalNameEditor.java,v 1.1 2010/09/13 21:16:53 apetrov Exp $
package gov.fnal.controls.applications.syndi.property;

import gov.fnal.controls.applications.syndi.repository.DisplayAddressSyntax;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/13 21:16:53 $
 */
public class GlobalNameEditor extends RegexPropertyEditor {

    public GlobalNameEditor() {
        super( DisplayAddressSyntax.NAME_PATTERN );
    }

}
