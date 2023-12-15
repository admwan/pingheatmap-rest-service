package net.spikesync.pingerdaemonrabbitmqclient;

import org.springframework.beans.factory.annotation.Autowired;


public class PingHeatMapCoolDownTask extends Thread {

	@Autowired
	private final PingHeatMap pingHeatMap;

	private volatile boolean isSuspended = false;

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PingHeatMapCoolDownTask.class);

	public PingHeatMapCoolDownTask(PingHeatMap piHeMa) {
		this.pingHeatMap = piHeMa;
	}
	
	public synchronized void suspendThread() {
		isSuspended = true;
		logger.debug("^^^^^^^^^^^^^^^^############## Cooldown Thread suspended!!!! #################^^^^^^^^^^^^^");

	}

	public synchronized void resumeThread() {
		isSuspended = false;
		notify(); // Notify waiting threads
	}

	public boolean getIsSuspended() {
		return this.isSuspended;
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				synchronized (this) {
					while (isSuspended) {
						wait();
					}
				}
				pingHeatMap.coolDownPingHeat();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}