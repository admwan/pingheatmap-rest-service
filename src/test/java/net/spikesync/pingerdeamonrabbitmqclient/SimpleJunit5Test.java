package net.spikesync.pingerdeamonrabbitmqclient;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import net.spikesync.pingerdaemonrabbitmqclient.SilverCloud;
import net.spikesync.pingerdaemonrabbitmqclient.SilverCloudNode;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:beans.xml")
public class SimpleJunit5Test {
	@Autowired
	private SilverCloud sc;

	@BeforeAll
	static void initAll() {
		System.out.println("---Inside initAll---");
	}

	@BeforeEach
	void init(TestInfo testInfo) {
		System.out.println("Start..." + testInfo.getDisplayName());
	}

	@Test
	public void messageTest() {
		ArrayList<SilverCloudNode> nodes = sc.getScNodes();
		SilverCloudNode targetNode = null;
		for (SilverCloudNode node : nodes) {
			if (node.getNodeName().equals("SURFIE")) targetNode = node;  
		}
		assertEquals("192.168.50.100", targetNode.getIpAddress());
	}

	
	@AfterEach
	void tearDown(TestInfo testInfo) {
		System.out.println("Finished..." + testInfo.getDisplayName());
	}

	@AfterAll
	static void tearDownAll() {
		System.out.println("---Inside tearDownAll---");
	}
} 