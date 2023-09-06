package net.spikesync.pingerdeamonrabbitmqclient;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@RunWith(SpringRunner.class)
@ContextConfiguration("beans.xml")
//@ImportResource("beans.xml")
//@SpringBootTest(classes = PingerdaemonRabbitmqClientApplication.class)
//@ComponentScan
class PingerdaemonRabbitmqClientApplicationTests {

 //   private static final Logger logger = LoggerFactory.getLogger(PingerdaemonRabbitmqClientApplicationTests.class);
	private static final Logger logger = LoggerFactory.getLogger(PingerdaemonRabbitmqClientApplicationTests.class);

	@Autowired
	ApplicationContext context;
	
//	@Autowired
//	private SilverCloudNode sc;

	@Value("${logging.level.net.spikesync}")
	private String logginglevel;
	
	@Value("${test-silvercloud-scnodes}")
	private String testingEnabled;


	@Test
	void contextLoads() {
		logger.info("##################### Value of property logging.level.net.spikesync: "+ logginglevel);
		
		if(context!=null)
			logger.info("**************** AutowiredCapableBeanFactory: " + context.toString());

		else {
			logger.info("ApplicationContext variable is null!!");
			return;
		}
		assertThat(context).isNotNull();
		assertThat(context.getBean(net.spikesync.pingerdaemonrabbitmqclient.SilverCloud.class)).isNotNull();

	}

}
