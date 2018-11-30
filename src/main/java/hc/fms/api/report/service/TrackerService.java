package hc.fms.api.report.service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import hc.fms.api.report.entity.FuelStatResult;
import hc.fms.api.report.entity.ReportGen;
import hc.fms.api.report.model.ReportResponse;
import hc.fms.api.report.model.GroupResponse;
import hc.fms.api.report.model.ReportGenFlatRequest;
import hc.fms.api.report.model.ReportGenResponse;
import hc.fms.api.report.model.SensorResponse;
import hc.fms.api.report.model.TrackerResponse;
import hc.fms.api.report.model.TripResponse;
import hc.fms.api.report.model.fuel.Plugin;
import hc.fms.api.report.model.fuel.ReportDesc;
import hc.fms.api.report.model.tracker.request.GenerateRequest;
import hc.fms.api.report.model.tracker.request.SensorRequest;
import hc.fms.api.report.model.tracker.request.TrackerInfo;
import hc.fms.api.report.model.tracker.request.TripRequest;
import hc.fms.api.report.properties.FmsProperties;
import hc.fms.api.report.repository.FuelStatisticsRepository;
import hc.fms.api.report.repository.ReportGenRepository;
import hc.fms.api.report.util.HttpUtil;

@Service
public class TrackerService {
	private RestTemplate restTemplate = new RestTemplate();
	private Logger logger = LoggerFactory.getLogger(TrackerService.class);
	@Autowired
	private ReportProcessor reportProcessor;
	@Autowired
	private HttpHeaders basicUrlEncodedContentTypeHeaders;
	@Autowired
	private FmsProperties fmsProps;
	@Autowired
	private ParameterizedTypeReference<TripResponse> tripResponseTypeRef;
	@Autowired
	private ParameterizedTypeReference<SensorResponse> sensorResponseTypeRef;
	@Autowired
	private ParameterizedTypeReference<GroupResponse> groupResponseTypeRef;
	@Autowired
	private ParameterizedTypeReference<ReportGenResponse> reportGenResponseTypeRef;
	@Autowired
	private ParameterizedTypeReference<ReportResponse> reportConsumptionResponseTypeRef;
	@Autowired
	private ParameterizedTypeReference<TrackerResponse> trackerResponseTypeRef;
	
