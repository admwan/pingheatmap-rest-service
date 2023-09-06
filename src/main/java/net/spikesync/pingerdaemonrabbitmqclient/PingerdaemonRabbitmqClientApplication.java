package net.spikesync.pingerdaemonrabbitmqclient;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;


public class PingerdaemonRabbitmqClientApplication {

    private static final Logger logger = LoggerFactory.getLogger(PingerdaemonRabbitmqClientApplication.class);
    private static final String QUEUE_NAME = "SilverSurfieRMQHpingQueue";
   // private static final CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
    private AmqpAdmin admin;
   // private AmqpTemplate template;
    private ApplicationContext context;
   
    
    @Autowired
    private SilverCloud sc;

    private PingerdaemonRabbitmqClientApplication() {
    	
    	this.context = new GenericXmlApplicationContext("classpath:beans.xml");
    	this.admin = context.getBean(RabbitAdmin.class);
    	this.admin.declareQueue(new Queue(QUEUE_NAME));
    	//this.template = context.getBean(AmqpTemplate.class);

    }
	public static void main(String[] args) {
		logger.info("Starting PingerdaemonRabbitmqClientApplication");
		PingerdaemonRabbitmqClientApplication pingDaemonApp = new PingerdaemonRabbitmqClientApplication();
		pingDaemonApp.run(args);
	}
	
	public void run(String... args) {
		logger.info("SilverCloud nodes: " + sc.getScNodes());
		
	}

}
