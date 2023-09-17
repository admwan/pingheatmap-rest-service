package net.spikesync.pingerdaemonrabbitmqclient;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

public class PingMsgReader {

	private static final Logger LOGGER = LoggerFactory.getLogger(PingMsgReader.class);

	CachingConnectionFactory factory;
	Connection connection = null;
	Channel channel = null;
	Queue rabbitMQ = null;

	private SilverCloud silverCloud;
	private AmqpTemplate amqpTemplate;
	// PingHeatMap MOET IK TOEVOEGEN ALS BEAN VOOR PingMsgReader! 
	//private PingHeatMap pingHeatMap;

	
	public PingMsgReader(SilverCloud sc, AmqpTemplate template, CachingConnectionFactory fact, Queue rq) {
		this.silverCloud = sc;
		this.amqpTemplate = template;
		this.factory = fact;
		this.rabbitMQ = rq;
	}
	
	public SilverCloud getSilverCloud() {
		return this.silverCloud;
	}
	
	public AmqpTemplate getAmqpTemplate() {
		return this.amqpTemplate;
	}
	

	public static void main(String args[]) {
		ApplicationContext context = new GenericXmlApplicationContext(
				"classpath:META-INF/spring/silvercloud/pingmsgcollector-context.xml");
		SilverCloud sc = context.getBean(SilverCloud.class);
		AmqpTemplate amqp = context.getBean(AmqpTemplate.class);
		CachingConnectionFactory cCfact = context.getBean(CachingConnectionFactory.class);
		Queue rmq = context.getBean(Queue.class);
		PingMsgReader msgR = new PingMsgReader(sc,amqp,cCfact,rmq);
		msgR.connectPingMQ(context);

	}

	public void connectPingMQ() {
		LOGGER.debug("Trying to connect to the Rabbit Message Queue ----------------- ***************");
		try {
			this.connection = this.factory.createConnection();
			this.channel = this.connection.createChannel(false);
			this.channel.queueDeclare(this.rabbitMQ.getName(), false, false, false, null);
			// The test below should be moved to the test class. All the injected dependencies should be checked during testing, not here!
			if (this.amqpTemplate == null) {
				LOGGER.error(
						"Could not instantiate AmqpTemplate in PingMsgReader!! WILL NOT BE ABLE TO READ MESSAGES FROM THE QUEUE!!");
			}

		} catch (Exception e1) {
			e1.printStackTrace();
			System.exit(0);
		}

	}

	public void connectPingMQ(ApplicationContext context) {

		CachingConnectionFactory factory = context.getBean(CachingConnectionFactory.class);
		connection = null;
		channel = null;
		rabbitMQ = null;
		try {
			this.connection = (Connection) factory.createConnection();
			channel = (Channel) connection.createChannel(false);
			rabbitMQ = context.getBean(Queue.class);
			channel.queueDeclare(rabbitMQ.getName(), false, false, false, null);

			this.amqpTemplate = context.getBean(AmqpTemplate.class);

			if (this.amqpTemplate == null) {
				LOGGER.error(
						"Could not instantiate AmqpTemplate in PingMsgReader!! WILL NOT BE ABLE TO READ MESSAGES FROM THE QUEUE!!");
			}

		} catch (Exception e1) {
			e1.printStackTrace();
			System.exit(0);
		}
	}
/*
	public void updatePingHeatMap(ApplicationContext context) {

		// String mockMsg = "Sat May 23 15:55:05 CEST
		// 2020;WIN219;192.168.1.13;WIN219;192.168.1.13;pingsuccess";
		// tokens[0]: lastPingDate; tokens[1]: PingOrig; tokens[3] pingDest; tokens[5]

		long nOfWaitingMsgs = 0;

		if (this.rabbitMQ == null) {
			CachingConnectionFactory factory = context.getBean(CachingConnectionFactory.class);
			connection = null;
			channel = null;
			rabbitMQ = null;
			vHostQueueName = null;

			try {
				this.connection = (Connection) factory.createConnection();
				channel = (Channel) connection.createChannel(false);
				rabbitMQ = context.getBean(Queue.class);
				vHostQueueName = rabbitMQ.getName();
				channel.queueDeclare(vHostQueueName, false, false, false, null);

			} catch (Exception e1) {
				e1.printStackTrace();
				System.exit(0);
			}
		}

		try {
			nOfWaitingMsgs = this.channel.messageCount(vHostQueueName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LOGGER.debug("Number of waiting messages in Queue: " + nOfWaitingMsgs);

		this.template = context.getBean(AmqpTemplate.class);

		if (this.template == null) {
			LOGGER.error(
					"Could not instantiate AmqpTemplate in PingMsgReader!! WILL NOT BE ABLE TO READ MESSAGES FROM THE QUEUE!!");
		}

		Object onePingMessage;

		if (this.template != null) {

			while (nOfWaitingMsgs-- > 0) {
				onePingMessage = this.template.receiveAndConvert(this.vHostQueueName);
				if (onePingMessage != null) {
					// LOGGER.info("Retrieved message in !!PingMsgReader.updatePingHeatMap()!!: [" +
					// onePingMessage.toString() + "] ");
					parsePingMessage(onePingMessage.toString());
				} else
					LOGGER.info(
							"Message retrieved in PingMsgReader.updatePingHeatMap() is empty. NOT UPDATING pingHeatMap!");
			}
		}
	}
	*/

