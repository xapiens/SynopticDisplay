// (c) 2001-2010 Fermi Research Allaince
//  $Id: DecimalFormatterFactory.java,v 1.2 2010/09/15 15:19:09 apetrov Exp $
package gov.fnal.controls.applications.syndi.util;

import java.text.DecimalFormat;
import java.text.ParseException;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.text.DefaultFormatter;

/**
 * This component is used to format numeric data in input fields.
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:19:09 $
 */
public class DecimalFormatterFactory extends AbstractFormatterFactory {

    private final DecimalFormat format;
    
    public DecimalFormatterFactory( String pattern ) {
        format = DecimalFormatFactory.createFormat( pattern );
    }

    @Override
    public AbstractFormatter getFormatter( JFormattedTextField field ) {
        return new DecimalFormatter();
    }

    private class DecimalFormatter extends DefaultFormatter {

        @Override
        public Double stringToValue( String str ) throws ParseException {
            Number n = format.parse( str );
            if (n instanceof Double) {
                return (Double)n;
            } else {
                return new Double( n.doubleValue());
            }
        }

        @Override
        public String valueToString( Object value ) {
            Number n = (Number)value;
            if (n == null || Double.isNaN( n.doubleValue())) {
                return ""; // Non-numeric values are not shown in the input fields
            }
            return format.format( value );
        }

        @Override
        public Class<?> getValueClass() {
            return Double.class;
        }

    }

}
