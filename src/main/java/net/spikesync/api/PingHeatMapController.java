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

import net.spikesync.pingerdaemonrabbitmqclient.PingHeatData;
import net.spikesync.pingerdaemonrabbitmqclient.PingHeatMap;
import net.spikesync.pingerdaemonrabbitmqclient.PingMsgReader;
import net.spikesync.pingerdaemonrabbitmqclient.SilverCloudNode;
import net.spikesync.pingerdaemonrabbitmqclient.PingHeatMapCoolDownTask;
import net.spikesync.pingerdaemonrabbitmqclient.PingHeatMapUpdateTask;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping(path = "/", produces = "application/json")
@CrossOrigin(origins = "http://localhost:8098")
public class PingHeatMapController {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PingHeatMapController.class);

	@Autowired
	private final PingHeatMap pingHeatMap;
	private PingHeatMapCoolDownTask piHeMaCoolDownTask;
	private PingHeatMapUpdateTask piHeMaUpdateTask;

	public PingHeatMapController(PingMsgReader piMeRe, PingHeatMap piHeMa, PingHeatMapCoolDownTask piHeMaCoDoTa, PingHeatMapUpdateTask piHeMaUpTa) {
		pingHeatMap = piHeMa;
		piHeMaCoolDownTask = piHeMaCoDoTa;
		piHeMaUpdateTask = piHeMaUpTa;
		
	}

	
	@PostMapping("/startupdatepingheatmap")
	public ResponseEntity<String> startUpdatePiHeMa() {
		logger.debug("Attempting to start pingHeatMapUpdateThread ...");
		try {
			if (this.piHeMaUpdateTask.getState() == Thread.State.NEW) {
	
				this.piHeMaUpdateTask.start();
				return ResponseEntity.ok("Succesfully STARTED ping heatmap UPDATE process!\n");
			}
			else if (this.piHeMaUpdateTask.getIsSuspended()) {
				this.piHeMaUpdateTask.resumeThread();
				return ResponseEntity.ok("Succesfully RESUMED ping heatmap UPDATE process!\n");
			}
			else {
				return ResponseEntity.ok("Ping heatmap UPDATE process ALREADY STARTED and is NOT SUSPENDED!\n");
			}
		} catch (Exception e) {
			logger.error("Exception while trying to (re)start the cooldown Thread!!\n");
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error occurred!!\n");
			
		}
	}
	
	
	// Idem as the previous method: automatically executed on startup when @Autowired is present!
	// @Autowired
	@PostMapping("/startcooldownpingheatmap")
	public ResponseEntity<String> startCooldownPingHeatMap() {
		logger.debug("Attempting to start pingHeatMapCooldownThread ...");
		try {
			if (this.piHeMaCoolDownTask.getState() == Thread.State.NEW) {
	
				this.piHeMaCoolDownTask.start();
				return ResponseEntity.ok("Succesfully STARTED ping heatmap COOLDOWN process!\n");
			}
			else if (this.piHeMaCoolDownTask.getIsSuspended()) {
				this.piHeMaCoolDownTask.resumeThread();
				return ResponseEntity.ok("Succesfully RESUMED ping heatmap COOLDOWN process!\n");
			}
			else {
				return ResponseEntity.ok("Ping heatmap cooldown process ALREADY STARTED and is not SUSPENDED!\n");
			}
		} catch (Exception e) {
			logger.error("Exception while trying to (re)start the cooldown Thread!!\n");
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error occurred!!\n");
			
		}
	}

	@PostMapping("/stopcooldownpingheatmap")
	public ResponseEntity<String> stopCooldownPiHeMa() {
		try {
			if ((this.piHeMaCoolDownTask.getState() != Thread.State.NEW)
					&& (!this.piHeMaCoolDownTask.getIsSuspended())) {
				this.piHeMaCoolDownTask.suspendThread();
				logger.debug("Ping coolDownTask Thread SUSPENDED!!");
				return ResponseEntity.ok("Ping heat COOLDOWN process is now suspended!\n");
			} else {
				return ResponseEntity.ok("Pingheat COOLDOWN process not started or ALREADY STOPPED!\n");
			}
		} catch (Exception e) {
			logger.error("Exception while trying to stop the COOLDOWN Thread!!\n");
			e.printStackTrace();

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error occurred!!\n" + e.getMessage());
		}

	}

	@PostMapping("/stopupdatepingheatmap")
	public ResponseEntity<String> stopUpdatePiHeMa() {
		try {
			if ((this.piHeMaUpdateTask.getState() != Thread.State.NEW)
					&& (!this.piHeMaUpdateTask.getIsSuspended())) {
				this.piHeMaUpdateTask.suspendThread();
				logger.debug("pingUpdateHeatMap Thread SUSPENDED!!");
				return ResponseEntity.ok("PINGUPDATEHEATMAP process is now suspended!\n");
			} else {
				return ResponseEntity.ok("PINGUPDATEHEATMAP process not started or ALREADY STOPPED!\n");
			}
		} catch (Exception e) {
			logger.error("Exception while trying to stop the PINGUPDATEHEATMAP Thread!!\n");
			e.printStackTrace();

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error occurred!!\n" + e.getMessage());
		}

	}

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

	/* This is the endpoint called by the HTML/JS in project js-fetch-pingheatmap */
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
