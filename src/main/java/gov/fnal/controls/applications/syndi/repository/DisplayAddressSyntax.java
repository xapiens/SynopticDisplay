// (c) 2001-2010 Fermi Research Alliance
// $Id: DisplayAddressSyntax.java,v 1.2 2010/09/15 15:53:52 apetrov Exp $
package gov.fnal.controls.applications.syndi.repository;

import java.util.regex.Pattern;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:53:52 $
 */
public interface DisplayAddressSyntax {

    static final String DEFAULT_SCHEMA = "repo";

    static final Pattern SCHEMA_PATTERN = Pattern.compile(
        "(repo|file)"
    );

    static final Pattern NAME_PATTERN = Pattern.compile(
        "[A-Za-z_][A-Za-z0-9_-]*"
    );

    static final Pattern PATH_PATTERN = Pattern.compile(
        "(?:/.*)*/(" + NAME_PATTERN + ")(?:\\.xml)?"
    );

    static final Pattern KEYVAL_PATTERN = Pattern.compile(
        "\\s*(" + NAME_PATTERN + ")\\s*=\\s*([^=,]*)\\s*"
    );

    static final Pattern PARAM_PATTERN = Pattern.compile(
        "(?:" + KEYVAL_PATTERN + "(?:," + KEYVAL_PATTERN + ")*)?"
    );

    // 1 = schema
    // 2 = path
    // 3 = simple name
    // 4 = parameters
    static final Pattern ADDRESS_PATTERN = Pattern.compile(
        "(?:" + SCHEMA_PATTERN + ":)?" +
        "(" + PATH_PATTERN + ")" +
        "(?:\\((" + PARAM_PATTERN + ")\\))?"
    );

    static final Pattern URL_PARAM_PATTERN = Pattern.compile(
        "%40(" + NAME_PATTERN + ")"
    );

}
