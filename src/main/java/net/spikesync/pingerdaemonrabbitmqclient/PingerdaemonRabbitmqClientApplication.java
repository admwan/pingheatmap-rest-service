package net.spikesync.pingerdaemonrabbitmqclient;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.GenericXmlApplicationContext;

@EnableAutoConfiguration
@Configuration
@ImportResource(value = { "classpath:beans.xml" })
public class PingerdaemonRabbitmqClientApplication {

    private static final Logger logger = LoggerFactory.getLogger(PingerdaemonRabbitmqClientApplication.class);
	private ApplicationContext context = new GenericXmlApplicationContext("classpath:beans.xml");
	private SilverCloud sc;
	private PingMsgReader msgR;
    
    public static void main(String[] args) {
    	PingerdaemonRabbitmqClientApplication devPingApp = new PingerdaemonRabbitmqClientApplication();
        RabbitAdmin ra = devPingApp.context.getBean(RabbitAdmin.class);
        devPingApp.sc = devPingApp.context.getBean(SilverCloud.class);
        devPingApp.msgR = devPingApp.context.getBean(PingMsgReader.class);
        logger.debug("\n***************************\n************************ PingMessageReader instance: " + devPingApp.msgR + "\n***********************");
        logger.debug("SilverCloud: " + devPingApp.sc.getScNodes());
    	logger.info("Starting DevPingApplication");
        devPingApp.run(args);
    }

    public void run(String... args) {
        logger.info("SilverCloud nodes:    " + sc.getScNodes().toString());
        logger.info("EXECUTING : command line runner");
        
        for (int i = 0; i < args.length; ++i) {
            logger.info("args[{}]: {}", i, args[i]);
        }
        
        
    }
}

