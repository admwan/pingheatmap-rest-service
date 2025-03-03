package net.spikesync.pingerdaemonrabbitmqclient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class PingEntry implements Cloneable {

	private SilverCloudNode pingOrig;
	private SilverCloudNode pingDest;
	private Date lastPingDate;
	private PINGRESULT lastPingResult; // -1 means unkown, 0 means failed, 1 means succeeded
	private PINGHEAT pingHeat; // -1 means unknown. scale pingheated: 0 means long time ago, 10 means recently heated (ping successful)

	public PingEntry(Date lastDate, SilverCloudNode orig, SilverCloudNode dest, PINGRESULT lastResult, PINGHEAT heat) {
		this.lastPingDate = lastDate;
		this.pingOrig = orig;
		this.pingDest = dest;
		this.lastPingResult = lastResult;
		this.pingHeat = heat;
	}
	
	public static enum PINGRESULT { // Use this enum instead of integer codes.
		PINGSUCCESS,
		PINGFAILURE,
		PINGUNKOWN // Ping status/result unknown. Used to be -1 (Integer).
	}
	
	public static enum PINGHEAT {
	
		
		UNKNOWN(-1), // Pingheat is unknown. This is the LOWEST ordinal!! Important to determine the warmer or colder temperature!
		GLACIAL(0),
		FRIGID(1),
		CRISPY(2), 
		TEPID(3),
		SNUG(4),
		TROPIC(5),
		SCORCHING(6);
		
		private int heat;

		PINGHEAT(int h) {
			this.heat=h;
		}			
		
		int getValue() {
			return heat;
		}
	}
	
	static public PINGHEAT getWarmerHeat(PINGHEAT temperature) {
		int index = temperature.ordinal();
		int nextIndex = index + 1;
		if (nextIndex > 7) return PINGHEAT.SCORCHING; //Hotter than SCORCHING is not possible.
		PINGHEAT[] pingheat = PINGHEAT.values();
		nextIndex %= pingheat.length;
		return pingheat[nextIndex];
	}
	
	static public PINGHEAT getColderHeat(PINGHEAT temperature) {
		int index = temperature.ordinal();
		int nextIndex = index - 1;
		if(nextIndex == 0) return PINGHEAT.GLACIAL; // Colder than GLACIAL is not possible. 
		else if(nextIndex == -1) return PINGHEAT.UNKNOWN; // PINGHEAT was unknown, and on an unsuccessful ping it remains unknown.
		PINGHEAT[] pingheat = PINGHEAT.values();
		nextIndex %= pingheat.length;
		return pingheat[nextIndex];
	}
	
		
	public int getPingHeatOrdinals() {
		switch(pingHeat) {
		case UNKNOWN: return 0;
		case GLACIAL: return 1;
		case FRIGID: return 2;
		case CRISPY: return 3;
		case TEPID: return 4;
		case SNUG: return 5;
		case TROPIC: return 6;
		case SCORCHING: return 7;
		default: 
			break;
		}
		return 0; // This value (the default) is the same as UNKNOWN.
	}
	
	
	
	public PINGHEAT getPingHeat() {
		return pingHeat;
	}
	public void setPingHeat(PINGHEAT pingHeat) {
		this.pingHeat = pingHeat;
	}
	public SilverCloudNode getPingOrig() {
		return pingOrig;
	}
	public void setPingOrig(SilverCloudNode pingOrig) {
		this.pingOrig = pingOrig;
	}
	public SilverCloudNode getPingDest() {
		return pingDest;
	}
	public void setPingDest(SilverCloudNode pingDest) {
		this.pingDest = pingDest;
	}
	public Date getLastPingDate() {
		return lastPingDate;
	}
	public void setLastPingDate(Date lastPing) {
		this.lastPingDate = lastPing;
	}
	public PINGRESULT getLastPingResult() {
		return lastPingResult;
	}
	public void setLastPingResult(PINGRESULT lastPingResult) {
		this.lastPingResult = lastPingResult;
	}
	
	@Override
	public String toString() {
		
		DateFormat format = new SimpleDateFormat(
                "EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
		return format.format(this.lastPingDate) + ";" + this.pingOrig +  ";" + this.pingDest + ";" +  this.lastPingResult + ";" +  this.pingHeat;
	}	
}

// For DateFormat see:  https://stackoverflow.com/questions/5937017/how-to-convert-a-date-in-this-format-tue-jul-13-000000-cest-2010-to-a-java-d
