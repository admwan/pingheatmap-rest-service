package net.spikesync.pingerdeamonrabbitmqclient;

import org.junit.BeforeClass;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.runner.RunWith;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


//import org.apache.logging.log4j.Logger;
//import org.apache.logging.log4j.LogManager;


import org.springframework.amqp.rabbit.test.RabbitListenerTest;
import org.springframework.amqp.rabbit.test.context.SpringRabbitTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;


import net.spikesync.pingerdaemonrabbitmqclient.PingEntry.PINGHEAT;
import net.spikesync.pingerdaemonrabbitmqclient.PropertiesLoader;
import net.spikesync.pingerdaemonrabbitmqclient.SilverCloudNode;

import org.junit.jupiter.api.condition.EnabledIf;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.Properties;

//@RabbitListenerTest
//@SpringJUnitConfig
//@SpringRabbitTest
@ExtendWith(SpringExtension.class)
@TestExecutionListeners(value = {
		  CustomTestExecutionListener.class,
		  DependencyInjectionTestExecutionListener.class
		})
@ContextConfiguration("classpath:beans.xml")
//@RunWith(SpringJUnit4ClassRunner.class)
class PingerdaemonRabbitmqClientApplicationTests {

 	private static final Logger logger = LoggerFactory.getLogger(PingerdaemonRabbitmqClientApplicationTests.class);

 	@Autowired
	private ApplicationContext context;
	private SilverCloudNode sc;

	//The class PropertiesLoader reads the properties file. Depending on the value of the property below testingEnabled will be set.
	//In turn, the method testingEnabled will return boolean.true or boolean.false depending on that value.
	private static String TEST_PROPERTY = "test-pingerdaemon-context";
	private String testingEnabled;
	private Properties prop;

	 @BeforeAll //From digitalocean Junit 5 tutorial
	  static void beforeAll() {
	    logger.debug("**--- Executed once before all test methods in this class ---**");
	  }

	  @BeforeEach //From digitalocean Junit 5 tutorial
	  void beforeEach() {
	    logger.debug("**--- Executed before each test method in this class ---**");
	  }
	  @AfterEach //From digitalocean Junit 5 tutorial
	  void afterEach() {
	    logger.debug("**--- Executed after each test method in this class ---**");
	  }

	  @AfterAll //From digitalocean Junit 5 tutorial
	  static void afterAll() {
	    logger.debug("**--- Executed once after all test methods in this class ---**");
	  }
  
	  
	@BeforeClass
	static void setTestsConfigurations(ExtensionContext ctx) {
		 //  TestConfiguration.setup(false);	
	
	}
	
	public PingerdaemonRabbitmqClientApplicationTests() { //Constructor in which the properties files is read.
 		prop = PropertiesLoader.loadProperties();
 		this.testingEnabled = prop.getProperty(TEST_PROPERTY);
 		logger.debug("Value of test-pingerdaemon-context: " + this.testingEnabled);
	}
	
	@Test
	@EnabledIf("testingEnabled")
	void contextLoads() {
		if(context!=null)
			logger.debug("**************** AutowiredCapableBeanFactory: " + context.toString());

		else {
			logger.debug("ApplicationContext variable is null!!");
			return;
		}
		assertThat(context).isNotNull();
		assertThat(context.getBean(net.spikesync.pingerdaemonrabbitmqclient.SilverCloud.class)).isNotNull();

	}

	@SuppressWarnings("unused") // This method is used by JUnit 5 @EnabledIf to determine whether to execute the
								// test or not.
	private boolean testingEnabled() {
		logger.debug("Value of this.testingEnabled: " + this.testingEnabled);

		if ((this.testingEnabled != null) && (testingEnabled.compareToIgnoreCase("TRUE") >= 0)) {
			logger.debug("Method testingEnabled is returning true!!");
			return true;
		} else {
			logger.debug("Method testingEnabled is returning false!!");
			return false;
		}
	}
}
