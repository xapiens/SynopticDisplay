// (c) 2001-2010 Fermi Research Allaince
//  $Id: DecimalFormatFactory.java,v 1.2 2010/09/15 15:19:09 apetrov Exp $
package gov.fnal.controls.applications.syndi.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:19:09 $
 */
public class DecimalFormatFactory implements FormatConstants {

    public static final String DEFAULT_FORMAT = "0.###";
    public static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;

    private static final Logger log = Logger.getLogger( DecimalFormatFactory.class.getName());

    public static DecimalFormat createFormat( String pattern ) {
        if (pattern == null) {
            return createDefaultFormat();
        }
        if (!hasPatternSymbols( pattern )) {
            return new StaticFormat( pattern );
        }
        try {
            DecimalFormat f = new DecimalFormat( pattern, new Symbols());
            f.setRoundingMode( DEFAULT_ROUNDING_MODE );
            String p = f.toPattern();
            boolean multipart = false, escape = false;
            for (int i = 0, n = p.length(); !multipart && i < n; i++) {
                char c = p.charAt( i );
                if (c == '\'') {
                    escape = !escape;
                } else if (!escape && c == ';') {
                    multipart = true;
                }
            }
            if (!multipart) {
                f.setNegativePrefix( f.getPositivePrefix() + "-" );
            }
            return f;
        } catch (Exception ex) {
            log.log( Level.WARNING, "Invalid decimal format: " + pattern, ex );
            return createDefaultFormat();
        }
    }

    private static DecimalFormat createDefaultFormat() {
        DecimalFormat f = new DecimalFormat( DEFAULT_FORMAT );
        f.setRoundingMode( DEFAULT_ROUNDING_MODE );
        return f;
    }

    public static String createPlaceholder( DecimalFormat f ) {
        if (f instanceof StaticFormat) {
            return f.toPattern();
        }
        StringBuilder res = new StringBuilder();
        String p = f.toPattern();
        boolean escape = false;
        for (int i = 0, n = p.length(); i < n; ++i) {
            char c = p.charAt( i );
            if (c == '\'') {
                escape = !escape;
            } else if (escape) {
                res.append( c );
            } else if (c == ';') {
                break;
            } else if (isPatternSymbol( c )) {
                res.append( PLACEHOLDER );
            } else {
                res.append( c );
            }
        }
        return res.toString();
    }

    private static boolean hasPatternSymbols( String p ) {
        boolean escape = false;
        for (int i = 0, n = p.length(); i < n; ++i) {
            char c = p.charAt( i );
            if (c == '\'') {
                escape = !escape;
            } else if (!escape && isPatternSymbol( c )) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPatternSymbol( char c ) {
        return c == '#' || c == '0';
    }

    public static String formatText( DecimalFormat f, String text ) {
        return f.getPositivePrefix() + text + f.getPositiveSuffix();
    }

    private DecimalFormatFactory() {}

    private static class Symbols extends DecimalFormatSymbols {
        
        Symbols() {
            setNaN( NAN );
            setInfinity( INFINITY );
        }
        
    }

    private static class StaticFormat extends DecimalFormat {

        private final String pattern;

        StaticFormat( String pattern ) {
            this.pattern = pattern.replaceAll( "'", "" );
        }

        @Override
        public StringBuffer format( double number, StringBuffer res, FieldPosition pos ) {
            return res.append( pattern );
        }

        @Override
        public StringBuffer format( long number, StringBuffer res, FieldPosition pos ) {
            return res.append( pattern );
        }

        @Override
        public Number parse( String source, ParsePosition parsePosition ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toPattern() {
            return pattern;
        }

    }

}
