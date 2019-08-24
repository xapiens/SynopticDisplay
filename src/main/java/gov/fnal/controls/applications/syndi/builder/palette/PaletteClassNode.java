// (c) 2001-2010 Fermi Research Allaince
// $Id: PaletteClassNode.java,v 1.2 2010/09/15 16:01:12 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.palette;

import gov.fnal.controls.applications.syndi.builder.element.AbstractComponent;
import gov.fnal.controls.applications.syndi.builder.element.BuilderComponent;
import gov.fnal.controls.applications.syndi.builder.element.BuilderComponentFactory;
import gov.fnal.controls.applications.syndi.builder.element.pin.PinCollection;
import gov.fnal.controls.applications.syndi.builder.element.pin.PinType;
import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.applications.syndi.property.ComponentProperty;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.applications.syndi.runtime.DisplayFormatException;
import gov.fnal.controls.applications.syndi.runtime.RuntimeComponent;
import java.awt.geom.Point2D;
import javax.swing.Icon;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:01:12 $
 */
class PaletteClassNode extends PaletteNode {
    
    private static String emptyToNull( String val ) {
        if ("".equals( val )) {
            return null;
        }
        return val;
    }
    
    private final Class<? extends RuntimeComponent> comp;

    PaletteClassNode( String name, String description, Icon icon,
            Class<? extends RuntimeComponent> comp ) {
        super( name, description, icon, true );
        this.comp = comp;
    }

    @Override
    public BuilderComponent createBuilderComponent() throws Exception {
        
        DisplayElement element_attr = comp.getAnnotation( DisplayElement.class );
        if (element_attr == null) {
            throw new DisplayFormatException( "Not a display component" );
        }

        AbstractComponent res = (AbstractComponent)BuilderComponentFactory.createComponent(
            null, // ID
            emptyToNull( element_attr.name()),
            emptyToNull( element_attr.designTimeView()),
            comp.getName()
        );

        PinCollection pins = res.pins();

        for (Pin pin_attr : element_attr.outputs()) {
            gov.fnal.controls.applications.syndi.builder.element.pin.Pin p = pins.addPin(
                pin_attr.number(),
                PinType.OUTPUT,
                emptyToNull( pin_attr.name())
            );
            p.setLocation( new Point2D.Double( pin_attr.x(), pin_attr.y()));
        }

        for (Pin pin_attr : element_attr.inputs()) {
            gov.fnal.controls.applications.syndi.builder.element.pin.Pin p = pins.addPin(
                pin_attr.number(),
                PinType.INPUT,
                emptyToNull( pin_attr.name())
            );
            p.setLocation( new Point2D.Double( pin_attr.x(), pin_attr.y()));
        }

        pins.setMinInputCount( element_attr.minInputs());
        pins.setMaxInputCount( element_attr.maxInputs());
        pins.setMinOutputCount( element_attr.minOutputs());
        pins.setMaxOutputCount( element_attr.maxOutputs());

        PropertyCollection props = new PropertyCollection();
        for (Property prop_attr : element_attr.properties()) {
            ComponentProperty<?> p = ComponentProperty.create( prop_attr );
            props.add( p );
        }
        res.setProperties( props );

        res.setHelpUrl( emptyToNull( element_attr.helpUrl()));

        res.setDescription( emptyToNull( element_attr.description()));

        return res;

    }

    @Override
    public String toString() {
        return "ClassNode[name=" + getName() + ";class=" + comp.getName() + "]";
    }

}
