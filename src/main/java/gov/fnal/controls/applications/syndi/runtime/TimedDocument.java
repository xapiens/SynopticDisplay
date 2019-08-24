// (c) 2001-2010 Fermi Research Allaince
// $Id: TimedDocument.java,v 1.2 2010/09/15 15:25:14 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime;

import org.w3c.dom.Document;

/**
 * XML document along with its modification time.
 * 
 * @author Andrey Petrov
 * @version $Revision: 1.2 $
 */
public class TimedDocument  {
    
    private final Document doc;
    private final long instant;
    
    public TimedDocument( Document doc, long instant ) {
        this.doc = doc;
        this.instant = instant;
    }
    
    public Document getDocument() {
        return doc;
    }
    
    public long getTime() {
        return instant;
    }
    
}

