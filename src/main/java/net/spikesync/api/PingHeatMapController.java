package net.spikesync.api;

import java.util.Date;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.spikesync.pingerdaemonrabbitmqclient.PingHeatData;
import net.spikesync.pingerdaemonrabbitmqclient.PingHeatMap;
import net.spikesync.pingerdaemonrabbitmqclient.SilverCloudNode;



@RestController
@RequestMapping(path="/", produces="application/json")
@CrossOrigin(origins="http://localhost:8098")
public class PingHeatMapController {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PingHeatMapController.class);

	@Autowired
	private final PingHeatMap pingHeatMap;

	
	public PingHeatMapController(PingHeatMap piHeMa) {
		pingHeatMap = piHeMa;
		
	}
	
	public startUpdatePiHeMa() {
		
	}
	
	@Autowired
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
		SilverCloudNode captNode = new SilverCloudNode("CAPTUW","192.168.50.104");
		SilverCloudNode thorNode = new SilverCloudNode("THORFW","192.168.50.107");
		Date lastCaptThorPing =  pingHeatMap.getLastTimeSuccesfulPing(captNode, thorNode);
		logger.debug("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Date of the last CAPTUW-THORFW successful ping that should be returned is: " + lastCaptThorPing);
		return lastCaptThorPing;
	}
	
	public PingHeatMap getPingHeatMapObject() {
		return this.pingHeatMap;
	}


}
