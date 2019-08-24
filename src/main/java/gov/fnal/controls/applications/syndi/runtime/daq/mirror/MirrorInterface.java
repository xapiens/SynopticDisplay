//  (c) 2001-2010 Fermi Research Alliance
//  $Id: MirrorInterface.java,v 1.3 2010/09/23 15:05:24 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.daq.mirror;

import gov.fnal.controls.applications.syndi.runtime.daq.AbstractDaqInterface;
import gov.fnal.controls.applications.syndi.runtime.daq.DaqChannel;
import gov.fnal.controls.applications.syndi.runtime.daq.DaqReadingChannel;
import gov.fnal.controls.applications.syndi.runtime.daq.DaqSettingChannel;
import gov.fnal.controls.applications.syndi.runtime.daq.SettingListener;
import gov.fnal.controls.tools.timed.TimedDouble;
import gov.fnal.controls.tools.timed.TimedNumber;
import java.awt.Component;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author  Andrey Petrov
 * @version $Date: 2010/09/23 15:05:24 $
 */
public class MirrorInterface extends AbstractDaqInterface {

    private final Random random = new SecureRandom();
    private final Map<String,Set<DaqReadingChannel>> readings =
            new HashMap<String,Set<DaqReadingChannel>>();
    private final Set<DaqSettingChannel> settings =
            new HashSet<DaqSettingChannel>();

    private boolean settingEnabled;

    MirrorInterface() {}

    @Override
    public Component getControlWidget() {
        return new SimpleControlButton( this );
    }

    @Override
    public boolean isSettingEnabled() {
        return settingEnabled;
    }

    void setSettingEnabled( boolean settingEnabled ) {
        if (settingEnabled == this.settingEnabled) {
            return;
        }
        this.settingEnabled = settingEnabled;
        fireSettingStateChanged( settingEnabled );
    }

    @Override
    protected void startInternal( Set<DaqChannel> channels ) throws Exception {
        Set<DaqReadingChannel> readingChannels = new HashSet<DaqReadingChannel>();
        Set<DaqSettingChannel> settingChannels = new HashSet<DaqSettingChannel>();
        for (DaqChannel c : channels) {
            if (c instanceof DaqReadingChannel) {
                readingChannels.add( (DaqReadingChannel)c );
            }
            if (c instanceof DaqSettingChannel) {
                settingChannels.add( (DaqSettingChannel)c );
            }
        }
        if (!readingChannels.isEmpty()) {
            for (DaqReadingChannel c : readingChannels) {
                String req = c.getDataRequest();
                Set<DaqReadingChannel> rcc = readings.get( req );
                if (rcc == null) {
                    rcc = new HashSet<DaqReadingChannel>();
                    readings.put( req, rcc );
                }
                rcc.add( c );
            }
        }
        if (!settingChannels.isEmpty()) {
            for (DaqSettingChannel c : settingChannels) {
                String req = c.getDataRequest();
                c.setSettingListener( new SettingHandler( req ));
                settings.add( c );
            }
        }

        generateRandomReadings();
    }

    @Override
    protected void stopInternal() {
        for (DaqSettingChannel c : settings) {
            c.setSettingListener( null );
        }
        settings.clear();
        readings.clear();
    }

    private void generateRandomReadings() {
        for (String req : readings.keySet()) {
            double val = random.nextDouble();
            setData( req, new TimedDouble( val ));
        }
    }

    private void setData( String dataRequest, TimedNumber data ) {
        Set<DaqReadingChannel> cc = readings.get( dataRequest );
        if (cc == null) {
            return;
        }
        for (DaqReadingChannel c : cc) {
            c.newData( data );
        }
    }

    private class SettingHandler implements SettingListener {

        final String req;

        SettingHandler( String req ) {
            this.req = req;
        }

        @Override
        public void newData( TimedNumber data ) {
            setData( req, data );
        }

    }

}
