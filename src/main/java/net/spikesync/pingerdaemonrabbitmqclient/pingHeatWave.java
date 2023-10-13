package net.spikesync.pingerdaemonrabbitmqclient;

import java.util.Date;

import net.spikesync.pingerdaemonrabbitmqclient.PingEntry.PINGHEAT;

public class pingHeatWave {

	private PINGHEAT pingHeat;
	private Date lastPingAttempt;
	private Date lastPingSuccess;
	private Date lastPingFailure;
	
	public pingHeatWave(PINGHEAT heat) {
		this.pingHeat = heat;
		this.lastPingAttempt = null;
		this.lastPingSuccess = null;
		this.lastPingFailure = null;
	}
	
	public pingHeatWave(PINGHEAT heat, Date lastAttempt, Date lastSuccess, Date lastFailure) {
		this.pingHeat = heat;
		this.lastPingAttempt = lastAttempt;
		this.lastPingSuccess = lastSuccess;
		this.lastPingFailure = lastFailure;
	}
	
	public PINGHEAT getPingHeat() { return this.pingHeat; }
	
	public Date getLastPingAttempt() { return this.lastPingAttempt; }
	
	public Date getLastPingSucces() { return this.lastPingSuccess; }
	
	public Date getLastPingFailure() { return this.lastPingFailure; }
	
}
