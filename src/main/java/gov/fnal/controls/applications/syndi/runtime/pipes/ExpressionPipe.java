// (c) 2001-2010 Fermi Research Alliance
// $Id: ExpressionPipe.java,v 1.3 2010/09/15 16:36:29 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.pipes;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.applications.syndi.runtime.RuntimeComponent;
import gov.fnal.controls.applications.syndi.runtime.DisplayFormatException;
import gov.fnal.controls.tools.expressions.DefaultContext;
import gov.fnal.controls.tools.expressions.Expression;
import gov.fnal.controls.tools.timed.TimedDouble;
import gov.fnal.controls.tools.timed.TimedError;
import gov.fnal.controls.tools.timed.TimedNumber;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Computes an arithmetic expression over one or several input values.
 * 
 * @author  Timofei Bolshakov, Andrey Petrov
 * @version $Date: 2010/09/15 16:36:29 $
 */
@DisplayElement(
    
    name            = "Expression",
    description     = "Computes an arithmetic expression over one or several input values.",
    group           = "Converters",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.PipeComponent",
    helpUrl         = "/Expression",
    
    properties = {
        @Property( caption="Width",        name="width",       value="60",         type=Integer.class ),
        @Property( caption="Height",       name="height",      value="20",         type=Integer.class ),
        @Property( caption="Expression",   name="expression",  value="input"                          ),
        @Property( caption="Data Tag",     name="tag",         value="",           required=false     )
    },

    minInputs = 1,
    maxInputs = 16,
    minOutputs = 1,
    maxOutputs = 64,
    
    inputs = { 
        @Pin( number = 1, x = 0, y = 0.5 )
    }, 
    
    outputs = { 
        @Pin( number = 2, x = 1, y = 0.5 )
    }

)
        
public class ExpressionPipe extends AbstractPipe {
    
    private static final Pattern INPUT_PATTERN = Pattern.compile(
        "^input(\\d{1,2})?$", Pattern.CASE_INSENSITIVE );
    
    private static final Logger log = Logger.getLogger( ExpressionPipe.class.getName());

    private final Context ctx = new Context();
    private final Map<Integer,Handle> inputs = new HashMap<Integer,Handle>();

    private boolean inputsReady;
    private String exprStr;
    private Expression expr;

    public ExpressionPipe() {}
    
    @Override
    protected void init( PropertyCollection props ) throws Exception {
        super.init( props );
        exprStr = props.getValue( String.class, "expression", "input" );
        try {
            expr = new Expression( exprStr );
        } catch (Exception ex) {
            throw new DisplayFormatException( "Invalid expression: " + exprStr, ex );
        }
    }
    
    @Override
    public void setInput( int index, RuntimeComponent comp, int reverseIndex ) {
        inputs.put( index, new Handle( comp, reverseIndex ));
    }

    @Override
    public RuntimeComponent getInput( int index ) {
        Handle h = inputs.get( index );
        return (h == null) ? null : h.comp;
    }

    @Override
    public String getDataTag( int outIndex ) {
        return (dataTag == null) ? exprStr : dataTag;
    }
    
    @Override
    public void offer( TimedNumber data, int inputIndex ) {
        inputs.get( inputIndex ).setValue( data.doubleValue());
        if (!inputsReady) {
            for (Handle h : inputs.values()) {
                if (!h.valueReady) {
                    return;
                }
            }
            inputsReady = true;
        }
        try {
            deliver( new TimedDouble( expr.compute( ctx )));
        } catch (Exception ex) {
            log.log( Level.INFO, "Cannot compute the expression", ex );
            deliver( new TimedError( 0, 0 ));
        }
    }
    
    private class Context extends DefaultContext {

        Context() {}

        @Override
        public double getValue( String name ) {
            Matcher m = INPUT_PATTERN.matcher( name );
            if (m.matches()) {
                String s = m.group( 1 );
                Integer index = new Integer( s == null ? "1" : s );
                if (inputs.containsKey( index )) {
                    return inputs.get( index ).value;
                }
            }
            return Double.NaN;
        }

    }

    private static class Handle {

        final RuntimeComponent comp;
        final int reverseIndex;
        
        private double value = Double.NaN;
        private boolean valueReady = false;

        Handle( RuntimeComponent comp, int reverseIndex ) {
            this.comp = comp;
            this.reverseIndex = reverseIndex;
        }

        void setValue( double value ) {
            this.value = value;
            valueReady = true;
        }

    }


}
