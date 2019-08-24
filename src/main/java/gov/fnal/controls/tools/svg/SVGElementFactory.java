package gov.fnal.controls.tools.svg;

import gov.fnal.controls.tools.resource.ClassIterator;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SVGElementFactory {

    private static final Logger log = Logger.getLogger( SVGElementFactory.class.getName());

    private final Map<String,Class<? extends SVGElement>> map =
            new HashMap<String,Class<? extends SVGElement>>();

    public SVGElementFactory() {
        String dir = getClass().getPackage().getName().replace( '.', '/' );
        try {
            ClassIterator<SVGElement> z = new ClassIterator<SVGElement>(
                dir,
                SVGElement.class,
                SVGTag.class
            );
            while (z.hasNext()) {
                Class<? extends SVGElement> clazz = z.next();
                SVGTag anno = clazz.getAnnotation( SVGTag.class );
                map.put( anno.value(), clazz );
            }
        } catch (IOException ex) {
            throw new RuntimeException( ex );
        }
    }

    public SVGElement createComponent( String tag ) {
        Class<? extends SVGElement> clazz = map.get( tag );
        if (clazz != null) {
            try {
                return clazz.newInstance();
            } catch (Exception ex) {
                log.log( Level.SEVERE, "Component creation failed", ex );
            }
        }
        log.fine( "Unknown component '" + tag + "'" );
        return null;
    }

}
