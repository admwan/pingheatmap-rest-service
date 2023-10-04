package net.spikesync.pingerdaemonrabbitmqclient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;


public class PingerdaemonRabbitmqClientApplication {

    private static final Logger logger = LoggerFactory.getLogger(PingerdaemonRabbitmqClientApplication.class);
	private ApplicationContext context = new GenericXmlApplicationContext("classpath:beans.xml");
	private PingMsgReader pingMsgReader;
    
    public static void main(String[] args) {
    	PingerdaemonRabbitmqClientApplication devPingApp = new PingerdaemonRabbitmqClientApplication();
    	devPingApp.pingMsgReader = devPingApp.context.getBean(PingMsgReader.class);
    	
    	
    	//Test PingHeatMap with debugger (debuggind doesn't work in test-methods).
    	PingHeatMap pingHeatMapObj = devPingApp.context.getBean(PingHeatMap.class);
    	HashMap<SilverCloudNode, HashMap<SilverCloudNode,Integer>> pingHeatMap = pingHeatMapObj.getPingHeatmap();
		SilverCloudNode colNode = new SilverCloudNode("CAPTUW", "192.168.50.104");
		SilverCloudNode rowNode = new SilverCloudNode("THORFW", "192.168.50.107");
		Integer pingHeat = pingHeatMapObj.getPingHeat(colNode, rowNode);
		logger.debug("Value of pingHeatMap.getPingHeat(colNode, rowNode) is:" + pingHeat );

 		Properties prop = PropertiesLoader.loadProperties();
 		if(prop == null) 
 			logger.debug("************** ========= Properties not loaded! Check the name of the properties file! ************** ========= ");
 		else 
 			logger.debug("************** ========= Property test-silvercloud-scnodes is set to: "  + prop.getProperty("test-silvercloud-scnodes"));
       
        

 		
 		devPingApp.run(args);
    }

    public void run(String... args) {
        
        for (int i = 0; i < args.length; ++i) {
            logger.info("args[{}]: {}", i, args[i]);
        }    
        
        logger.debug("Now starting listener with devPingApp..connectPingMQ(context) --------------------**********");

        this.pingMsgReader.connectPingMQ();

    }
}

