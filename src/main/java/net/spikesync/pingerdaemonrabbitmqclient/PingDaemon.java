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

public class PingDaemon implements Runnable {

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
		pingDaemon.run();

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
		} else
			logger.debug("---------- Specified node in the call to main(String node) exists - and is instantiated as: "
					+ this.thisNode.toString());
	}

	@Override
	public void run() {
		SilverCloudNode captNode = this.silverCloud.getNodeByName("CAPTUW");
		VmPinger vmPinger = new VmPinger(this.thisNode, captNode);
		Thread pingerThread = new Thread(vmPinger);
		pingerThread.start();
		while (true) {
			logger.debug("Current list of PingEntry's after retrieving them from the VmPinger object:\n "
					+ vmPinger.getPingEntries().toString());
			vmPinger.clearPingEntries();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}