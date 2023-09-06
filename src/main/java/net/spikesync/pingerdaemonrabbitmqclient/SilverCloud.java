package net.spikesync.pingerdaemonrabbitmqclient;

import java.util.HashMap;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

public class SilverCloud {

    private static final Logger logger = LoggerFactory.getLogger(PingerdaemonRabbitmqClientApplication.class);

	private ArrayList<SilverCloudNode> scNodes;

	public SilverCloud(HashMap<String,String> scN) {
		
		this.scNodes = new ArrayList<SilverCloudNode>();
			scN.forEach((key, value) -> {
				scNodes.add(new SilverCloudNode(key,value));
				logger.info("Created SilverCloudeNode: (" + key + ", " + value + "), and added to ArrayList scNodes");
				});
		}
	
	public ArrayList<SilverCloudNode> getScNodes() { return this.scNodes; }
}
