package net.spikesync.pingerdaemonrabbitmqclient;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

import net.spikesync.pingerdaemonrabbitmqclient.PingEntry.PINGHEAT;
import net.spikesync.pingerdaemonrabbitmqclient.PingEntry.PINGRESULT;

public class PingMsgReader {

	/* 
	 * Create an error situation in parsePingMessageProperly() to test proper application shutdown after an NPE.
	 */
	int countNoPaPiMePrCalls = 0;
	static int npeAfter = 100;
	
	private static final Logger logger = LoggerFactory.getLogger(PingMsgReader.class);

	CachingConnectionFactory factory;
	Connection connection = null;
	Channel channel = null;
	Queue rabbitMQ = null;

	// PingHeatMap MOET IK TOEVOEGEN ALS BEAN VOOR PingMsgReader! Hier stond eerst SilverCloud, maar daar heb ik geen 
	// methods van nodig, en de nodes staan mogelijk ook niet in de volgorde waarin ze in de heatmap staan.
	private PingHeatMap pingHeatMap;
	private AmqpTemplate amqpTemplate;

	// This constructor is NEW compared to the one in
	// silvercloud-pingermatrix-spring-ajax-integrated!!
	public PingMsgReader(PingHeatMap piHeMa, AmqpTemplate template, CachingConnectionFactory fact, Queue rq) {
		logger.debug(
				"================== Instantiating PingMsgReader with 4 argument constructor!!!! =====================");
		this.pingHeatMap = piHeMa;
		this.amqpTemplate = template;
		this.factory = fact;
		this.rabbitMQ = rq;
	}

	public boolean connectPingMQ() {
		if (this.connection == null) {
			logger.debug("Trying to connect to the Rabbit Message Queue ----------------- ***************");
			try {
				this.connection = this.factory.createConnection();
				this.channel = this.connection.createChannel(false);
				this.channel.queueDeclare(this.rabbitMQ.getName(), false, false, false, null);
				// The test below should be moved to the test class. All the injected
				// dependencies should be checked during testing, not here!
				if (this.amqpTemplate == null) {
					logger.error(
							"Could not instantiate AmqpTemplate in connectPingMQ!! WILL NOT BE ABLE TO READ MESSAGES FROM THE QUEUE!!");
				}

			} catch (Exception e1) {
				e1.printStackTrace();
				logger.error("Failed to connect to RabbitMQ!! Is the RabbitMQ running?");
				return false;
			}
			return true; // this.connection was false, i.e., there was no connection yet, but now there
							// is.
		} else
			return true; // There already was a connection and it can be used.

	}

	public ArrayList<PingEntry> createPingEntriesFromRabbitMqMessages() {

		long nOfWaitingMsgs = 0;

		try {
			this.connection = (Connection) factory.createConnection();
			channel.queueDeclare(this.rabbitMQ.getName(), false, false, false, null);

		} catch (Exception e1) {
			e1.printStackTrace();
			System.exit(0);
		}

		try {
			nOfWaitingMsgs = this.channel.messageCount(this.rabbitMQ.getName());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		logger.debug("Number of waiting messages in Queue: " + nOfWaitingMsgs);

		Object onePingMessage;

		ArrayList<PingEntry> pingEntriesFromRmq = new ArrayList<PingEntry>();

		while (nOfWaitingMsgs-- > 0) {
			onePingMessage = this.amqpTemplate.receiveAndConvert(this.rabbitMQ.getName());
			if (onePingMessage != null) {
				PingEntry newPingEntry = parsePingMessageProperly(onePingMessage.toString());
				/*
				 * Parsing the message from the RMQ should result in a new PingEntry or null if
				 * there is something wrong with the message, e.g., the node in the message
				 * doesn't exists, it is not added to the list.
				 */
				if (newPingEntry != null) {
					pingEntriesFromRmq.add(newPingEntry);
					logger.debug("Parsed Message: " + newPingEntry.toString());
				}
			} else
				logger.debug(
						"Message retrieved in PingMsgReader.updatePingHeatMap() is empty. NOT UPDATING pingHeatMap!");
		}
		return pingEntriesFromRmq;
	}

	/*
	 * Method parsePingMessageProperly dissects the message read from the RMQ. It checks several boundary conditions, e.g.,
	 * the number of tokens that are separated with a semicolon (;). On every occasion the message is uncompliant to 
	 * the required format or, the method returns null, indicating a non-valid or empty message.
	 */
	private PingEntry parsePingMessageProperly(String pingQm) {

		// String mockMsg = "Sat May 23 15:55:05 CEST
		// 2020;WIN219;192.168.1.13;WIN219;192.168.1.13;pingsuccess";
		// tokens[0]: lastPingDate; 
		// tokens[1]: PingOrig (nodeName); 
		// tokens[2]: PingOrig (ipAddress);
		// tokens[3]: pingDest (nodeName);
		// tokens[4]: pingDest (ipAddress);
		// tokens[5]: pingHeat;

		
		 /* If an NPE is thrown in this method, it will exit the thread but not the application, which will malfunction 
		 *  from then on. Force an NPE after a certain number (npeApter) of calls of this method.
		 */	
		//if(countNoPaPiMePrCalls++ > npeAfter) throw new NullPointerException();

		
		logger.debug(" [x] Received '" + pingQm + "'");
		String delims = ";";
		String[] tokens = pingQm.split(delims);
		
		
		
		if (tokens.length != 6) {
			logger.error("Message from RMQ has wrong item number. ABORTING PARSE!!");
			return null; // Error condition. No valid PingEntry object can be constructed.
		} // The token number should be six, otherwise the message can't be parsed!
		else {

			DateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
			Date lastPingDate;
			try {
				lastPingDate = format.parse(tokens[0]);
			} catch (ParseException e) {
				logger.error("Error parsing DATE in message on RMQ. ABORTING Parse!");
				return null; // Error condition. No valid PingEntry object can be constructed.
			}

			Set<String> silverCloudNodes = this.pingHeatMap.getPiHeMaAsNodeNameList();
			
			/*
			 * Both the origin and the destination nodes must be in the known node list. If not so, return null
			 * which prevents a PingEntry to be created. 
			 */
			if( (!silverCloudNodes.contains(tokens[1])) || (!silverCloudNodes.contains(tokens[3])) ) {
				logger.debug("Nodename: " + tokens[1] + "OR" + tokens[3] + " NOT FOUND in SilverCloud Node list!!");
				return null;
			}		
			 else {
				SilverCloudNode origNode = new SilverCloudNode(tokens[1], tokens[2]);
				SilverCloudNode destNode = new SilverCloudNode(tokens[3], tokens[4]);
				PingEntry.PINGRESULT pingEnumResult;
				if (tokens[5].equals("pingsuccess")) {
					pingEnumResult = PINGRESULT.PINGSUCCESS;

				} else
					pingEnumResult = PINGRESULT.PINGFAILURE;

				PingEntry pe = new PingEntry(lastPingDate, origNode, destNode, pingEnumResult, PINGHEAT.UNKNOWN);

				return pe;
			}
		}

	}

}
