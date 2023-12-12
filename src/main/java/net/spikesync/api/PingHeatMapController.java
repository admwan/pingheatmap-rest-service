package net.spikesync.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.spikesync.pingerdaemonrabbitmqclient.PingEntry;
import net.spikesync.pingerdaemonrabbitmqclient.PingHeatData;
import net.spikesync.pingerdaemonrabbitmqclient.PingHeatMap;
import net.spikesync.pingerdaemonrabbitmqclient.PingMsgReader;
import net.spikesync.pingerdaemonrabbitmqclient.SilverCloud;
import net.spikesync.pingerdaemonrabbitmqclient.SilverCloudNode;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping(path = "/", produces = "application/json")
@CrossOrigin(origins = "http://localhost:8000")
public class PingHeatMapController {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PingHeatMapController.class);

	@Autowired
	private final PingHeatMap pingHeatMap;
	private final PingMsgReader pingMsgReader;
	private Runnable pingHeMaUpRunnable;
	private Runnable pingHeMaCooldownRunnable;
	private Thread pingHeMaCoWorkerThread;

	public PingHeatMapController(PingMsgReader piMeRe, PingHeatMap piHeMa) {
		pingMsgReader = piMeRe;
		pingHeatMap = piHeMa;
		pingHeMaUpRunnable = new Runnable() {
			@Override
			public void run() {
				readRmqUpdatePiHeMa();
			}
		};

		pingHeMaCooldownRunnable = new Runnable() {
			@Override
			public void run() {
				cooldownPingHeatMap();
			}
		};
	}

	public void readRmqUpdatePiHeMa() {

		logger.debug("Now starting listener with devPingApp..connectPingMQ(context) --------------------**********");

		// In this project everything needed by PingMsgReader is injected at
		// bean-construction time, so it is ready to be used!
		while (true) {
			boolean connectionEstablished;
			connectionEstablished = this.pingMsgReader.connectPingMQ();
			if (connectionEstablished) {
				ArrayList<PingEntry> newPingEntries = this.pingMsgReader.createPingEntriesFromRabbitMqMessages();
				if ((newPingEntries != null) && !newPingEntries.isEmpty())
					this.pingHeatMap.setPingHeat(newPingEntries);
				else
					logger.debug("newPingEntries is null or empty! Not creating any new PingEntry's!!!!!");
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				logger.error(
						"Exception during sleep of Thread running pingMsgReader.createPingEntriesFromRabbitMqMessages() !!");
				e.printStackTrace();
			}

		}
	}

	public void cooldownPingHeatMap() {
		while (true) {
			pingHeatMap.coolDownPingHeat();
			pingHeatMap.printPingHeatMap();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				logger.debug(
						"Caught an InterruptedException. This is not an error condition, but caused by the method ...");
			}

		}
	}

	// The following two methods that start the update threads are *automatically*
	// called when creating an instance of this class! This only happens when
	// annotated with @Autowired! TBD: HOW??
	@Autowired
	//@PostMapping("/startupdatepingheatmap")
	public void startUpdatePiHeMa() {
		logger.debug(
				"^^^^^^^^^^^^&&&&&&&&&&&&^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ Attempting to start pingHeatMapUpdateThread ...");
		new Thread(this.pingHeMaUpRunnable).start();
	}

	// Idem as the previous method: automatically executed on startup when
	// @Autowired is present!
	 @Autowired
	//@PostMapping("/startcooldownpingheatmap")
	public ResponseEntity<String> startCooldownPingHeatMap() {
		logger.debug(
				"^^^^^^^^^^^^############^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ Attempting to start pingHeatMapCooldownThread ...");
		try {
			this.pingHeMaCoWorkerThread = new Thread(this.pingHeMaCooldownRunnable);
			this.pingHeMaCoWorkerThread.start();
			return ResponseEntity.ok("Success");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred");
		}
	}
