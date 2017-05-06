import java.util.Hashtable;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.sun.mail.smtp.SMTPTransport;

import java.security.Security;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Scraper {
    // matches yugioh set IDs (MACR-036) etc
	final static Pattern regex = Pattern.compile(".*([A-Z]{4}[0-9]{3}).*");
	
	public static void main(String[] args) {
	    // schedule checking of page
		ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
		ses.scheduleAtFixedRate(new Runnable() {
		    @Override
		    public void run() {
		        scrape_page(args);
		    }
		}, 0, 1, TimeUnit.HOURS);
	}

	private static void scrape_page(String[] args) {
		String url = args[0];
		String email = args[1];
		String sender = args[2];
		String password = args[3];
        // store desired items in hashtable
		Hashtable<String, Boolean> items = new Hashtable<String, Boolean>();
		for (int i = 4; i < args.length; i++) {
			items.put(args[i], true);
		}
        // total number of desired items
		int count = items.size();
		// read document/url
		Document doc = null;
		try {
			doc = Jsoup.connect(url).get();
			// assume that every item is in stock, then we just check sold-out item tags
			for(Element j: doc.select("a[href].sold-out")) {
			    // get image file name (contains the card set code)
				String card = j.select("img").first().attr("src");
				// checks if there is a match
				Matcher m = regex.matcher(card);
				if (m.matches()) {
					String card_id = m.group(1);
					if(items.containsKey(card_id)) {
					    // checks item as not in stock and decrement number of in-stock desired items
						items.put(card_id, false);
						count -= 1;
					}
				}
			}
			// if not zero then at least one item is in stock
			if(count > 0) email_user(items, email, sender, password);
			else { System.out.println("Nothing Sent"); }
		} catch (Exception e) {e.printStackTrace();}
	}

    /**
     * Sends the list of in-stock item to recipient
     * @param items
     * @param recipient
     * @param sender
     * @param sender_password
     */
	private static void email_user(Hashtable<String, Boolean> items, String recipient, String sender, String sender_password) {
        String to = recipient;
        String body = "";
        for (String s : items.keySet()) {
            // item is still sold out
            if (items.get(s) == true) continue;
            // TODO: add url for each item
            body += s + " is now in stock\n";
        }
        // SMTP stuff
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
        // Get a Properties object
        Properties props = System.getProperties();
        props.setProperty("mail.smtps.host", "smtp.gmail.com");
        props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
        props.setProperty("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.port", "465");
        props.setProperty("mail.smtp.socketFactory.port", "465");
        props.setProperty("mail.smtps.auth", "true");
        props.put("mail.smtps.quitwait", "false");

		Session session = Session.getDefaultInstance(props);

		try {
			MimeMessage message = new MimeMessage(session);
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setFrom(new InternetAddress(sender));
			message.setSubject("Wishlist Item(s) in Stock!");
			message.setText(body);
            SMTPTransport t = (SMTPTransport)session.getTransport("smtps");
            t.connect("smtp.gmail.com", sender, sender_password);
            t.sendMessage(message, message.getAllRecipients());
            t.close();
			System.out.println("Sent message.");
		} catch (Exception e) { e.printStackTrace();}
	}
}
