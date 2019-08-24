// (c) 2001-2010 Fermi Research Allaince
// $Id: NestedHandler.java,v 1.2 2010/09/15 15:55:51 apetrov Exp $
package gov.fnal.controls.applications.syndi.xml;

import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:55:51 $
 */
public abstract class NestedHandler extends DefaultHandler {

    public abstract boolean isOpen();

}