/*
	@PostMapping("/stopcooldownpingheatmap")
	public ResponseEntity<String> stopUpdatePiHeMa() {
		try {
			if (this.pingHeMaCoWorkerThread != null && this.pingHeMaCoWorkerThread.isAlive()) {
				this.pingHeMaCoWorkerThread.interrupt();
			}
			logger.debug("After interrupting this.pingHeMaCoWorkerThread. When is the exception thrown?");
			return ResponseEntity.ok("Success");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred");
		}

	}
*/
	@Autowired
	@PostMapping("/pingheatmap")
	public HashMap<SilverCloudNode, HashMap<SilverCloudNode, PingHeatData>> getPingHeatMap() {
		logger.debug("Now returning pingHeatMap as HashMap from REST API method getPingHeatMap");
		return pingHeatMap.getPingHeatmap();
	}

	@Autowired
	@PostMapping("/stringifiedheatmap")
	public String getStringifiedHeatMap() {
		String returnString = pingHeatMap.getPingHeatMapAsString();
		logger.debug("Stringified pingHeatMap: " + returnString);
		return returnString;
	}

	@Autowired
	@CrossOrigin(origins = { "http://localhost:8000" })
	@PostMapping("/jsonpingheatmap")
	public String getJsonPingHeatMap() {
		logger.debug("Now returning pingHeatMap as HashMap converted to JSON --- REST API method getPingHeatMap");
		ObjectMapper mapper = new ObjectMapper();
		String jsonPiHeMa = "";
		try {

			jsonPiHeMa = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(pingHeatMap);

		} catch (IOException e) {
			e.printStackTrace();
			logger.debug(
					"pinHeatMap can NOT be converted into JSON!!!! Returning a stringified pingHeatMap instead!!!!!!!");
			return getStringifiedHeatMap();
		}
		return jsonPiHeMa;
	}

	@Autowired
	@CrossOrigin(origins = { "http://localhost:8000" })
	@GetMapping("/plainjsonpingheatmap")
	public ResponseEntity<AjaxResponseBody> getPlainJsonPingHeatMap() {
		AjaxResponseBody ajaxReBo = new AjaxResponseBody();
		ajaxReBo.setPingNodeList(this.pingHeatMap.getSilverCloudNodeNameList());

		logger.debug("AjaxResponsebody.getPingNodeList TO BE RETURNED BY ENDPOINT /plainjsonpingheatmap: "
				+ ajaxReBo.getPingNodeList());
		ArrayList<SimplePingHeat> simplePingHeatList = this.pingHeatMap.getPiHeMaAsSimplePingHeatList();
		ajaxReBo.setPingMatrixData(simplePingHeatList);

		return ResponseEntity.ok(ajaxReBo);

	}
	/*
	 * Crude mapping of the whole pingHeatMap onto JSON won't work !!! This has been
	 * replaced by the AjaxResponseBody type as parameter of ResponseEntity above.
	 * 
	 * logger.
	 * debug("Returning !! ResponseEntity<String> !! with JSON as the body, and OK as the status !! \n"
	 * +
	 * "--------------------------------------------------- REST API method getPingHeatMap"
	 * ); ObjectMapper mapper = new ObjectMapper(); String jsonPiHeMa = ""; try {
	 * 
	 * jsonPiHeMa = mapper.writeValueAsString(pingHeatMap);
	 * 
	 * } catch (IOException e){ e.printStackTrace(); logger.
	 * debug("pinHeatMap can NOT be converted into JSON!!!! Returning null!!!!!!!");
	 * return null; }
	 */

	@Autowired
	@PostMapping("/lastcaptpingdate")
	public Date getLastCaptPingUpdate() {
		SilverCloudNode captNode = new SilverCloudNode("CAPTUW", "192.168.50.104");
		SilverCloudNode thorNode = new SilverCloudNode("THORFW", "192.168.50.107");
		Date lastCaptThorPing = pingHeatMap.getLastTimeSuccesfulPing(captNode, thorNode);
		logger.debug(
				"@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Date of the last CAPTUW-THORFW successful ping that should be returned is: "
						+ lastCaptThorPing);
		return lastCaptThorPing;
	}

	/*
	 * This endpoint returns a ResponseEntity, see:
	 * https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/
	 * ann-methods/responseentity.html It has the fields String body and String
	 * etag. For the time being this serves as a substitute response from the
	 * previous silvercloud-pingermatrix-spring-ajax-integrated project in attempt
	 * to reuse the JS/jQuery code of that template.
	 * 
	 */
	@PostMapping(value = "/show-pinger-matrix", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<AjaxResponseBody> getSearchResultViaAjax(@RequestBody DataInPostRequest dataInPostRequest,
			Errors errors) {

		logger.debug(
				"Now in PostMapping method getSearchResultsViaAjax() with dataInPostRequest.getPostRequestDataVal1: "
						+ dataInPostRequest.getPostRequestDataVal1());
		AjaxResponseBody result = new AjaxResponseBody();

		// If error, just return a 400 bad request, along with the error message
		if (errors.hasErrors()) {

			result.setMsg(
					errors.getAllErrors().stream().map(x -> x.getDefaultMessage()).collect(Collectors.joining(",")));
			return ResponseEntity.badRequest().body(result);

		}
		// No errors, return the values requested from the PinHeatMap!!! TBD!!!!
		result.setMsg("No data returned from PingerMatrixUpdateService!!");
		// result.setMsg("Success!!");

		// result.setPingNodeList(pingMatrix.getPingNodes());
		// result.setPingMatrixData(pingMatrix.getPingHeatData());

		return ResponseEntity.ok(result);

	}

	public PingHeatMap getPingHeatMapObject() {
		return this.pingHeatMap;
	}

}
