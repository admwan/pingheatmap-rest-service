package net.spikesync.pingerdaemonrabbitmqclient;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

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

public class PingMsgProducer {

	private static final Logger logger = LoggerFactory.getLogger(PingMsgProducer.class);

	CachingConnectionFactory factory;
	Connection connection = null;
	Channel channel = null;
	Queue rabbitMQ = null;
	ArrayList<PingEntry> pingEntries = null;

	private SilverCloud silverCloud;
	private AmqpTemplate amqpTemplate;
	// PingHeatMap MOET IK TOEVOEGEN ALS BEAN VOOR PingMsgReader!
	// private PingHeatMap pingHeatMap;

	// This constructor is NEW compared to the one in
	// silvercloud-pingermatrix-spring-ajax-integrated!!
	public PingMsgProducer(SilverCloud sc, AmqpTemplate template, CachingConnectionFactory fact, Queue rq) {
		logger.debug(
				"================== Instantiating PingMsgReader with 4 argument constructor!!!! =====================");
		this.silverCloud = sc;
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
			return true; //this.connection was false, i.e., there was no connection yet, but now there is.
		} 
		else return true; //There already was a connection and it can be used. 
		
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
				pingEntriesFromRmq.add(newPingEntry);
				logger.debug("Parsed Message: " + newPingEntry.toString());
			} else
				logger.debug(
						"Message retrieved in PingMsgReader.updatePingHeatMap() is empty. NOT UPDATING pingHeatMap!");
		}
		return pingEntriesFromRmq;
	}

	/*
	 * This message takes a list of PingEntry's, transforms them into a String that is written to the RabbitMQ
	 * in the format specified.
	 * <Date d>;<SilverCloudNode thisNode.getNodeName()>;<SilverCloudNode thisNode.getIPaddress()>;
	 * <SilverCloudNode thatNode.getNodeName()>;<SilverCloudNode thatNode.getIPaddress()><;PINGRESULT.[value]>
	 */
	public void writePiEnToRabbitmq(SilverCloudNode thisNode, ArrayList<PingEntry> piEnLi) {
		/* Debugging for development, remove after finishing up
		logger.debug("********************** PingMsgProducer object for this node: " + thisNode.getNodeName() + " "
				+ "called with Ping Entry destination list size: " + piEnLi.size());
		*/
		piEnLi.forEach(pingentry -> {
			String stringFromPiE = stringifyPingEntry(pingentry);
			/* TBD: put this in a JUnit test!!!! */
			logger.debug("String to be written to RabbitMQ: " + stringFromPiE);
			PingEntry peConvertedBackFromString = parsePingMessageProperly(stringFromPiE);
			logger.debug("Did the round-trip conversion work? " + peConvertedBackFromString.equals(pingentry));
			/**/
			this.amqpTemplate.convertAndSend("SilverSurfieRMQHpingQueue",stringFromPiE);
			//piEnLi.remove(pingentry); Don't remove the PingEntry's here or you'll get a concurrent access Exception!
		});
	}
	
	/*
	 * This method creates a String from a PingEntry. This is more straightforward than parsing them, so this time
	 * all the elements are just concatenated.
	 * Example: [ Mon Oct 30 14:29:37 CET 2023;THORFW;192.168.50.107;HYDRFS;192.168.50.116;pingsuccess ]
	 */
	private String stringifyPingEntry(PingEntry piEn) {
		String pingEntryString = piEn.getLastPingDate() + ";" +
				piEn.getPingOrig().getNodeName() + ";" +
				piEn.getPingOrig().getIpAddress() + ";" +
				piEn.getPingDest().getNodeName() + ";" +
				piEn.getPingDest().getIpAddress() + ";" +
				piEn.getLastPingResult().toString().toLowerCase() + ";";
		return pingEntryString;
				
	}
	
	private PingEntry parsePingMessageProperly(String pingQm) {

		// String mockMsg = "Sat May 23 15:55:05 CEST
		// 2020;WIN219;192.168.1.13;WIN219;192.168.1.13;pingsuccess";
		// tokens[0]: lastPingDate; tokens[1]: PingOrig (nodeName); tokens[3] pingDest;
		// tokens[5]

		logger.debug(" [x] Received '" + pingQm + "'");
		String delims = ";";
		String[] tokens = pingQm.split(delims);

		if (tokens.length != 6) {
			logger.error("Message from RMQ has wrong contents. ABORTING PARSE!!");
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
