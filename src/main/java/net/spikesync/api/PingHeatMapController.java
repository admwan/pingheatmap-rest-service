package net.spikesync.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.spikesync.pingerdaemonrabbitmqclient.PingEntry;
import net.spikesync.pingerdaemonrabbitmqclient.PingHeatData;
import net.spikesync.pingerdaemonrabbitmqclient.PingHeatMap;
import net.spikesync.pingerdaemonrabbitmqclient.PingMsgReader;
import net.spikesync.pingerdaemonrabbitmqclient.SilverCloudNode;

@RestController
@RequestMapping(path = "/", produces = "application/json")
@CrossOrigin(origins = "http://localhost:8098")
public class PingHeatMapController {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PingHeatMapController.class);

	@Autowired
	private final PingHeatMap pingHeatMap;
	private final PingMsgReader pingMsgReader;
	private Runnable pingHeMaUpThread;
	private Runnable pingHeMaCooldownThread;

	public PingHeatMapController(PingMsgReader piMeRe, PingHeatMap piHeMa) {
		pingMsgReader = piMeRe;
		pingHeatMap = piHeMa;
		pingHeMaUpThread = new Runnable() {
			@Override
			public void run() {
				readRmqUpdatePiHeMa();
			}
		};
		pingHeMaCooldownThread = new Runnable() {
			@Override
			public void run() {
				cooldownPingHeatMap();
			}
		};
	}

	public void readRmqUpdatePiHeMa() {

		logger.debug("Now starting listener with devPingApp..connectPingMQ(context) --------------------**********");

		// In this project everything needed by PingMsgReader is injected at
		// bean-construction time, so it is ready to be used!
		while (true) {
			boolean connectionEstablished;
			connectionEstablished = this.pingMsgReader.connectPingMQ();
			if (connectionEstablished) {
				ArrayList<PingEntry> newPingEnties = this.pingMsgReader.createPingEntriesFromRabbitMqMessages();
				this.pingHeatMap.setPingHeat(newPingEnties);
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				logger.error(
						"Exception during sleep of Thread running pingMsgReader.createPingEntriesFromRabbitMqMessages() !!");
				e.printStackTrace();
			}

		}
	}

	public void cooldownPingHeatMap() {
		while(true) {
			pingHeatMap.coolDownPingHeat();
			pingHeatMap.printPingHeatMap();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				logger.error(
						"Exception during sleep of Thread running pingHeatMap.coolDownPingHeat() !!");
				e.printStackTrace();
			}

		}
	}

	
	// The following two methods that start the update threads are *automatically* called when creating an instance
	// of this class! How is that possible?
	@Autowired
	@PostMapping("/startupdatepingheatmap")
	public void startUpdatePiHeMa() {
		logger.debug("^^^^^^^^^^^^&&&&&&&&&&&&^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ Starting pingHeatMapUpdateThread ...");
		new Thread(this.pingHeMaUpThread).start();
	}

//	@Autowired
	@PostMapping("/startcooldownpingheatmap")
	public void startCooldownPingHeatMap() {
		logger.debug("^^^^^^^^^^^^############^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ Starting pingHeatMapCooldownThread ...");
		new Thread(this.pingHeMaCooldownThread).start();
	}
	
	public void stopUpdatePiHeMa() {
		/* TBD ...................... */
	}

//	@Autowired
	@PostMapping("/pingheatmap")
	public HashMap<SilverCloudNode, HashMap<SilverCloudNode, PingHeatData>> getPingHeatMap() {
		logger.debug("Now returning pingHeatMap as HashMap from REST API method getPingHeatMap");
		return pingHeatMap.getPingHeatmap();
	}

	@Autowired
	@PostMapping("/stringifiedheatmap")
	public String getStringifiedHeatMap() {
		String returnString = pingHeatMap.getHeatMapAsString();
		logger.debug("Stringified pingHeatMap: " + returnString);
		return returnString;
	}

	@Autowired
	@PostMapping("/lastcaptpingdate")
	public Date getLastCaptPingUpdate() {
		SilverCloudNode captNode = new SilverCloudNode("CAPTUW", "192.168.50.104");
		SilverCloudNode thorNode = new SilverCloudNode("THORFW", "192.168.50.107");
		Date lastCaptThorPing = pingHeatMap.getLastTimeSuccesfulPing(captNode, thorNode);
		logger.debug(
				"@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Date of the last CAPTUW-THORFW successful ping that should be returned is: "
						+ lastCaptThorPing);
		return lastCaptThorPing;
	}

	public PingHeatMap getPingHeatMapObject() {
		return this.pingHeatMap;
	}

}
