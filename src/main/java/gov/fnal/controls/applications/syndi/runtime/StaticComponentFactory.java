// (c) 2001-2010 Fermi Research Allaince
// $Id: StaticComponentFactory.java,v 1.2 2010/09/15 15:25:14 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime;

import gov.fnal.controls.applications.syndi.util.SVGUtil;
import gov.fnal.controls.tools.svg.SVGComponentWrapper;
import gov.fnal.controls.tools.svg.SVGSvg;
import gov.fnal.controls.tools.svg.SVGSyntaxHandler;
import java.awt.Component;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import org.w3c.dom.Element;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:25:14 $
 */
class StaticComponentFactory {

    private static final TransformerFactory fac = TransformerFactory.newInstance();

    private final SVGSyntaxHandler parser = new SVGSyntaxHandler( false );
    private final Transformer trx;

    StaticComponentFactory() {
        try {
            trx = fac.newTransformer();
        } catch (TransformerConfigurationException ex) {
            throw new RuntimeException( ex );
        }
    }

    Component createComponent( Element source ) throws DisplayFormatException {
        parser.reset();
        trx.reset();
        try {
            trx.transform( new DOMSource( source ), new SAXResult( parser ));
            SVGSvg svg = (SVGSvg)parser.getResult();
            SVGUtil.forceMonospaceFonts( svg );
            return new SVGComponentWrapper( svg );
        } catch (Exception ex) {
            throw new DisplayFormatException( "Cannot create static component", ex );
        }
    }
    
}
