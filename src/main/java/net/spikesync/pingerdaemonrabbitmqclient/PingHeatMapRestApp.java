package net.spikesync.pingerdaemonrabbitmqclient;

import java.util.Collections;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@SpringBootConfiguration
@EnableAutoConfiguration
@ImportResource(locations = {"classpath:beans.xml" })
@SpringBootApplication
public class PingHeatMapRestApp {

	private static final Logger logger = LoggerFactory.getLogger(PingHeatMapRestApp.class);

	public static void main(String[] args) {

		logger.debug("Starting PingHeatMapRestApp-main");
		Properties prop = PropertiesLoader.loadProperties();
		if (prop == null)
			logger.debug(
					"************** ========= Properties not loaded! Check the name of the properties file! ************** ========= ");
		else
			logger.debug("************** ========= Property test-silvercloud-scnodes is set to: "
					+ prop.getProperty("test-silvercloud-scnodes"));

		SpringApplication springDevPingApp = new SpringApplication(PingHeatMapRestApp.class);
		springDevPingApp.setDefaultProperties(Collections.singletonMap("server.port", "8099"));
		springDevPingApp.run(args);

	}
}