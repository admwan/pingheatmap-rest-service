package net.spikesync.pingerdaemonrabbitmqclient;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;

public class PingHeatMapUpdateTask extends Thread {

	@Autowired
	private final PingHeatMap pingHeatMap;
	private final PingMsgReader pingMsgReader;

	private boolean connectionEstablished = false;
	private volatile boolean isSuspended = false;

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PingHeatMapUpdateTask.class);

	public PingHeatMapUpdateTask(PingHeatMap piHeMa, PingMsgReader piMsRe) {
		this.pingHeatMap = piHeMa;
		this.pingMsgReader = piMsRe;
	}

	public synchronized void suspendThread() {
		isSuspended = true;
		logger.debug("Ping UPDATE Thread SUSPENDED!");

	}

	public synchronized void resumeThread() {
		isSuspended = false;
		logger.debug("Ping heatmap UPDATE Thread RESUMED!");
		notify(); // Notify waiting threads
	}

	public boolean getIsSuspended() {
		return this.isSuspended;
	}

	public void readRmqUpdatePiHeMa() {

		logger.debug("In readRmqUpdatPiHeMa()  -------------- part of PingHeatMapUpdateTask.");

		// In this project everything needed by PingMsgReader is injected at
		// bean-construction time, so it is ready to be used!

		if (!connectionEstablished) {
			try {
				connectionEstablished = this.pingMsgReader.connectPingMQ();

			} catch (Exception ce) {
				logger.error("Connection with RabbitMQ failed!! Is the RabbitMQ service running?\n"
						+ "NOT updating PingHeat matrix!!");
			}
		} else if (connectionEstablished) {
			ArrayList<PingEntry> newPingEntries = this.pingMsgReader.createPingEntriesFromRabbitMqMessages();
			if ((newPingEntries != null) && !newPingEntries.isEmpty())
				this.pingHeatMap.setPingHeat(newPingEntries);
			else
				logger.debug("newPingEntries is null or empty! Not creating any new PingEntry objects!!!!!");
		}
		else {
			logger.error("No connection to RabbitMQ established, even after trying, and no Exception occurred!");
		}
	}

	@Override
	public void run() {
		while (true) {
			logger.debug("Ping heatmap UPDATE Thread STARTED!");
			try {
				synchronized (this) {
					while (isSuspended) {
						wait();
					}
				}
				readRmqUpdatePiHeMa();
				logger.debug("Now printing PingHeatMap in PingHeatMap after clling readRmqUpdatePiHeMa()");
				pingHeatMap.printPingHeatMap();
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
