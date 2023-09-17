package net.spikesync.pingerdaemonrabbitmqclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;


public class PingerdaemonRabbitmqClientApplication {

    private static final Logger logger = LoggerFactory.getLogger(PingerdaemonRabbitmqClientApplication.class);
	private ApplicationContext context = new GenericXmlApplicationContext("classpath:beans.xml");
	private SilverCloud sc;
	private PingMsgReader pingMsgReader;
    
    public static void main(String[] args) {
    	PingerdaemonRabbitmqClientApplication devPingApp = new PingerdaemonRabbitmqClientApplication();
    	devPingApp.pingMsgReader = devPingApp.context.getBean(PingMsgReader.class);
        devPingApp.run(args);
    }

    public void run(String... args) {
        
        for (int i = 0; i < args.length; ++i) {
            logger.info("args[{}]: {}", i, args[i]);
        }    
        
        logger.debug("Now starting listener with devPingApp..connectPingMQ(context) --------------------**********");
//        this.pingMsgReader.connectPingMQ(context);
        this.pingMsgReader.connectPingMQ();

    }
}

