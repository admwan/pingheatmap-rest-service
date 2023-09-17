package net.spikesync.pingerdaemonrabbitmqclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

//The annotations below only work wit Spring Boot
//@EnableAutoConfiguration
//@Configuration
//@ImportResource(value = { "classpath:beans.xml" })

public class PingerdaemonRabbitmqClientApplication {

    private static final Logger logger = LoggerFactory.getLogger(PingerdaemonRabbitmqClientApplication.class);
	private ApplicationContext context = new GenericXmlApplicationContext("classpath:beans.xml");
	private SilverCloud sc;
	private PingMsgReader pingMsgReader;
    
    public static void main(String[] args) {
    	PingerdaemonRabbitmqClientApplication devPingApp = new PingerdaemonRabbitmqClientApplication();
        // RabbitAdmin ra = devPingApp.context.getBean(RabbitAdmin.class);
        devPingApp.sc = devPingApp.context.getBean(SilverCloud.class);
    	devPingApp.pingMsgReader = devPingApp.context.getBean(PingMsgReader.class);
    //	devPingApp.pingMsgReader = new PingMsgReader(devPingApp.context);
        if(devPingApp.pingMsgReader.getAmqpTemplate() != null)
        	logger.debug("devPingApp instance of PingMsgReader: AMQPTemplate EXISTS!  !!!!!!!! OK !!!!!!!!" );
        else
        	logger.debug("devPingApp instance of PingMsgReader: AMQPTemplate DOES NOT EXIST %%%%%%%%% ERROR ###########");
        logger.debug("\n***************************"
        		+ "\n************************ PingMessageReader instance: " + devPingApp.pingMsgReader 
        		+ "\n***********************");
        logger.debug("SilverCloud from this.pingMsgReader.getSilvercloud(): " + devPingApp.pingMsgReader.getSilverCloud().getScNodes());
    	logger.debug("Starting DevPingApplication");
        devPingApp.run(args);
    }

    public void run(String... args) {
        logger.debug("SilverCloud nodes: "); 
        this.sc.getScNodes().forEach(e -> logger.debug(e.toString()));
        logger.debug("EXECUTING : command line runner");
        
        for (int i = 0; i < args.length; ++i) {
            logger.info("args[{}]: {}", i, args[i]);
        }    
        
        logger.debug("Not starting listener with devPingApp..connectPingMQ(context) --------------------**********");
        this.pingMsgReader.connectPingMQ(context);
    }
}

