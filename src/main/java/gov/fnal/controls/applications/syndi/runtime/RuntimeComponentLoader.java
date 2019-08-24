// (c) 2001-2010 Fermi Research Allaince
// $Id: RuntimeComponentLoader.java,v 1.2 2010/09/15 15:25:14 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime;

import java.io.Reader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:25:14 $
 */
public class RuntimeComponentLoader {

    private static final RuntimeComponentLoader instance = new RuntimeComponentLoader();

    public static RuntimeComponentLoader getInstance() {
        return instance;
    }

    private final DocumentBuilder parser;

    private RuntimeComponentLoader() throws RuntimeException {
        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        fac.setNamespaceAware( true );
        fac.setValidating( false );
        try {
            parser = fac.newDocumentBuilder();
        } catch (Exception ex) {
            throw new RuntimeException( ex );
        }
    }

    public synchronized Document load( Reader reader ) throws Exception {
        parser.reset();
        return parser.parse( new InputSource( reader ));
    }

}
