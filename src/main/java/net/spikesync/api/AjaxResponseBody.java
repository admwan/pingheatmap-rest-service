package net.spikesync.api;

import java.util.List;

import net.spikesync.pingerdaemonrabbitmqclient.PingEntry;

public class AjaxResponseBody {

    String msg;
    
//    Integer rowSize;
//    Integer colSize;
    
    List<String> pingNodeList;
    List<PingEntry> pingMatrixData;

    public List<String> getPingNodeList() {
    	return this.pingNodeList;
    }
    
    public void setPingNodeList(List<String> nodeL) {
    	this.pingNodeList = nodeL;
    }
    
	public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<PingEntry> getPingMatrixData() {
        return pingMatrixData;
    }

    public void setPingMatrixData(List<PingEntry> pingMatrixD) {
        this.pingMatrixData = pingMatrixD;
    }

}
