package net.spikesync.pingerdaemonrabbitmqclient;

import java.util.ArrayList;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class PingHeatMapUpdateTask extends Thread implements ApplicationContextAware {

	@Autowired
	private final PingHeatMap pingHeatMap;
	private final PingMsgReader pingMsgReader;

	private ApplicationContext applicationContext;

	private boolean connectionEstablished = false;
	private volatile boolean isSuspended = false;
	private volatile boolean isCoolDownTaskSuspended = false;

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PingHeatMapUpdateTask.class);

	public PingHeatMapUpdateTask(PingHeatMap piHeMa, PingMsgReader piMsRe) {
		this.pingHeatMap = piHeMa;
		this.pingMsgReader = piMsRe;
	}

	@Override
	public void setApplicationContext(ApplicationContext appCtx) throws BeansException {
		this.applicationContext = appCtx;

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
		} else {
			logger.error("No connection to RabbitMQ established, even after trying, and no Exception occurred!");
		}
	}

	@Override
	public void run() {
		logger.debug("Ping heatmap UPDATE Thread STARTED!");
		while (true) {
			try {
				synchronized (this) {
					while (isSuspended) {
						wait();
					}
				}
				readRmqUpdatePiHeMa();
				/*
				 * The if clause belos contains: get the status of the pingheatmap cooldown task through its bean in the context. 
				 * If it is active, don't print the pingheatmap because then it is printed in the pingheat cooldown bean.
				 */
				if (applicationContext.getBean(PingHeatMapCoolDownTask.class).getIsSuspended()) {
					logger.debug("Now printing PingHeatMap in PingHeatMap after calling readRmqUpdatePiHeMa() AND cooldown is not running!");
					pingHeatMap.printPingHeatMap();
				} else
					logger.debug(
							"Pingheat Cooldown task is active; not printing PingHeatMap table (avoiding duplicate.)");
				Thread.sleep(2000);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
