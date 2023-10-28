package net.spikesync.pingerdaemonrabbitmqclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

public class PingDaemon { //implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(PingDaemon.class);

	private SilverCloud silverCloud;
	private SilverCloudNode thisNode;
	
	public PingDaemon(SilverCloud siCl) {
		this.silverCloud = siCl;
	}
	
	public PingDaemon(SilverCloudNode thNo, SilverCloud siCl) {
		this.thisNode = thNo;
		this.silverCloud = siCl;
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
	//	pingDaemon.run();

		((AbstractApplicationContext) context).close();
	}

	private void setThisNode(String thisNodeString) {

		this.thisNode = this.silverCloud.getNodeByName(thisNodeString);
		
		if (this.thisNode == null) {
			logger.error("The specified node is not an ACTIVE node!\n"
					+ "----Please specify one of the following nodes or modify the list of active nodes in silvercloud-context.xml:\n ");
			this.silverCloud.getScNodes().forEach((node) -> {
				System.out.println(node + ", ");
			});

			System.exit(1);
		}
		else logger.debug("---------- Specified node in the call to main(String node) exists - and is instantiated as: " + this.thisNode.toString());
	}
/*
	@Override
	public void run() {
		pinger = null;
		Thread pingerThread = null;
		ArrayList<Thread> pingerThreads = new ArrayList<Thread>();

		List<Pinger> pingerList = new LinkedList<Pinger>();
		// For each peer Node build a Pinger object and start a thread for it.
		for (Map.Entry<SilvercloudNode, String> destNode : this.scNodeList.entrySet()) {

			// In the implementation of Pinger.pintDest() information about opening a port
			// on Node itself can give useful information
			pinger = new Pinger(this.scNodeList.get(this.thisNode), destNode.getValue(), thisNode, destNode.getKey());

			pingerList.add(pinger); // A very elaborate - but safe way to obtain the IP address of this node.
			// Note that the assumption that there is only one IP address per node is in
			// effect here (so we only need one <code>for</code> loop).

			logger.debug("Added pinger OrigIp: " + pinger.getOrigIp() + ", DestIp: " + pinger.getDestIp()
					+ ", OrigScNode: " + pinger.getOrigScNode() + ", DestScNode: " + pinger.getDestScNode());
			pingerThread = new Thread(pinger);
			pingerThreads.add(pingerThread);
			pingerThread.start();

		}
	} */
}