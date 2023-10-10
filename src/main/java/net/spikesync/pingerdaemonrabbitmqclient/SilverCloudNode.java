package net.spikesync.pingerdaemonrabbitmqclient;

import java.util.Objects;

public class SilverCloudNode {

	private String nodeName;
	private String ipAddress;
	private int hashCode;

	// Constructor with name and ip address as Strings
	public SilverCloudNode(String name, String address) {
		this.nodeName = name;
		this.ipAddress = address;
		this.hashCode = Objects.hash(name,address);
	}
	
	// Constructor based on another SilverCloudNode (copy constructor)
	public SilverCloudNode(SilverCloudNode scNode) {
		this.nodeName = scNode.getNodeName();
		this.ipAddress = scNode.getIpAddress();
		this.hashCode = scNode.hashCode();
	}

	public String getNodeName() {
		return this.nodeName;
	}

	public String getIpAddress() {
		return this.ipAddress;
	}

	public void setNodeName(String nName) {
		this.nodeName = nName;
	}
	
	public void setIpAddess(String ipAdr) {
		this.ipAddress = ipAdr;
	}
	
	@Override
	public String toString() {
		return ("(" + this.nodeName + ", " + this.ipAddress + ")");
	}

	@Override
	public int hashCode() {
		return this.hashCode;
	}
	
	@Override
	public boolean equals(Object other) {
		if ((other == null) || (getClass() != other.getClass())) { // other must not be null and be of the same class 
			return false;
		} // end if
		else { 
			SilverCloudNode that = (SilverCloudNode) other;
			if((this.nodeName.equals(that.getNodeName())) && (this.ipAddress.equals(that.getIpAddress())))
			return true;
		}
		return false;
	}
}