	/*
	private PingEntry parsePingMessageProperly(String pingQm) {

		PingEntry pingEntry = new PingEntry();

		LOGGER.info(" [x] Received '" + pingQm + "'");
		String delims = ";";
		String[] tokens = pingQm.split(delims);

		if (tokens.length != 6) {
			LOGGER.error("Message from RMQ has wrong contents. ABORTING PARSE!!");
			return null; // Error condition. No valid PingEntry object can be constructed.
		} else {
			PingEntry pe = new PingEntry();

			DateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
			try {
				pe.setLastPingDate(format.parse(tokens[0]));
			} catch (ParseException e) {
				LOGGER.error("Error parsing DATE in message on RMQ. ABORTING Parse!");
				return null; // Error condition. No valid PingEntry object can be constructed.
			}

			try {
				pe.setPingOrig(SilverCloudNode.valueOf(tokens[1]));

			} catch (IllegalArgumentException iae) {
				LOGGER.error("Error parsing ORIGINATOR node in message from RMQ. ABORTING Parse!");
				return null; // Error condition. No valid PingEntry object can be constructed.
			}
			try {
				pe.setPingDest(SilverCloudNode.valueOf(tokens[3]));
			} catch (IllegalArgumentException iae) {
				LOGGER.error("Error parsing DESTINATION node in message from RMQ. ABORTING Parse!");
				return null; // Error condition. No valid PingEntry object can be constructed.
			}
		}
		return pingEntry;
	}
*/


	/*
	 * This method both parses a message from String and writes it into the
	 * PingerMatrix. Split it up into parsing and writing methods.
	 */

	
	/*
	private void parsePingMessage(String pingQM) {

		LOGGER.info(" [x] Received '" + pingQM + "'");
		String delims = ";";
		String[] tokens = pingQM.split(delims);

		if (tokens.length != 6) {
			LOGGER.error("Message from RMQ has wrong contents. ABORTING PARSE!!");
			return;
		} else {
			PingEntry pe = new PingEntry();

			DateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
			try {
				pe.setLastPingDate(format.parse(tokens[0]));
			} catch (ParseException e) {
				LOGGER.error("Error parsing DATE in message on RMQ. ABORTING Parse!");
				return;
			}

			try {
				pe.setPingOrig(SilverCloudNode.valueOf(tokens[1]));

			} catch (IllegalArgumentException iae) {
				LOGGER.error("Error parsing ORIGINATOR node in message from RMQ. ABORTING Parse!");
				return;
			}
			try {
				pe.setPingDest(SilverCloudNode.valueOf(tokens[3]));
			} catch (IllegalArgumentException iae) {
				LOGGER.error("Error parsing DESTINATION node in message from RMQ. ABORTING Parse!");
				return;
			}

			/*
			 * FIELDS of the PingEntry object:
			 * 
			 * SilverCloudNode pingOrig; DO NOT CHANGE! SilverCloudNode pingDest; DO NOT
			 * CHANGE! Date lastPing; CHANGE to the value of the message in the Ping Message
			 * Queue. int lastPingResult; CHANGE to the value of the message in the Ping
			 * Message Queue. int pingHeat; CHANGE according to the 'heat' algorithm
			 * 
			 

			// Determining the success of the ping and the values to update the pingHeatMap
			// with ...
			if (tokens[5].equals("pingsuccess")) {
				pe.setLastPingResult(1);
			} else if (tokens[5].equals("pingfailure")) {
				pe.setLastPingResult(-1);
			}

			else {
				LOGGER.error(
						"Error parsing ping result field. tokens[5] should be one of {\"pingsuccess\", \"pingfailure\"} node in message from RMQ. ABORTING Parse!");
				return;
			}

			HashMap<SilverCloudNode, PingEntry> pingHeatMapRow = this.pingHeatMap.getHeatMapRow(pe.getPingOrig()); // PingOrig
																													// is
																													// the
																													// row
			PingEntry peHeatMapEntry = (PingEntry) pingHeatMapRow.get(pe.getPingDest()); // PingDest is the column

			if (peHeatMapEntry != null) {

				LOGGER.debug("PingEntry retrieved from pingHeatMap: " + peHeatMapEntry.toString());
				// Zet hier de JUnit test bij!!

				peHeatMapEntry.setLastPingDate(pe.getLastPingDate());

				if (pe.getLastPingResult() == -1) { // Immediately change the pinHeat to 0 if the ping is unsuccessful!
					peHeatMapEntry.setPingHeat(0);
					peHeatMapEntry.setLastPingResult(0);
				} else if ((peHeatMapEntry.getPingHeat() + 1 <= 10)) {
					peHeatMapEntry.setPingHeat(peHeatMapEntry.getPingHeat() + 1);
					peHeatMapEntry.setLastPingResult(1);
				}
			} else {
				LOGGER.error("Ping Entry for source: " + pe.getPingOrig() + " and destination: " + pe.getPingDest()
						+ " does not exist! Check if nodes in silvercloud-context.xml exist!!");
			}
		} 
		*/
	}

