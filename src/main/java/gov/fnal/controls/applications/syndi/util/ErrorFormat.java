// (c) 2001-2010 Fermi Research Allaince
//  $Id: ErrorFormat.java,v 1.2 2010/09/15 15:19:09 apetrov Exp $
package gov.fnal.controls.applications.syndi.util;

import gov.fnal.controls.tools.timed.TimedError;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:19:09 $
 */
public class ErrorFormat extends Format implements FormatConstants {
    
    private final String prefix, suffix;

    public ErrorFormat() {
        this.prefix = "";
        this.suffix = "";
    }

    public ErrorFormat( String prefix, String suffix ) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public ErrorFormat( DecimalFormat format ) {
        this.prefix = normalize( format.getPositivePrefix());
        this.suffix = normalize( format.getPositiveSuffix());
    }

    private static String normalize( String str ) {
        if (str == null) {
            return "";
        }
        return str.replace( "\\@", "" );
    }

    @Override
    public StringBuffer format( Object obj, StringBuffer buf, FieldPosition pos ) {
        if (!(obj instanceof TimedError)) {
            throw new IllegalArgumentException();
        }
        TimedError err = (TimedError)obj;
        pos.setBeginIndex( 0 );
        pos.setEndIndex( 0 );
        buf.append( prefix );
        buf.append( BAD_PREFIX );
        buf.append( err.getFacilityCode());
        buf.append( ' ' );
        buf.append( err.getErrorNumber());
        buf.append( BAD_SUFFIX );
        buf.append( suffix );
        return buf;
    }

    @Override
    public Object parseObject( String source, ParsePosition pos ) {
        throw new UnsupportedOperationException( "Parsing not supported" );
    }

}
