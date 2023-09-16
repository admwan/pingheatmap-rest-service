package net.spikesync.pingerdaemonrabbitmqclient;

public class SilverCloudNode {
	
	private String nodeName;
	private String ipAddress;
	
	public SilverCloudNode(String name, String address) {
		this.nodeName = name;
		this.ipAddress = address;
	}
	public String getNodeName() {
		return this.nodeName;
	}
	
	public String getIpAddress() {
		return this.ipAddress;
	}
	
	@Override
	public String toString() {
		return("(" + this.nodeName + ", " + this.ipAddress +")");
	}
}
