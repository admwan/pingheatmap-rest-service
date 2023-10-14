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

public class PingMsgReader {

	private static final Logger LOGGER = LoggerFactory.getLogger(PingMsgReader.class);

	CachingConnectionFactory factory;
	Connection connection = null;
	Channel channel = null;
	Queue rabbitMQ = null;

	private SilverCloud silverCloud;
	private AmqpTemplate amqpTemplate;
	// PingHeatMap MOET IK TOEVOEGEN ALS BEAN VOOR PingMsgReader!
	// private PingHeatMap pingHeatMap;

	// This constructor is NEW compared to the one in
	// silvercloud-pingermatrix-spring-ajax-integrated!!
	public PingMsgReader(SilverCloud sc, AmqpTemplate template, CachingConnectionFactory fact, Queue rq) {
		LOGGER.debug(
				"================== Instantiating PingMsgReader with 4 argument constructor!!!! =====================");
		this.silverCloud = sc;
		this.amqpTemplate = template;
		this.factory = fact;
		this.rabbitMQ = rq;
	}

//	public SilverCloud getSilverCloud() {
//		return this.silverCloud;
//	}
//
//	public AmqpTemplate getAmqpTemplate() {
//		return this.amqpTemplate;
//	}
//
	/*
	 * public static void main(String args[]) { ApplicationContext context = new
	 * GenericXmlApplicationContext(
	 * "classpath:META-INF/spring/silvercloud/pingmsgcollector-context.xml");
	 * SilverCloud sc = context.getBean(SilverCloud.class); AmqpTemplate amqp =
	 * context.getBean(AmqpTemplate.class); CachingConnectionFactory cCfact =
	 * context.getBean(CachingConnectionFactory.class); Queue rmq =
	 * context.getBean(Queue.class); PingMsgReader msgR = new
	 * PingMsgReader(sc,amqp,cCfact,rmq); msgR.connectPingMQ(context); }
	 */

	public boolean connectPingMQ() {
		if (this.connection == null) {
			LOGGER.debug("Trying to connect to the Rabbit Message Queue ----------------- ***************");
			try {
				this.connection = this.factory.createConnection();
				this.channel = this.connection.createChannel(false);
				this.channel.queueDeclare(this.rabbitMQ.getName(), false, false, false, null);
				// The test below should be moved to the test class. All the injected
				// dependencies should be checked during testing, not here!
				if (this.amqpTemplate == null) {
					LOGGER.error(
							"Could not instantiate AmqpTemplate in connectPingMQ!! WILL NOT BE ABLE TO READ MESSAGES FROM THE QUEUE!!");
				}

			} catch (Exception e1) {
				e1.printStackTrace();
				LOGGER.error("Failed to connect to RabbitMQ!! Is the RabbitMQ running?");
				return false;
			}
			return true; //this.connection was false, i.e., there was no connection yet, but now there is.
		} 
		else return true; //There already was a connection and it can be used. 
		
	}

	/*
	 * public void connectPingMQ(ApplicationContext context) {
	 * 
	 * CachingConnectionFactory factory =
	 * context.getBean(CachingConnectionFactory.class); connection = null; channel =
	 * null; rabbitMQ = null; try { this.connection = (Connection)
	 * factory.createConnection(); channel = (Channel)
	 * connection.createChannel(false); rabbitMQ = context.getBean(Queue.class);
	 * channel.queueDeclare(rabbitMQ.getName(), false, false, false, null);
	 * 
	 * this.amqpTemplate = context.getBean(AmqpTemplate.class);
	 * 
	 * if (this.amqpTemplate == null) { LOGGER.error(
	 * "Could not instantiate AmqpTemplate in PingMsgReader!! WILL NOT BE ABLE TO READ MESSAGES FROM THE QUEUE!!"
	 * ); }
	 * 
	 * } catch (Exception e1) { e1.printStackTrace(); System.exit(0); } }
	 */

	// In silvercloud-pingermatrix-spring-ajax-integrated this class isn't
	// instantiated as Bean, and all the elements need to be fetched or
	// constructed!!
	// That is soooo UN-Spring and ...
	// In the project pingerdaemon-rabbitmq-client all the necessary dependencies
	// are present when the bean is instantiated.
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

		LOGGER.debug("Number of waiting messages in Queue: " + nOfWaitingMsgs);

		Object onePingMessage;

		ArrayList<PingEntry> pingEntriesFromRmq = new ArrayList<PingEntry>();

		while (nOfWaitingMsgs-- > 0) {
			onePingMessage = this.amqpTemplate.receiveAndConvert(this.rabbitMQ.getName());
			if (onePingMessage != null) {
				PingEntry newPingEntry = parsePingMessageProperly(onePingMessage.toString());
				pingEntriesFromRmq.add(newPingEntry);
				LOGGER.debug("Parsed Message: " + newPingEntry.toString());
			} else
				LOGGER.debug(
						"Message retrieved in PingMsgReader.updatePingHeatMap() is empty. NOT UPDATING pingHeatMap!");
		}
		return pingEntriesFromRmq;
	}

	private PingEntry parsePingMessageProperly(String pingQm) {

		// String mockMsg = "Sat May 23 15:55:05 CEST
		// 2020;WIN219;192.168.1.13;WIN219;192.168.1.13;pingsuccess";
		// tokens[0]: lastPingDate; tokens[1]: PingOrig (nodeName); tokens[3] pingDest;
		// tokens[5]

		LOGGER.debug(" [x] Received '" + pingQm + "'");
		String delims = ";";
		String[] tokens = pingQm.split(delims);

		if (tokens.length != 6) {
			LOGGER.error("Message from RMQ has wrong contents. ABORTING PARSE!!");
			return null; // Error condition. No valid PingEntry object can be constructed.
		} // The token number should be six, otherwise the message can't be parsed!
		else {

			DateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
			Date lastPingDate;
			try {
				lastPingDate = format.parse(tokens[0]);
			} catch (ParseException e) {
				LOGGER.error("Error parsing DATE in message on RMQ. ABORTING Parse!");
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
