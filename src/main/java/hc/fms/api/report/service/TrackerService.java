package hc.fms.api.report.service;

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

import hc.fms.api.report.model.GroupResponse;
import hc.fms.api.report.model.SensorResponse;
import hc.fms.api.report.model.TripResponse;
import hc.fms.api.report.model.tracker.request.SensorRequest;
import hc.fms.api.report.model.tracker.request.TripRequest;
import hc.fms.api.report.properties.FmsProperties;
import hc.fms.api.report.util.HttpUtil;

@Service
public class TrackerService {
	private RestTemplate restTemplate = new RestTemplate();
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
}
