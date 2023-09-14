package net.spikesync.pingerdaemonrabbitmqclient;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.GenericXmlApplicationContext;

import com.rabbitmq.client.Channel;

@EnableAutoConfiguration
@Configuration
@ImportResource(value = { "classpath:beans.xml" })
public class PingerdaemonRabbitmqClientApplication {

    private static final Logger logger = LoggerFactory.getLogger(PingerdaemonRabbitmqClientApplication.class);
    
	private static final String QUEUE_NAME = "SilverSurfieRMQHpingQueue";
	private static final CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
	
	private AmqpAdmin admin;
	private AmqpTemplate template;
	private ApplicationContext context;
	
	private Queue rabbitMQ;
	
    private SilverCloud sc;

    //Consturctor for stand-alone test-usage
    PingerdaemonRabbitmqClientApplication() {
    	
    	this.context = new GenericXmlApplicationContext("classpath:beans.xml");
    	this.admin = context.getBean(RabbitAdmin.class);
//    	this.admin.declareQueue(new Queue(QUEUE_NAME, false, false, false, null));
    	this.admin.declareQueue(this.rabbitMQ);
    	this.template = context.getBean(AmqpTemplate.class);
    }
    
    //Constructor for use within other application (DevPing)
    PingerdaemonRabbitmqClientApplication(ApplicationContext appCtx) {
    	
    	this.context = appCtx;
    	this.admin = context.getBean(RabbitAdmin.class);
    	this.admin.declareQueue(new Queue(QUEUE_NAME, false, false, false, null));
    	// this.admin.declareQueue(this.rabbitMQ);
    	this.template = context.getBean(AmqpTemplate.class);
    }
    
	public static void main(String[] args) {
		logger.debug("******************* Starting PingerdaemonRabbitmqClientApplication ******************* ");
		PingerdaemonRabbitmqClientApplication pingDaemonApp = new PingerdaemonRabbitmqClientApplication();
//		CachingConnectionFactory factory = pingDaemonApp.context.getBean(CachingConnectionFactory.class);
//		Connection connection = null;
//		Channel channel = null;
//		Queue rabbitMQ = null;
//		String vHostQueueName = null;
//		try {
//			connection = (Connection) factory.createConnection(); 
//			channel = (Channel) connection.createChannel(false);
//			rabbitMQ = pingDaemonApp.context.getBean(Queue.class);
//			vHostQueueName = rabbitMQ.getName();
//			channel.queueDeclare(vHostQueueName, false, false, false, null);
//
//		} catch (Exception e1) {
//			e1.printStackTrace();
//			System.exit(0);
//		}
//
		
		
		//pingDaemonApp.run(args);
	}
	
	public void run(String... args) {
		logger.info("SilverCloud nodes: " + sc.getScNodes());
		
	}

}
