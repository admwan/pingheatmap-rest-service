package net.spikesync.pingerdaemonrabbitmqclient;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

/*
 * The PingDaemon pings all the nodes defined in the SilverCloud Bean (beans.xml). It instantiates a VmPinger object for 
 * each node that is being pinged and puts the result in a PingEntry that, in turn, gets added to an ArrayList
 * of PingEntry's. The PingDeamon's run() method creates a Thread for each VmPinger object and starts it. In the 
 * indefinite loop inside run() the list of PingEntry's of each VmPinger object is retrieved and printed to the log. 
 * TBD replace printing the list of PingEntry's by writing them to the RabbitMQ.
 */

public class PingDaemon implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(PingDaemon.class);

	private SilverCloud silverCloud;
	private PingMsgProducer pingMsgProducer;
	private SilverCloudNode thisNode;
	private HashMap<String, VmPinger> vMpingObjectArray;
	private ArrayList<PingEntry> pingEntriesAllVms;

	public PingDaemon(SilverCloud siCl, PingMsgProducer piMsPr) {
		this.silverCloud = siCl;
		this.pingMsgProducer = piMsPr;
		this.vMpingObjectArray = new HashMap<String, VmPinger>();
		this.pingEntriesAllVms = new ArrayList<PingEntry> ();

	}
	public static void main(String[] args) {

		ApplicationContext context = new GenericXmlApplicationContext("classpath:beans.xml");
		PingDaemon pingDaemon = context.getBean(PingDaemon.class);
		if (args.length == 1) {
			pingDaemon.setThisNode(args[0]);
		} else {
			String nodeNameList = "";
			for (SilverCloudNode scn : pingDaemon.silverCloud.getScNodes()) {
				nodeNameList += scn.getNodeName() + " ";
			}
			System.out.println("Please specify the name of this Silvercloud node as one of these: " + nodeNameList
					+ "\nExiting ...");
			System.exit(1);
		}
		pingDaemon.run();

		((AbstractApplicationContext) context).close();
	}

	private void setThisNode(String thisNodeString) {

		this.thisNode = this.silverCloud.getNodeByName(thisNodeString);

		if (this.thisNode == null) {
			logger.error("The specified node is not an ACTIVE node!\n"
					+ "----Please specify one of the following nodes or modify the list of active nodes in beans.xml:\n ");
			this.silverCloud.getScNodes().forEach((node) -> {
				System.out.println(node + ", ");
			});

			System.exit(1);
		} else
			logger.debug("---------- Specified node in the call to main(String node) exists - and is instantiated as: "
					+ this.thisNode.toString());
	}
	
	@Override
	public void run() {
		/*
		 * Before entering the indefinite ping-loop create Threads for all the VmPinger objects in the
		 * vmPingObjectArray. The Threads are not stored anywhere because they don't need to be managed. The individual
		 * vmPingObjects, however, need to be stored in order to access the PingEntry list they have collected. 
		 */
		
		//Check if the SilverCloudNode's are valid!
		if( (this.thisNode.getIpAddress() == null) || (this.thisNode.getNodeName() == null)) {
			logger.error("The values of this node are invalid!!! Exiting!!");
			System.exit(1);
		}
		silverCloud.getScNodes().forEach(silverCloudNode -> {
			this.vMpingObjectArray.put(silverCloudNode.getNodeName(), new VmPinger(this.thisNode, silverCloudNode));
		});

		this.vMpingObjectArray.forEach((vmPingerNode, vmObject) -> {
			Thread vmObjThread = new Thread(vmObject);
			vmObjThread.start();
			logger.debug("Started VM Pinger Thread for nodes: " + this.thisNode.getNodeName() + ", " + vmPingerNode);
		});

		while (true) {
			this.vMpingObjectArray.forEach((vmPingerNode, vmPingObject) -> {
				logger.debug("List of PingEntry's for nodes: " + this.thisNode.getNodeName() + ", " + vmPingerNode
						+ ": \n" + vmPingObject.getPingEntries());
				this.pingEntriesAllVms.addAll(vmPingObject.getPingEntries());
				vmPingObject.clearPingEntries(); // Don't forget to clear the list of PingEntry's after reading them!!!
				//logger.debug("--------------------------------------------------------------------------------------------------------");
				//logger.debug("Current list of all PingEntry's waiting to be written to the RabbitMQ: "+ this.pingEntriesAllVms.toString());
				this.pingMsgProducer.writePiEnToRabbitmq(this.thisNode, pingEntriesAllVms);
				// Assuming that all entries are written to the RabbitMQ in good order, the list must be cleared.
				// Note that this cannot be done in PingMsgProducer, because that will cause a concurrent access Exception: 
				// The list of PingEntries is being tried to be modified by (several) different Threads, that - of course -
				// causes inconsistencies. 
				logger.info("PingEntry's written to the RabbitMq: " + this.pingEntriesAllVms.toString());
				this.pingEntriesAllVms.clear();
			});

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				logger.error("Thread sleep interrupted!! This really shouldn't happen.");
				e.printStackTrace();
			}
		}
	}
}

