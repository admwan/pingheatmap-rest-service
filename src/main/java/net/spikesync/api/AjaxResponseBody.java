package net.spikesync.api;

import java.util.ArrayList;

/* Class to encapsulate the various datastructures necessary for rendering the PingHeatMap in an HTML table */
public class AjaxResponseBody {

    String msg;
    
    /* The PingHeat matrix has the dimensions pingNodeList.size() x pingNodeList.size() and consists of the Node Names
     * as both the rows as the columns. The pngMatrixData can therefore be linear because it consists of exactly 
     * the values of the squared (row,column) elements.
     */
    
    ArrayList<String> pingNodeList; 
    ArrayList<SimplePingHeat> pingMatrixData;

	public String getMsg() {
        return msg;
    }

    public ArrayList<String> getPingNodeList() {
    	return this.pingNodeList;
    }

    public ArrayList<SimplePingHeat> getPingMatrixData() {
        return pingMatrixData;
    }
    
   public void setPingNodeList(ArrayList<String> nodeL) {
   	this.pingNodeList = nodeL;
   }
 

   public void setMsg(String msg) {
       this.msg = msg;
   }
   

    public void setPingMatrixData(ArrayList<SimplePingHeat> pingMatrixD) {
        this.pingMatrixData = pingMatrixD;
    }

}
