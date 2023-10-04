package net.spikesync.pingerdaemonrabbitmqclient;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingHeatMap {

	private static final Logger logger = LoggerFactory.getLogger(PingHeatMap.class);

	private HashMap<SilverCloudNode, HashMap<SilverCloudNode,Integer>> pingHeatMap;
	
	
	public PingHeatMap(SilverCloud sc) {
	
		int colCount = 0;
		int rowCount = 0;
		
		pingHeatMap = new HashMap<SilverCloudNode, HashMap<SilverCloudNode,Integer>>();
	
		for (SilverCloudNode rowNode : sc.getScNodes()) {
			++rowCount;
			for (SilverCloudNode colNode : sc.getScNodes()) {
				++colCount;
				HashMap<SilverCloudNode, Integer> colEntry = new HashMap<SilverCloudNode, Integer>();
				colEntry.put(new SilverCloudNode(colNode), Integer.valueOf(-1)); // The default meaningless value of -1 is the default and means there is no real value for the ping heat.
				
				logger.debug("Putting Node " + rowNode.getNodeName() + ", " + colNode.getNodeName() + " in PingHeatMap" + 
						" -- col, row: (" + colCount + ", " + rowCount + ") ");
				
				pingHeatMap.put(new SilverCloudNode(rowNode), colEntry);
				
			}			
		}
	}
	
	public HashMap<SilverCloudNode, HashMap<SilverCloudNode,Integer>> getPingHeatmap() {
		return pingHeatMap;
	}
	
	public Integer getPingHeat(SilverCloudNode colNode, SilverCloudNode rowNode) {
		HashMap<SilverCloudNode, Integer> row = pingHeatMap.get(rowNode);
		Integer heat = pingHeatMap.get(rowNode).get(colNode);
		logger.debug("pingHeat of nodes " + rowNode.getNodeName() + ", " + colNode.getNodeName() + ": " + heat);
		
		return pingHeatMap.get(colNode).get(rowNode);
	}

}
