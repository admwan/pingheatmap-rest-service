package net.spikesync.pingerdaemonrabbitmqclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class PingHeatMapCoolDownTask extends Thread implements ApplicationContextAware {

	@Autowired
	private final PingHeatMap pingHeatMap;
	private ApplicationContext applicationContext;
	
	private volatile boolean isSuspended = true; // This task is not running when it is created; only after run() is
													// called for the first time and after resuming!

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PingHeatMapCoolDownTask.class);

	public PingHeatMapCoolDownTask(PingHeatMap piHeMa) {
		this.pingHeatMap = piHeMa;
	}
	
	@Override
	public void setApplicationContext(ApplicationContext appCtx) {
		this.applicationContext = appCtx;
	}

	public synchronized void suspendThread() {
		isSuspended = true;
		logger.debug("Ping heatmap COOLDOWN Thread SUSPENDED!");

	}

	public synchronized void resumeThread() {
		isSuspended = false;
		logger.debug("Ping heatmap COOLDOWN Thread RESUMED!");
		notify(); // Notify waiting threads
	}

	public boolean getIsSuspended() {
		return this.isSuspended;
	}

	@Override
	public void run() {
		logger.debug("Ping heatmap COOLDOWN Thread STARTED!");
		this.isSuspended = false;
		while (true) {
			try {
				synchronized (this) {
					while (isSuspended) {
						wait();
					}
				}
				pingHeatMap.coolDownPingHeat();
				pingHeatMap.printPingHeatMap();
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}