	@Autowired
	private FuelStatisticsRepository fuelStatRepository;
	@Autowired
	private ReportGenRepository reportGenRepository;
	public TrackerResponse getTrackerList(String hash) {
		MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
		map.add("hash", hash);
		TrackerResponse response = null;
		try {
			ResponseEntity<TrackerResponse> responseEntity = restTemplate.exchange(
					String.format("%s%s", fmsProps.getBaseUrl(), fmsProps.getApi().getTracker()), 
					HttpMethod.POST, 
					new HttpEntity<>(map, basicUrlEncodedContentTypeHeaders), 
					trackerResponseTypeRef
			);
			response= responseEntity.getBody();
		} catch(HttpStatusCodeException  e) {
			try {response = HttpUtil.getObjectMapper().readValue(e.getResponseBodyAsString(), TrackerResponse.class);} catch(Exception ex) { ex.printStackTrace();}
		}
		return response;
	}
	public TripResponse getTrip(TripRequest req) {
		return getTrip(req.getHash(), req.getTrackerId(), req.getFrom(), req.getTo());
	}
	public TripResponse getTrip(String hash, int trackerId, String from, String to) {
		MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
		map.add("tracker_id", String.valueOf(trackerId));
		map.add("from", from);
		map.add("to", to);
		map.add("hash", hash);
		TripResponse response = null;
		try {
			ResponseEntity<TripResponse> responseEntity = restTemplate.exchange(String.format("%s%s", fmsProps.getBaseUrl(), fmsProps.getApi().getTrip()), HttpMethod.POST, new HttpEntity<>(map, basicUrlEncodedContentTypeHeaders), tripResponseTypeRef);
			response= responseEntity.getBody();
		} catch(HttpStatusCodeException  e) {
			try {response = HttpUtil.getObjectMapper().readValue(e.getResponseBodyAsString(), TripResponse.class);} catch(Exception ex) {	ex.printStackTrace();}
		}
		return response;
	}
	public SensorResponse getSensorList(SensorRequest req) {
		return getSensorList(req.getHash(), req.getTrackerId());
	}
	public SensorResponse getSensorList(String hash, int trackerId) {
		MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
		map.add("tracker_id", String.valueOf(trackerId));
		map.add("hash", hash);
		SensorResponse response = null;
		try {
			ResponseEntity<SensorResponse> responseEntity = restTemplate.exchange(String.format("%s%s", fmsProps.getBaseUrl(), fmsProps.getApi().getTrackerSensor()), HttpMethod.POST, new HttpEntity<>(map, basicUrlEncodedContentTypeHeaders), sensorResponseTypeRef);
			response= responseEntity.getBody();
		} catch(HttpStatusCodeException  e) {
			try {response = HttpUtil.getObjectMapper().readValue(e.getResponseBodyAsString(), SensorResponse.class);} catch(Exception ex) {	ex.printStackTrace();}
		}
		return response;
	}
	public GroupResponse getGroupList(String hash) {
		MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
		map.add("hash", hash);
		GroupResponse response = null;
		try {
			ResponseEntity<GroupResponse> responseEntity = restTemplate.exchange(String.format("%s%s", fmsProps.getBaseUrl(), fmsProps.getApi().getTrackerGroup()), HttpMethod.POST, new HttpEntity<>(map, basicUrlEncodedContentTypeHeaders), groupResponseTypeRef);
			response= responseEntity.getBody();
		} catch(HttpStatusCodeException  e) {
			try {response = HttpUtil.getObjectMapper().readValue(e.getResponseBodyAsString(), GroupResponse.class);} catch(Exception ex) {	ex.printStackTrace();}
		}
		return response;
	}
	public ReportGenResponse requestReportGen(GenerateRequest req) {
		MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
		map.add("hash", req.getHash());
		map.add("from", req.getFrom());
		map.add("to", req.getTo());
		map.add("geocoder", req.getGeocoder());
		
		map.add("trackers", req.getTrackers());
		map.add("plugin", req.getPlugin());
		map.add("time_filter", req.getTimeFilter());
		ReportGenResponse response = null;
		try {
			ResponseEntity<ReportGenResponse> responseEntity = restTemplate.exchange(String.format("%s%s", fmsProps.getBaseUrl(), fmsProps.getApi().getReportGen()), HttpMethod.POST, new HttpEntity<>(map, basicUrlEncodedContentTypeHeaders), reportGenResponseTypeRef);
			response= responseEntity.getBody();
		} catch(HttpStatusCodeException  e) {
			try {response = HttpUtil.getObjectMapper().readValue(e.getResponseBodyAsString(), ReportGenResponse.class);} catch(Exception ex) {	ex.printStackTrace();}
		}
		return response;
	}
	public ReportResponse retrieveReport(String hash, long reportId) {
		MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
		map.add("hash", hash);
		map.add("report_id", String.valueOf(reportId));
		ReportResponse response = null;
		try {
			ResponseEntity<ReportResponse> responseEntity = restTemplate.exchange(String.format("%s%s", fmsProps.getBaseUrl(), fmsProps.getApi().getReportRetrieve()), HttpMethod.POST, new HttpEntity<>(map, basicUrlEncodedContentTypeHeaders), reportConsumptionResponseTypeRef);
			response= responseEntity.getBody();
		} catch(HttpStatusCodeException e) {
			//e.printStackTrace();
			try {response = HttpUtil.getObjectMapper().readValue(e.getResponseBodyAsString(), ReportResponse.class);} catch(Exception ex) {ex.printStackTrace();}
		}
		return response;
	}
	
