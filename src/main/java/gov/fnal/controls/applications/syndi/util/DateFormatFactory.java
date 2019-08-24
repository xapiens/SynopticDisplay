// (c) 2001-2010 Fermi Research Allaince
//  $Id: DateFormatFactory.java,v 1.2 2010/09/15 15:19:09 apetrov Exp $
package gov.fnal.controls.applications.syndi.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:19:09 $
 */
public class DateFormatFactory implements FormatConstants {

    public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final Logger log = Logger.getLogger( DateFormatFactory.class.getName());

    public static DateFormat createFormat( String pattern ) throws IllegalArgumentException {
        try {
            return new SimpleDateFormat( pattern );
        } catch (Exception ex) {
            log.log( Level.WARNING, "Invalid date format: " + pattern, ex );
            return createDefaultFormat();
        }
    }

    public static DateFormat createDefaultFormat() {
        return new SimpleDateFormat( DEFAULT_FORMAT );
    }

    public static String createPlaceholder( DateFormat f ) {
        if (!(f instanceof SimpleDateFormat)) {
            f = createDefaultFormat();
        }
        String pattern = ((SimpleDateFormat)f).toPattern();
        return pattern.replaceAll( "[GyMwWDdFEaHkKhmsSzZ]", PLACEHOLDER ).replaceAll( "[\'\"]", "" );
    }

    private DateFormatFactory() {}

}
