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

			//Create a new row for the pingHeatMap
			HashMap<SilverCloudNode, Integer> colEntry = new HashMap<SilverCloudNode, Integer>();
			//Put the new row in the pingHeatMap
			pingHeatMap.put(rowNode, colEntry);
			
			for (SilverCloudNode colNode : sc.getScNodes()) {

				//Put a new entry into the current row of the pingHeatMap
				colEntry.put(new SilverCloudNode(colNode), Integer.valueOf(-1)); // The default meaningless value of -1 is the default and means there is no real value for the ping heat.
				
				logger.debug("New column Entry: " + colNode.toString());
				
				logger.debug("Putting Node " + rowNode.getNodeName() + ", " + colNode.getNodeName() + " in PingHeatMap" + 
						" -- col, row: (" + colCount + ", " + rowCount + ") ");
				
				colCount++; //Increment the column number
			}
			//Add the fully filled column to the pingHeatmap
			pingHeatMap.put(new SilverCloudNode(rowNode), colEntry);

			logger.debug("Current row of NODE: " + rowNode.getNodeName() + " IN the pingHeatMap: " + this.pingHeatMap.get(rowNode).toString());
			
			rowCount++; //Increment the column number
			colCount=0; //Start a new column and set it to index 0
		}
		
	}
	
	// Change to private access when the class is working properly
	public HashMap<SilverCloudNode, HashMap<SilverCloudNode,Integer>> getPingHeatmap() {
		return pingHeatMap;
	}
	
	public Integer readPingHeat(SilverCloudNode rowNode, SilverCloudNode colNode) {
		//HashMap<SilverCloudNode, Integer> row = pingHeatMap.get(rowNode);
		//logger.debug("pingHeatMap contains node: " + rowNode.getNodeName() + ", yes? " + pingHeatMap.containsKey(rowNode));		
		//logger.debug("##################@@@@@@@@@@@@@@@@@@@@@@ Value of rownode: " + row.toString());

		//Integer heat = pingHeatMap.get(rowNode).get(colNode);
		//logger.debug("pingHeat of nodes " + rowNode.getNodeName() + ", " + rowNode.getNodeName() + ": " + heat);	
		return pingHeatMap.get(rowNode).get(colNode);
	}
	
	public void setPingHeat(SilverCloudNode rowNode, SilverCloudNode colNode, Integer heat) {
		this.pingHeatMap.get(rowNode).put(colNode, heat);
	}
	
}
