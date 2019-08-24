//  (c) 2010 Fermi Research Alliance
//  $Id: Mailer.java,v 1.1 2010/09/15 18:43:05 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author Andrey Petrov
 */
class Mailer {
    
    private static final Logger log = Logger.getLogger( Mailer.class.getName());

    public static final String MAIL_RECIPIENT = System.getProperty( "mail.recipient" );

    public static void sendMessage(
            String dispName,
            CharSequence stackTrace,
            boolean quarantine,
            boolean restart ) {
        
        if (MAIL_RECIPIENT == null) {
            return;
        }

        Formatter fmt = new Formatter( new StringBuilder());
        fmt.format( "Synoptic server on %s has experienced a problem:\n\n", getHostName());
        if (quarantine) {
            fmt.format( "\n\nDisplay '%s' will be sent in quarantine.", dispName );
        }
        if (restart) {
            fmt.format( "\n\nThe server will be automatically restarted." );
        }
        fmt.format( "\n\n*** END OF MESSAGE ***" );

        try {
            MimeMessage mm = new MimeMessage( Session.getInstance( System.getProperties()));
            mm.addRecipients( Message.RecipientType.TO, MAIL_RECIPIENT );
            mm.setSentDate( new Date());
            mm.setSubject( "Synoptic Server Failure" );
            mm.setText( fmt.toString());
            Transport.send( mm );
        } catch (MessagingException ex) {
            log.log( Level.SEVERE, "Cannot send email to the admins", ex );
        }
        
    }

    private static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            return "?";
        }
    }

    private Mailer() {}

}
