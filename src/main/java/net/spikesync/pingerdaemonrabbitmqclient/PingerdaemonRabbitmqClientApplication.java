package net.spikesync.pingerdaemonrabbitmqclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.GenericXmlApplicationContext;

import net.spikesync.api.PingHeatMapController;

@SpringBootConfiguration
@EnableAutoConfiguration
@ImportResource(locations = {"classpath:beans.xml" })
@SpringBootApplication
public class PingerdaemonRabbitmqClientApplication {

	private static final Logger logger = LoggerFactory.getLogger(PingerdaemonRabbitmqClientApplication.class);
//	private ApplicationContext context = new GenericXmlApplicationContext("classpath:beans.xml");
/*	private PingMsgReader pingMsgReader;
	private PingHeatMap pingHeatMap;
	private PingHeatMapController pingHeatMapController;
*/
	
/*	public PingerdaemonRabbitmqClientApplication(PingMsgReader piMsRe, PingHeatMap piHeMa,
		PingHeatMapController piHeMaCo) {
		this.pingMsgReader = piMsRe;
		this.pingHeatMap = piHeMa;
		this.pingHeatMapController = piHeMaCo;

	}
*/
	public static void main(String[] args) {
  
 		Properties prop = PropertiesLoader.loadProperties();
 		if(prop == null) 
 			logger.debug("************** ========= Properties not loaded! Check the name of the properties file! ************** ========= ");
 		else 
 			logger.debug("************** ========= Property test-silvercloud-scnodes is set to: "  + prop.getProperty("test-silvercloud-scnodes"));
	
		SpringApplication  springDevPingApp = new SpringApplication(PingerdaemonRabbitmqClientApplication.class);
  /*	PingerdaemonRabbitmqClientApplication devPingApp = new PingerdaemonRabbitmqClientApplication(null,null,null);
        devPingApp.pingMsgReader = devPingApp.context.getBean(PingMsgReader.class);
    	devPingApp.pingHeatMap = devPingApp.context.getBean(PingHeatMap.class);
  */  	

    	springDevPingApp.setDefaultProperties(Collections.singletonMap("server.port", "8098"));
    	springDevPingApp.run(args);
    	
	}
}
  
 /*		Runnable r1 = new Runnable() { 
 			public void run() {
 				devPingApp.readRmqAndUpdatePingHeatMap(args);
 			}
 		};
 		new Thread(r1).start();
     	
 		Runnable r2 = new Runnable() {
 			public void run() {
 				while (true) {
 				devPingApp.pingHeatMap.coolDownPingHeat();
 				devPingApp.pingHeatMap.printPingHeatMap();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
 				}
 			}
 				
 		};
 		new Thread(r2).start();
    }
*/


  /*  public void readRmqAndUpdatePingHeatMap(String... args) {
        
        for (int i = 0; i < args.length; ++i) {
            logger.info("args[{}]: {}", i, args[i]);
        }    
        
        logger.debug("Now starting listener with devPingApp..connectPingMQ(context) --------------------**********");

        // In this project everything needed by PingMsgReader is injected at bean-construction time, so it is ready to be used!
        while(true) {
        	boolean connectionEstablished;
			connectionEstablished = this.pingMsgReader.connectPingMQ();
			if(connectionEstablished) {
				ArrayList<PingEntry> newPingEnties = this.pingMsgReader.createPingEntriesFromRabbitMqMessages();
				this.pingHeatMap.setPingHeat(newPingEnties);
			}
				try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
    }*/

