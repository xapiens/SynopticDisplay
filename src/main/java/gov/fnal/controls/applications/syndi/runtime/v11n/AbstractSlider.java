// (c) 2001-2010 Fermi Research Alliance
// $Id: AbstractSlider.java,v 1.2 2010/09/15 15:48:22 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.applications.syndi.runtime.RuntimeComponent;
import gov.fnal.controls.applications.syndi.runtime.RuntimeComponentSupport;
import gov.fnal.controls.applications.syndi.runtime.RuntimeControl;
import gov.fnal.controls.applications.syndi.util.DecimalFormatFactory;
import gov.fnal.controls.tools.timed.TimedDouble;
import gov.fnal.controls.tools.timed.TimedNumber;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Hashtable;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.w3c.dom.Element;

/**
 *
 * @author Timofei Bolshakov, Andrey Petrov
 * @version $Date: 2010/09/15 15:48:22 $
 */
public abstract class AbstractSlider extends JPanel
        implements RuntimeComponent, RuntimeControl, ChangeListener, ActionListener {

    private static final double DEFAULT_MIN_VALUE = 0.0;
    private static final double DEFAULT_MAX_VALUE = 1.0;
    
    private static final int DEFAULT_LABEL_COUNT = 3;

    private static final int SEND_DELAY = 300; // ms

    private final RuntimeComponentSupport sup = new RuntimeComponentSupport();
    private final Timer timer;

    protected final JSlider slider = new JSlider();
    protected final JLabel display = new JLabel();

    private double minValue, maxValue;
    private double lastValue;
    private String dataTag;
    private DecimalFormat labelFormat, displayFormat;
    private int scaleSize, labelCount;
    private boolean mute = false;

    public AbstractSlider() {
        super( new GridBagLayout());
        display.setOpaque( false );
        slider.setOpaque( false );
        slider.setPaintTicks( true );
        slider.addChangeListener( this );
        timer = new Timer( SEND_DELAY, this );
        timer.setCoalesce( true );
        timer.setRepeats( false );
        setOpaque( false );
    }

    @Override
    public final void init( Element source ) throws Exception {
        PropertyCollection props = PropertyCollection.create( source );
        VisualComponent.handleStandartProps( props, this );
        init( props );
    }

    protected void init( PropertyCollection props ) throws Exception {

        slider.setFont( getFont());
        slider.setForeground( getForeground());

        display.setFont( getFont());
        display.setForeground( getForeground());

        minValue = props.getValue( Double.class, "min", DEFAULT_MIN_VALUE );
        maxValue = props.getValue( Double.class, "max", DEFAULT_MAX_VALUE );

        boolean inverted;
        if (minValue < maxValue) {
            inverted = false;
        } else if (minValue > maxValue) {
            inverted = true;
            double vv = minValue;
            minValue = maxValue;
            maxValue = vv;
        } else {
            inverted = false;
            minValue = DEFAULT_MIN_VALUE;
            maxValue = DEFAULT_MAX_VALUE;
        }

        dataTag = props.getValue( String.class, "tag" );

        String str = props.getValue( String.class, "format" );
        labelFormat = (str == null) ? null : DecimalFormatFactory.createFormat( str );
        displayFormat = DecimalFormatFactory.createFormat( str );

        labelCount = props.getValue( Integer.class, "numLabels", DEFAULT_LABEL_COUNT );
        if (labelCount < 0) {
            labelCount = 0;
        }
        if (labelCount > 16) {
            labelCount = 16;
        }
        scaleSize = (labelCount + 1) * 1000;

        synchronized (this) {
            mute = true;
            try {
                slider.setMinimum( 0 );
                slider.setMaximum( scaleSize );
            } finally {
                mute = false;
            }
        }

        slider.setInverted( inverted );

        int dv = scaleSize / (labelCount + 1);
        slider.setMajorTickSpacing( dv );
        slider.setMinorTickSpacing( dv / 5 );

        Hashtable<Integer,JLabel> labels = new Hashtable<Integer,JLabel>();
        if (labelFormat != null) {
            labels.put( 0, new ScaleLabel( minValue ));
            labels.put( scaleSize, new ScaleLabel( maxValue ));
            double step = (maxValue - minValue) / (labelCount + 1);
            double val = minValue + step;
            for (int i = 0; i < labelCount; i++) {
                labels.put( valueToPosition( val ), new ScaleLabel( val ));
                val += step;
            }
            slider.setLabelTable( labels );
            slider.setPaintLabels( true );
        } else {
            slider.setPaintLabels( false );
        }

        double defaultValue = (maxValue + minValue) / 2;
        setValue( defaultValue );
    }

    private double positionToValue( int position ) {
        double q = (double)position / scaleSize;
        return q * (maxValue - minValue) + minValue;
    }

    private int valueToPosition( double value ) {
        double q = (value - minValue) / (maxValue - minValue);
        return (int)Math.round( q * scaleSize );
    }

    @Override
    public void stateChanged( ChangeEvent e ) {
        double val = getValue();
        display.setText( displayFormat.format( val ));
        if (!mute) {
            timer.restart();
        }
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        double val = getValue();
        synchronized (this) {
            if (val == lastValue) {
                return;
            }
            lastValue = val;
        }
        sup.deliver( new TimedDouble( val ));
    }

    @Override
    public void setInput( int index, RuntimeComponent comp, int reverseIndex ) {
        sup.setInput( index, comp, reverseIndex );
    }

    @Override
    public RuntimeComponent getInput( int index ) {
        return sup.getInput( index );
    }

    @Override
    public void setOutput( int index, RuntimeComponent comp, int reverseIndex ) {
        sup.setOutput( index, comp, reverseIndex );
    }

    @Override
    public RuntimeComponent getOutput( int index ) {
        return sup.getOutput( index );
    }

    @Override
    public String getDataTag( int outIndex ) {
        return dataTag;
    }

    @Override
    public boolean doesSetting() {
        return false;
    }

    @Override
    public void offer( TimedNumber data, int inputIndex ) {
        double val = data.doubleValue();
        if (!Double.isNaN( val )) {
            if (val < minValue) {
                val = minValue;
            }
            if (val > maxValue) {
                val = maxValue;
            }
            setValue( val );
        }
    }

    private double getValue() {
        return positionToValue( slider.getValue());
    }

    private synchronized void setValue( double val ) {
        mute = true;
        timer.stop();
        try {
            lastValue = val;
            slider.setValue( valueToPosition( val ));
        } finally {
            mute = false;
        }
    }

    @Override
    public void setSettingEnabled( boolean settingEnabled ) {
        slider.setEnabled( !sup.doesSetting() || settingEnabled );
        repaint();
    }

    @Override
    public void paint( Graphics g ) {
        super.paint( g );
        if (!slider.isEnabled()) {
            g.setColor( DISABLED_CROSS_COLOR );
            g.drawLine( 0, 0, getWidth() - 1, getHeight() - 1 );
            g.drawLine( 0, getHeight() - 1, getWidth() - 1, 0 );
        }
    }
    class ScaleLabel extends JLabel {

        ScaleLabel( double value ) {
            super( labelFormat.format( value ));
            setFont( AbstractSlider.this.getFont());
            setForeground( AbstractSlider.this.getForeground());
            setOpaque( false );
            setHorizontalAlignment( SwingConstants.LEFT );
        }

    }

}
