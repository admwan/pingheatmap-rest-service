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
@ImportResource(locations = { "classpath:beans.xml" })
@SpringBootApplication
public class PingerdaemonRabbitmqClientApplication {

	private static final Logger logger = LoggerFactory.getLogger(PingerdaemonRabbitmqClientApplication.class);

	public static void main(String[] args) {

		Properties prop = PropertiesLoader.loadProperties();
		if (prop == null)
			logger.debug(
					"************** ========= Properties not loaded! Check the name of the properties file! ************** ========= ");
		else
			logger.debug("************** ========= Property test-silvercloud-scnodes is set to: "
					+ prop.getProperty("test-silvercloud-scnodes"));

		SpringApplication springDevPingApp = new SpringApplication(PingerdaemonRabbitmqClientApplication.class);
		springDevPingApp.setDefaultProperties(Collections.singletonMap("server.port", "8098"));
		springDevPingApp.run(args);

	}
}