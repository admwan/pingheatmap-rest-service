package net.spikesync.pingerdaemonrabbitmqclient;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.spikesync.pingerdaemonrabbitmqclient.PingEntry;
import net.spikesync.pingerdaemonrabbitmqclient.PingEntry.PINGHEAT;
import net.spikesync.pingerdaemonrabbitmqclient.PingEntry.PINGRESULT;
import net.spikesync.pingerdaemonrabbitmqclient.PingHeatData;

public class PingHeatMap {

	private static final Logger logger = LoggerFactory.getLogger(PingHeatMap.class);

	private HashMap<SilverCloudNode, HashMap<SilverCloudNode, PingHeatData>> pingHeatMap;

	public PingHeatMap(SilverCloud sc) {

		int colCount = 0;
		int rowCount = 0;

		pingHeatMap = new HashMap<SilverCloudNode, HashMap<SilverCloudNode, PingHeatData>>();

		for (SilverCloudNode rowNode : sc.getScNodes()) {

			// Create a new row for the pingHeatMap: this is a new HashMap!!
			HashMap<SilverCloudNode, PingHeatData> colEntry = new HashMap<SilverCloudNode, PingHeatData>();
			// Put the new row in the pingHeatMap
			pingHeatMap.put(rowNode, colEntry);

			for (SilverCloudNode colNode : sc.getScNodes()) {

				// Put a new column entry into the current row of the pingHeatMap, i.e., an
				// entry in the row-HashMap.
				colEntry.put(new SilverCloudNode(colNode), new PingHeatData(PingEntry.PINGHEAT.UNKNOWN)); // The default
																											// meaningless
																											// value of
																											// -1 is the
																											// default
																											// and means
																											// there is
																											// no real
																											// value for
																											// the ping
																											// heat.

				logger.debug("New column Entry: " + colNode.toString());
				logger.debug("Putting Node " + rowNode.getNodeName() + ", " + colNode.getNodeName() + " in PingHeatMap"
						+ " -- col, row: (" + colCount + ", " + rowCount + ") ");
				colCount++; // Increment the column number
			}
			// Add the fully filled column to the pingHeatmap
			pingHeatMap.put(new SilverCloudNode(rowNode), colEntry);
			logger.debug("Current row of NODE: " + rowNode.getNodeName() + " IN the pingHeatMap: "
					+ this.pingHeatMap.get(rowNode).toString());

			rowCount++; // Increment the column number
			colCount = 0; // Start a new column and set it to index 0
		}
	}

	// Change to private access when the class is working properly
	public HashMap<SilverCloudNode, HashMap<SilverCloudNode, PingHeatData>> getPingHeatmap() {
		return pingHeatMap;
	}

	public PingHeatData getPingHeat(SilverCloudNode rowNode, SilverCloudNode colNode) {
		// HashMap<SilverCloudNode, Integer> row = pingHeatMap.get(rowNode);
		// logger.debug("pingHeatMap contains node: " + rowNode.getNodeName() + ", yes?
		// " + pingHeatMap.containsKey(rowNode));
		// logger.debug("##################@@@@@@@@@@@@@@@@@@@@@@ Value of rownode: " +
		// row.toString());

		// Integer heat = pingHeatMap.get(rowNode).get(colNode);
		// logger.debug("pingHeat of nodes " + rowNode.getNodeName() + ", " +
		// rowNode.getNodeName() + ": " + heat);
		return pingHeatMap.get(rowNode).get(colNode);
	}