	public ReportGenResponse generateReport(ReportGenFlatRequest req) {
		/*
		- trackers => [{trackerId:73, mileageSensorId: 222, fuleSensorId: 111} ,{trackerId:69, mileageSensorId: 224, fuelSensorId: 124}], from=> 2018-11-23 00:00:00, to=> 2018-11-23 23:59:59, detailsIntervalMinutes => 360 
		genReq.setTrackers(Arrays.asList(73, 69));
		genReq.setFrom("2018-11-23 00:00:00");
		genReq.setTo("2018-11-23 23:59:59");
		genReq.setTimeFilter(new TimeFilter());//default
		Plugin plugin = new Plugin();
		plugin.setDetailsIntervalMinutes(60 * 6);//default
		plugin.setShowAddress(true);//default
		plugin.setFilter(true);//default
		List<Plugin.Sensor> sensors = new ArrayList<>();
		Plugin.Sensor sensor = new Plugin.Sensor();
		sensor.setTrackerId(71);
		sensor.setSensorId(647);
		sensors.add(sensor);
		
		sensor = new Plugin.Sensor();
		sensor.setTrackerId(69);
		sensor.setSensorId(645);
		sensors.add(sensor);
		plugin.setSensors(sensors);
		 */
		List<TrackerInfo> infoList = req.getTrackers();
		List<Integer> trackerIdList = infoList.stream().map(info -> info.getTrackerId()).collect(Collectors.toList());
		
		String from = req.getFrom();
		String to = req.getTo();
		String hash = req.getHash();
		int detailsIntervalMinutes = req.getIntervalInMin();
		
		GenerateRequest fuelGenReq = new GenerateRequest();
		fuelGenReq.setHash(hash);

		fuelGenReq.setTrackers(trackerIdList);
		fuelGenReq.setFrom(from);
		fuelGenReq.setTo(to);
		
		Plugin plugin = new Plugin();
		plugin.setDetailsIntervalMinutes(detailsIntervalMinutes);
		//logger.info(infoList.stream().map(info -> new Plugin.Sensor(info.getTrackerId(), info.getFuelConsumptionSensorId())).collect(Collectors.toList()) + "");
		plugin.setSensors(infoList.stream().map(info -> new Plugin.Sensor(info.getTrackerId(), info.getFuelConsumptionSensorId())).collect(Collectors.toList()));
		fuelGenReq.setPlugin(plugin);
				
		
		GenerateRequest mileageGenReq = new GenerateRequest();
		mileageGenReq.setHash(hash);

		mileageGenReq.setTrackers(trackerIdList);
		mileageGenReq.setFrom(from);
		mileageGenReq.setTo(to);
		
		plugin = new Plugin();
		plugin.setDetailsIntervalMinutes(detailsIntervalMinutes);
		plugin.setSensors(infoList.stream().map(info -> new Plugin.Sensor(info.getTrackerId(), info.getHardwareMileageSensorId())).collect(Collectors.toList()));
		mileageGenReq.setPlugin(plugin);
		
		
		ReportGenResponse response = new ReportGenResponse();
		ReportGenResponse fuelGenResponse = requestReportGen(fuelGenReq);
		if(fuelGenResponse.isSuccess()) {
			ReportGenResponse mileageGenResponse = requestReportGen(mileageGenReq);
			if(mileageGenResponse.isSuccess()) {
				ReportGen reportGen = new ReportGen();
				reportGen.setFuelReportId(fuelGenResponse.getId());
				reportGen.setMileageReportId(mileageGenResponse.getId());
				reportGen.setLabel(req.getLabel());
				reportGen.setFrom(from);
				reportGen.setTo(to);
				final ReportGen reportGenSaved = reportProcessor.logReportGen(reportGen);
				response.setSuccess(true);
				response.setId(reportGenSaved.getId());
				ExecutorService execService = Executors.newSingleThreadExecutor();
				
				execService.submit(() -> {
					long elapsed = 0L, start = System.currentTimeMillis();
					long fuelReportId = reportGenSaved.getFuelReportId();
					long mileageReportId = reportGenSaved.getMileageReportId();
					ReportDesc fuelReport = null, mileageReport = null;
					while(true) {
						try {
							Thread.sleep(5 * 1000);
						} catch(Exception e) {
							e.printStackTrace();
						}
						if(fuelReport == null) {
							try {
								ReportResponse fuelReportResponse = retrieveReport(req.getHash(), fuelReportId);
								if(fuelReportResponse.isSuccess()) {
									fuelReport = fuelReportResponse.getReport();
								} else if(fuelReportResponse.getStatus().getCode() == 229) {
									logger.info("Fuel consumption report generation still in progress ..");
								} else if(fuelReportResponse.getStatus().getCode() == 204) {
									logger.info("Fuel consumption report not found");
									break;
								} else {
									logger.info("Fuel consumption report unknown error status " + fuelReportResponse);
								}
							} catch(Exception e) {
								e.printStackTrace();
							}
						}
						if(mileageReport == null) {
							try {
								ReportResponse mileageReportResponse = retrieveReport(req.getHash(), mileageReportId);
								if(mileageReportResponse.isSuccess()) {
									mileageReport = mileageReportResponse.getReport();
								} else if(mileageReportResponse.getStatus().getCode() == 229) {
									logger.info("Mileage report generation still in progress ..");
								} else if(mileageReportResponse.getStatus().getCode() == 204) {
									logger.info("Mileage report not found");
									break;
								} else {
									logger.info("Mileage report unknown error status " + mileageReportResponse);
								}
							} catch(Exception e) {
								//e.printStackTrace();
							}
						}
						if(fuelReport != null && mileageReport != null) {
							logger.info("retrieved both reports " + reportGenSaved);
							reportProcessor.process(reportGenSaved, fuelReport, mileageReport);
							break;
						}
						elapsed = System.currentTimeMillis() - start;
						if(elapsed > 60 * 1000 * 60) {
							break;
						}
					}
					logger.info("Report generation Thread exited");
				});
				execService.shutdown();
				
			} else {
				return mileageGenResponse;//fuel generation successful but faile to generate mileageReport 
			}
		} else {
			return fuelGenResponse;//fuel generation failed
		}
		return response;//all successful and created a single report reponse which includes both of the sub reports
	}
	public List<FuelStatResult> getFuelStatisticsResultListByReportId(Long reportId) {
		return fuelStatRepository.getFuelStatResultList(reportId);
	}
	public List<ReportGen> getReportGenList() {
		return reportGenRepository.findAllByOrderByCreatedDateDesc();
		
	}
	public List<Long> getReportGenListInProgress() {
		List<ReportGen> genList = reportGenRepository.findAllByFuelReportProcessed(false);
		return genList.stream().map(reportGen -> reportGen.getId()).collect(Collectors.toList());
	}
}
