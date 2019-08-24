// (c) 2001-2010 Fermi Research Alliance
// $Id: DisplayXMLSource.java,v 1.2 2010/09/15 15:53:52 apetrov Exp $
package gov.fnal.controls.applications.syndi.repository;

import gov.fnal.controls.applications.syndi.builder.element.BuilderComponent;
import gov.fnal.controls.applications.syndi.builder.element.ComponentParser;
import gov.fnal.controls.applications.syndi.builder.element.GenericContainer;
import gov.fnal.controls.applications.syndi.runtime.DisplayFormatException;
import java.io.File;
import java.util.logging.Logger;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import org.w3c.dom.Document;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:53:52 $
 */
public class DisplayXMLSource implements DisplaySource<Document> {

    private static final Logger log = Logger.getLogger( DisplayXMLSource.class.getName());

    private static final TransformerFactory trxFac = TransformerFactory.newInstance();

    private final Document doc;
    private final String name;

    private Transformer xform;

    public DisplayXMLSource( Document doc, String name ) {
        if (doc == null) {
            throw new NullPointerException();
        }
        this.doc = doc;
        this.name = name;
    }

    @Override
    public Document getSource() {
        return doc;
    }

    @Override
    public String getSimpleName() {
        return name;
    }

    @Override
    public File getLocalFile() {
        return null;
    }

    @Override
    public Document loadDocument() {
        return doc;
    }

    @Override
    public GenericContainer createBuilderComponent() throws Exception {

        ComponentParser syntax = new ComponentParser();
        getTransformer().transform(
            new DOMSource( doc.getDocumentElement()),
            new SAXResult( syntax )
        );

        BuilderComponent comp = syntax.getResult();
        if (!(comp instanceof GenericContainer)) {
            throw new DisplayFormatException( "Invalid component type" );
        }
        
        int originAdjustmentCount = syntax.getOriginAdjustmentCount();
        if (originAdjustmentCount > 0) {
            log.info( "Adjusted origin of " + originAdjustmentCount + " SVG component(s)" );
        }
        int removedInvisibleCount = syntax.getRemovedInvisibleCount();
        if (removedInvisibleCount > 0) {
            log.info( "Removed " + removedInvisibleCount + " invisible SVG component(s)" );
        }

        return (GenericContainer)comp;
    }

    private synchronized Transformer getTransformer() throws Exception {
        if (xform == null) {
            xform = trxFac.newTransformer();
        } else {
            xform.reset();
        }
        return xform;
    }

    @Override
    public String toString() {
        return "DisplayXMLSource";
    }

}