	public void coolDownPingHeat() {
		for (Entry<SilverCloudNode, HashMap<SilverCloudNode, PingHeatData>> rowNode : pingHeatMap.entrySet()) {
			for (Entry<SilverCloudNode, PingHeatData> colNode : rowNode.getValue().entrySet()) {

				/*
				 * The simplest coolDown is to lower the temperature at each call, but that's
				 * too crude colNode.setValue(new
				 * PingHeatData(PingEntry.getColderHeat(colNode.getValue().getPingHeat()))); //
				 * PINGHEAT is now embedded in PingHeatData, so first construct a new instance
				 * of PingHeatData with the new value of PINGHEAT, and than put the PingHeatData
				 * instance into the column!
				 */
				long currentMillis = new Date().getTime();
				PingHeatData cellHeatData = colNode.getValue();
				long lastSuccessfulPing = -1;
				if(cellHeatData.getLastPingSuccess() != null)
					lastSuccessfulPing = colNode.getValue().getLastPingSuccess().getTime();

				/*
				 * For cooldown to work properly, there has to be at least one ping attempt, and
				 * it should be EITHER successful or failed.
				 */
				if (cellHeatData.getLastPingSuccess() != null) {
					long successfulPingMillis = cellHeatData.getLastPingSuccess().getTime();
					if ((currentMillis - successfulPingMillis > 5000)) {
						colNode.setValue(new PingHeatData(PingEntry.getColderHeat(cellHeatData.getPingHeat())));
						// The successful ping was longer than 3 seconds ago
					}
				}
				// DEBUGGING temperatures

				if (lastSuccessfulPing > 0) {
					logger.debug("Last difference between [successFulPingMillis, currentTime] of node (" + 
				rowNode.getKey().getNodeName() + 
				", " +  colNode.getKey().getNodeName() + "): ["  +  (currentMillis - lastSuccessfulPing)  + "]");
				}
//				if (cellHeatData.getLastPingFailure() == null) {
//					logger.debug("Last Ping FAILURE of node (" + rowNode.getKey().getNodeName() + ", "
//							+ colNode.getKey().getNodeName() + ") is NULL!!! ");
//				}
//
				logger.debug("pingHeat of pair after cool-down: (" + rowNode.getKey().getNodeName() + ", "
						+ colNode.getKey().getNodeName() + "): " + colNode.getValue().getPingHeat());

			}
		}
	}

	public void setPingHeat(SilverCloudNode rowNode, SilverCloudNode colNode, PingHeatData heat) {
		this.pingHeatMap.get(rowNode).put(colNode, heat);
	}

	public void setPingHeat(ArrayList<PingEntry> pingEntries) {
		for (PingEntry pingEntry : pingEntries) {
			SilverCloudNode rowNode = pingEntry.getPingOrig();
			SilverCloudNode colNode = pingEntry.getPingDest();
			PingHeatData currentPingHeat = getPingHeat(rowNode, colNode); // How to get the next warmer value of
																			// PINGHEAT on pinguccess??

			PingHeatData nextPingHeat;

			if (pingEntry.getLastPingResult().equals(PingEntry.PINGRESULT.PINGSUCCESS)) {
				//Date now = new Date();
				nextPingHeat = new PingHeatData(PingEntry.getWarmerHeat(currentPingHeat.getPingHeat()));
				nextPingHeat.setLastPingSuccess(pingEntry.getLastPingDate());
				//In the new PingHeat entry, the lastPingFailure value has to be copied from the old one!!!
				nextPingHeat.setLastPingFailure(getPingHeat(rowNode,colNode).getLastPingFailure());
				setPingHeat(rowNode, colNode, nextPingHeat);
			}

			else if (pingEntry.getLastPingResult().equals(PINGRESULT.PINGFAILURE)) {
				//Date now = new Date();
				nextPingHeat = new PingHeatData(PingEntry.getColderHeat(currentPingHeat.getPingHeat()));
				nextPingHeat.setLastPingFailure(pingEntry.getLastPingDate());
				//In the new PingHeat entry, the lastPingSuccess value has to be copied from the old one!!!
				nextPingHeat.setLastPingSuccess(getPingHeat(rowNode,colNode).getLastPingSuccess());
				setPingHeat(rowNode, colNode, nextPingHeat);

			}
			// If there is no PINGSUCCESS or PINGFAILURE, the status of the pingHeat and all
			// of the dates remain unchanged.

			// logger.info("Set pingheat of (rowNode, colNode): (" + rowNode.getNodeName() +
			// ", " + colNode.getNodeName() +
			// ") to:" + nextPingHeat.getPingHeat());

		}
	}

}
