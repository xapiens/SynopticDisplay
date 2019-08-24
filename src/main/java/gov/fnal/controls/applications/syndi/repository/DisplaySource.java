// (c) 2001-2010 Fermi Research Alliance
// $Id: DisplaySource.java,v 1.2 2010/09/15 15:53:51 apetrov Exp $
package gov.fnal.controls.applications.syndi.repository;

import gov.fnal.controls.applications.syndi.builder.element.GenericContainer;
import java.io.File;
import org.w3c.dom.Document;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:53:51 $
 */
public interface DisplaySource<T> {

    T getSource();

    String getSimpleName();

    File getLocalFile();

    Document loadDocument() throws Exception;

    GenericContainer createBuilderComponent() throws Exception;

}
