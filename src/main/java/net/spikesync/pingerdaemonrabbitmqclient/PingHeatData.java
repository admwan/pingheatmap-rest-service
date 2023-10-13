package net.spikesync.pingerdaemonrabbitmqclient;

import java.util.Date;

import net.spikesync.pingerdaemonrabbitmqclient.PingEntry.PINGHEAT;

public class PingHeatData {

	private PINGHEAT pingHeat;
	private Date lastPingAttempt;
	private Date lastPingSuccess;
	private Date lastPingFailure;
	
	public PingHeatData(PINGHEAT heat) {
		this.pingHeat = heat;
		this.lastPingAttempt = null;
		this.lastPingSuccess = null;
		this.lastPingFailure = null;
	}
	
	public PingHeatData(PINGHEAT heat, Date lastAttempt, Date lastSuccess, Date lastFailure) {
		this.pingHeat = heat;
		this.lastPingAttempt = lastAttempt;
		this.lastPingSuccess = lastSuccess;
		this.lastPingFailure = lastFailure;
	}
	
	public PINGHEAT getPingHeat() { return this.pingHeat; }
	
	public Date getLastPingAttempt() { return this.lastPingAttempt; }
	
	public Date getLastPingSucces() { return this.lastPingSuccess; }
	
	public Date getLastPingFailure() { return this.lastPingFailure; }
	
	public void setPingHeat(PINGHEAT heat) {
		this.pingHeat = heat;
	}
	
}
