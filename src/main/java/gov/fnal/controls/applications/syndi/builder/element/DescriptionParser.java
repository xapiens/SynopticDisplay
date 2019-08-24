// (c) 2001-2010 Fermi Research Allaince
// $Id: DescriptionParser.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import java.util.Stack;
import org.xml.sax.Attributes;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public class DescriptionParser extends AbstractParser<String> {
    
    private final StringBuilder description = new StringBuilder();
    private final StringBuilder author = new StringBuilder();
    private final StringBuilder copyright = new StringBuilder();
    private final StringBuilder version = new StringBuilder();
    private final StringBuilder helpUrl = new StringBuilder();
    private final Stack<StringBuilder> stack = new Stack<StringBuilder>();
    private final BuilderComponent owner;
    
    private boolean newVersion = true;
    
    public DescriptionParser() {
        this( null );
    }

    public DescriptionParser( BuilderComponent owner ) {
        this.owner = owner;
    }
    
    @Override
    public void startElement( String uri, String name, String qName, Attributes attrs ) {
        if (done) {
            throw new IllegalStateException( "Parsing is completed" );
        }
        if ("desc".equals( name )) {
            stack.push( description );
            newVersion = true;
        } else if ("description".equals( name )) {
            stack.push( description );
            newVersion = false;
        } else if (newVersion) {
            stack.push( null );
        } else if ("author".equals( name ) || "keeper".equals( name )) {
            stack.push( author );
        } else if ("copyright".equals( name )) {
            stack.push( copyright );
        } else if ("version".equals( name )) {
            stack.push( version );
        } else if ("helpURL".equals( name )) {
            stack.push( helpUrl );
        } else if (!stack.isEmpty()) {
            stack.push( stack.peek());
        } else {
            stack.push( null );
        }
    }

    @Override
    public void endElement( String uri, String name, String qName ) {
        if (done) {
            throw new IllegalStateException( "Parsing is completed" );
        }
        stack.pop();
        if (stack.isEmpty()) {
            if ("description".equals( name )) { // old version
                if (description.length() > 0) {
                    description.append( "; " );
                }
                append( "Author: ", author );
                append( "(c) ", copyright );
                append( "Version: ", version );
                append( "Help URL: ", helpUrl );
            }
            result = description.toString();
            if (owner != null) {
                owner.setDescription( result );
            }
            done = true;
        }
    }
    
    @Override
    public void characters( char ch[], int start, int length ) {
        if (done) {
            throw new IllegalStateException( "Parsing is completed" );
        }
        String str = new String( ch, start, length ).trim();
        if (str.length() > 0) {
            StringBuilder buf = stack.peek();
            if (buf != null) {
                if (buf.length() > 0) {
                    buf.append( " " );
                }
                buf.append( str );
            }
        }
    }
    
    private void append( String caption, StringBuilder text ) {
        if (text.length() > 0) {
            description.append( caption );
            description.append( text );
            description.append( "; " );
        }
    }

    @Override
    public int getOriginAdjustmentCount() {
        return 0;
    }

    @Override
    public int getRemovedInvisibleCount() {
        return 0;
    }

}
