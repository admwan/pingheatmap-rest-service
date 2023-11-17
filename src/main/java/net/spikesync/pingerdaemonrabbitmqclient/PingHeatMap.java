package net.spikesync.pingerdaemonrabbitmqclient;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.spikesync.api.SimplePingHeat;
import net.spikesync.pingerdaemonrabbitmqclient.PingEntry.PINGHEAT;
import net.spikesync.pingerdaemonrabbitmqclient.PingEntry.PINGRESULT;

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
				/*
				 * Put a new column entry into the current row of the pingHeatMap, i.e., an
				 * entry in the row-HashMap. Call PingHeat constructor with the one argument
				 * constructor PINGHEAT.UNKNOWN, the date values are set to null! Test on the
				 * PINGHEAT status te determine ping heat cooldown in that method.
				 */

				colEntry.put(new SilverCloudNode(colNode), new PingHeatData(PingEntry.PINGHEAT.UNKNOWN));
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
		logger.debug("Size of the PingHeatMatrix rows: " + this.pingHeatMap.size() + ", columns: "
				+ this.pingHeatMap.keySet().size());
	}

	public ArrayList<String> getSilverCloudNodeNameList() {
		ArrayList<String> silverCloudNodeNameList = new ArrayList<String>();
		this.pingHeatMap.forEach((key, value) -> {
			silverCloudNodeNameList.add(key.getNodeName());
		});
		return silverCloudNodeNameList;
	}
	
	public HashMap<SilverCloudNode, HashMap<SilverCloudNode, PingHeatData>> getPingHeatmap() {
		logger.debug("**************************&&&&&&&&&&&&&&&&&&&&&&&&&&&&& Now returning pingHeatMap: "
				+ this.pingHeatMap.toString());
		return pingHeatMap;
	}

	// Added a bunch of getters and constructors for debugging. This replaces the
	// getters and setters from
	// PingHeatData. Previously, I only retrieved the PingHeatData object and worked
	// that, now I'm doing it
	// from PingHeatMap itself. This should eliminate a layer on indirection that
	// possibly causes errors.

	public PINGHEAT getPingHeat(SilverCloudNode rowNode, SilverCloudNode colNode) {
		return this.pingHeatMap.get(rowNode).get(colNode).getPingHeat();
	}

	public Date getLastTimeSuccesfulPing(SilverCloudNode rowNode, SilverCloudNode colNode) {
		return this.pingHeatMap.get(rowNode).get(colNode).getLastPingSuccess();
	}

	public Date getLastTimeFailedPing(SilverCloudNode rowNode, SilverCloudNode colNode) {
		return this.pingHeatMap.get(rowNode).get(colNode).getLastPingFailure();
	}

	public void setPingHeat(SilverCloudNode rowNode, SilverCloudNode colNode, PINGHEAT pingHeat) {
		PingHeatData pingHeatData = pingHeatMap.get(rowNode).get(colNode);
		pingHeatData.setPingHeat(pingHeat);
	}

	public void setLastTimePingSucceeded(SilverCloudNode rowNode, SilverCloudNode colNode, Date pingSuccessDate) {
		PingHeatData pingHeatData = pingHeatMap.get(rowNode).get(colNode);
		pingHeatData.setLastPingSuccess(pingSuccessDate);
	}

	public void setLastTimePingFailed(SilverCloudNode rowNode, SilverCloudNode colNode, Date pingFailedDate) {
		PingHeatData pingHeatData = pingHeatMap.get(rowNode).get(colNode);
		pingHeatData.setLastPingSuccess(pingFailedDate);
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
				 * instance into the column! For cooldown to work properly, there has to be at
				 * least one ping attempt, and it should be EITHER successful or failed.
				 * 
				 */
				long currentMillis = new Date().getTime();
				Date lastSuccuessFulPingDate = getLastTimeSuccesfulPing(rowNode.getKey(), colNode.getKey());
				if (lastSuccuessFulPingDate != null) {

					if (((currentMillis - lastSuccuessFulPingDate.getTime()) % 6000 >= 5000)) {
						PingHeatData pingHeatData = colNode.getValue();
						PINGHEAT currentPingHeat = colNode.getValue().getPingHeat();
						pingHeatData.setPingHeat(PingEntry.getColderHeat(currentPingHeat));

						// This is the old method, but replacing cell entries is potentially a risk
						// unless
						// you clone the current cell, which is unnecessary and inefficient.
						// colNode.setValue(new
						// PingHeatData(PingEntry.getColderHeat(cellHeatData.getPingHeat())));

						/*
						 * DEBUGGING temperatures
						 * 
						 * if (lastSuccessfulPing > 0) { logger.
						 * debug("Last difference modulo 5000 between [successFulPingMillis, currentTime] of node ("
						 * + rowNode.getKey().getNodeName() + ", " + colNode.getKey().getNodeName() +
						 * "): [" + ((currentMillis - lastSuccessfulPing)%5000) + "]"); } if
						 * (lastSuccessfulPing == -1) { logger.debug("Last Ping FAILURE of node (" +
						 * rowNode.getKey().getNodeName() + ", " + colNode.getKey().getNodeName() +
						 * ") is NULL!!! "); }
						 */
					}
				}
			}
		}
	}

	public String getPingHeatMapAsString() {
		String foHeMa = "";
		for (Entry<SilverCloudNode, HashMap<SilverCloudNode, PingHeatData>> rowNode : pingHeatMap.entrySet()) {
			for (Entry<SilverCloudNode, PingHeatData> colNode : rowNode.getValue().entrySet()) {
				foHeMa += "(" + rowNode.getKey().getNodeName() + ", " + colNode.getKey().getNodeName()
						+ "): [pingHeat: " + colNode.getValue().getPingHeat() + "]\n";
			}
		}
	return foHeMa;
	}

	public void printPingHeatMap() {
		for (Entry<SilverCloudNode, HashMap<SilverCloudNode, PingHeatData>> rowNode : pingHeatMap.entrySet()) {
			// int countCells = 0;
			for (Entry<SilverCloudNode, PingHeatData> colNode : rowNode.getValue().entrySet()) {
				// ++countCells;
				logger.debug("pingHeat of pair after cool-down: (" + rowNode.getKey().getNodeName() + ", "
						+ colNode.getKey().getNodeName() + "): " + colNode.getValue().getPingHeat());
				// + " --- cellCounter: " + countCells);
			}
		}
		logger.debug(" --------------------------------------------------------------------------------------------- ");
	}

	public ArrayList<SimplePingHeat> getPiHeMaAsSimplePingHeatList() {
		ArrayList<SimplePingHeat> pingHeMaPiEnLi = new ArrayList<SimplePingHeat> ();
		
		for (Entry<SilverCloudNode, HashMap<SilverCloudNode, PingHeatData>> rowNode : pingHeatMap.entrySet()) {
			
			for (Entry<SilverCloudNode, PingHeatData> colNode : rowNode.getValue().entrySet()) {
			
				int cellPingHeat = this.getPingHeat(rowNode.getKey(),colNode.getKey()).getValue();
				
				pingHeMaPiEnLi.add(new SimplePingHeat(rowNode.getKey().getNodeName(), 
						colNode.getKey().getNodeName(), cellPingHeat));				
			}
		}	
		return pingHeMaPiEnLi;
	}
	
	/*
	 * Method  getPiHeMaAsNodeNameList() returns the SilverCloud node names in the order as they appear in the 
	 * PingHeatMatrix.
	 */
	public Set<String> getPiHeMaAsNodeNameList() {
		ArrayList<String> pingHeMaNoNaLi = new ArrayList<String>();
		
		//For future use: get a list of unique row node entries of type SilverCloudNode.
		Set<Map.Entry<SilverCloudNode, HashMap<SilverCloudNode, PingHeatData>>> rowEntrySet = 
				new HashSet<Map.Entry<SilverCloudNode,HashMap<SilverCloudNode,PingHeatData>>>();
		for (Entry<SilverCloudNode, HashMap<SilverCloudNode, PingHeatData>> rowNode : pingHeatMap.entrySet()) {		
			
			//For future use.
			rowEntrySet.add(rowNode);
			
			//Collect the row node names into a list with duplicates. The duplicates are the number of the number of columns.
			pingHeMaNoNaLi.add(rowNode.getKey().getNodeName());			
			}
		/* Return only the set of unique node names in the order they were added into the Set. 
		 * ChatGPT confirms:
		 * The one-argument constructor of LinkedHashSet that takes a Set as an argument maintains the order of elements 
		 * while ensuring uniqueness by filtering out duplicates. When you create a new LinkedHashSet by passing another 
		 * Set as an argument, the iteration order of the elements in the resulting LinkedHashSet is determined by the 
		 * iteration order of the elements in the provided Set.
		 * THIS IS NOT EXPLICITLY MENTIONED IN THE API DOC!
		 */
		return new LinkedHashSet<>(pingHeMaNoNaLi);
	}

	public void setPingHeat(SilverCloudNode rowNode, SilverCloudNode colNode, PingHeatData heat) {
		this.pingHeatMap.get(rowNode).put(colNode, heat);
	}

	public void setPingHeat(ArrayList<PingEntry> pingEntries) {
		for (PingEntry pingEntry : pingEntries) {
			SilverCloudNode rowNode = pingEntry.getPingOrig();
			SilverCloudNode colNode = pingEntry.getPingDest();
			PINGHEAT currentPingHeat = this.getPingHeat(rowNode, colNode);
			if (pingEntry.getLastPingResult().equals(PingEntry.PINGRESULT.PINGSUCCESS)) {
				this.setPingHeat(rowNode, colNode, PingEntry.getWarmerHeat(currentPingHeat));
				this.setLastTimePingSucceeded(rowNode, colNode, pingEntry.getLastPingDate());
			}

			else if (pingEntry.getLastPingResult().equals(PINGRESULT.PINGFAILURE)) {
				this.setPingHeat(rowNode, colNode, PingEntry.getColderHeat(currentPingHeat));
				this.setLastTimePingSucceeded(rowNode, colNode, pingEntry.getLastPingDate());
			}
			// If there is no PINGSUCCESS or PINGFAILURE, the status of the pingHeat and all
			// of the dates remain unchanged.

			// logger.info("Set pingheat of (rowNode, colNode): (" + rowNode.getNodeName() +
			// ", " + colNode.getNodeName() +
			// ") to:" + nextPingHeat.getPingHeat());

		}
	}

}
