package kr.re.keti.sc.datacoreusertool.api.widgetdashboard.service;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.datacoreusertool.api.dataservicebroker.service.DataServiceBrokerSVC;
import kr.re.keti.sc.datacoreusertool.api.dataservicebroker.vo.CommonEntityListResponseVO;
import kr.re.keti.sc.datacoreusertool.api.dataservicebroker.vo.CommonEntityVO;
import kr.re.keti.sc.datacoreusertool.api.dataservicebroker.vo.EntityRetrieveUIVO;
import kr.re.keti.sc.datacoreusertool.api.dataservicebroker.vo.EntityRetrieveVO;
import kr.re.keti.sc.datacoreusertool.api.map.service.MapSVC;
import kr.re.keti.sc.datacoreusertool.api.map.vo.MapSearchConditionBaseResponseVO;
import kr.re.keti.sc.datacoreusertool.api.subscription.service.SubscriptionSVC;
import kr.re.keti.sc.datacoreusertool.api.subscription.vo.SubscriptionUIVO;
import kr.re.keti.sc.datacoreusertool.api.subscription.vo.SubscriptionVO;
import kr.re.keti.sc.datacoreusertool.api.widgetdashboard.dao.WidgetDashboardDAO;
import kr.re.keti.sc.datacoreusertool.api.widgetdashboard.vo.WidgetChartDataVO;
import kr.re.keti.sc.datacoreusertool.api.widgetdashboard.vo.WidgetChartHistoryDataVO;
import kr.re.keti.sc.datacoreusertool.api.widgetdashboard.vo.WidgetChartMapDataVO;
import kr.re.keti.sc.datacoreusertool.api.widgetdashboard.vo.WidgetDashboardBaseResponseVO;
import kr.re.keti.sc.datacoreusertool.api.widgetdashboard.vo.WidgetDashboardBaseVO;
import kr.re.keti.sc.datacoreusertool.api.widgetdashboard.vo.WidgetDashboardFileUIVO;
import kr.re.keti.sc.datacoreusertool.api.widgetdashboard.vo.WidgetDashboardResponseVO;
import kr.re.keti.sc.datacoreusertool.api.widgetdashboard.vo.WidgetDashboardUIResponseVO;
import kr.re.keti.sc.datacoreusertool.api.widgetdashboard.vo.WidgetDashboardUIVO;
import kr.re.keti.sc.datacoreusertool.api.widgetdashboard.vo.WidgetDashboardVO;
import kr.re.keti.sc.datacoreusertool.api.widgetdashboard.vo.WidgetSessionVO;
import kr.re.keti.sc.datacoreusertool.common.code.Constants;
import kr.re.keti.sc.datacoreusertool.common.code.WidgetDashboardCode;
import kr.re.keti.sc.datacoreusertool.common.code.WidgetDashboardCode.ChartType;
import kr.re.keti.sc.datacoreusertool.common.component.Properties;
import kr.re.keti.sc.datacoreusertool.common.vo.ClientExceptionPayloadVO;
import kr.re.keti.sc.datacoreusertool.notification.vo.NotificationVO;
import kr.re.keti.sc.datacoreusertool.notification.vo.WidgetWebSocketRegistVO;
import kr.re.keti.sc.datacoreusertool.notification.websocket.WidgetWebSocketSessionManager;
import kr.re.keti.sc.datacoreusertool.scheduler.WidgetSchedulerService;
import kr.re.keti.sc.datacoreusertool.security.service.UserToolSecuritySVC;
import kr.re.keti.sc.datacoreusertool.util.ValidateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for management of widget dash board.
 * @FileName WidgetDashboardSVC.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class WidgetDashboardSVC {
	
	@Autowired
	private WidgetDashboardDAO widgetDashboardDAO;
	
	@Autowired
	private DataServiceBrokerSVC dataServiceBrokerSVC; 
	
	@Autowired
	private SubscriptionSVC subscriptionSVC;
	
	@Autowired
	private UserToolSecuritySVC userToolSecuritySVC;
	
	@Autowired
	private WidgetSchedulerService widgetSchedulerService;
	
	@Autowired
	private MapSVC mapSVC;
	
	@Autowired
	private Properties properties;
	
	final static String LEGEND = "legend";

	/**
	 * Create widget
	 * @param widgetDashboardUIVO		WidgetDashboardUIVO
	 * @return							Result of create widget.
	 */
	public ResponseEntity<WidgetDashboardUIResponseVO> createWidget(HttpServletRequest request, WidgetDashboardUIVO widgetDashboardUIVO) {
		String userId = userToolSecuritySVC.getUserId(request).getBody();
		
		if(ValidateUtil.isEmptyData(userId)) {
			log.warn("Authentication failure. userId: {}", userId);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		String widgetId = UUID.randomUUID().toString();
		widgetDashboardUIVO.setWidgetId(widgetId);
		WidgetDashboardVO widgetDashboardVO = makeWidgetDashboardVO(userId, widgetDashboardUIVO);

		try {
			widgetDashboardDAO.createWidget(widgetDashboardVO);
		} catch(DuplicateKeyException e) {
			log.warn("Duplicate widget. widgetId: {}", widgetDashboardUIVO.getWidgetId(), e);
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		} catch(Exception e) {
			log.error("Fail to createWidget.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

		WidgetDashboardUIResponseVO widgetDashboardUIResponseVO = new WidgetDashboardUIResponseVO();
		widgetDashboardUIResponseVO.setWidgetId(widgetId);
		widgetDashboardUIResponseVO.setUserId(userId);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(widgetDashboardUIResponseVO);
	}

	/**
	 * Update widget
	 * @param widgetDashboardUIVO	WidgetDashboardUIVO
	 * @return						Result of update widget.
	 */
	public ResponseEntity<WidgetDashboardUIResponseVO> updateWidget(HttpServletRequest request, WidgetDashboardUIVO widgetDashboardUIVO) {
		ResponseEntity<WidgetDashboardUIResponseVO> widgetDashboardUIResponse = null;
		String userId = userToolSecuritySVC.getUserId(request).getBody();
		
		if(ValidateUtil.isEmptyData(userId)) {
			log.warn("Authentication failure. userId: {}", userId);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		WidgetDashboardVO widgetDashboardVO = makeWidgetDashboardVO(userId, widgetDashboardUIVO);
		
		try {
			if(widgetDashboardDAO.updateWidget(widgetDashboardVO) > 0) {
				widgetDashboardUIResponse = getWidget(userId, widgetDashboardVO.getDashboardId(), widgetDashboardVO.getWidgetId());
			} else {
				log.error("Fail to update DB for widget data.");
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			}
		} catch(Exception e) {
			log.error("Fail to updateWidget.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} 
		
		WidgetDashboardUIResponseVO widgetDashboardUIResponseVO = null;
		if(widgetDashboardUIResponse != null) {
			widgetDashboardUIResponseVO = widgetDashboardUIResponse.getBody();
		}
		
		return ResponseEntity.ok().body(widgetDashboardUIResponseVO);
	}
	
	/**
	 * Update widget layout
	 * @param widgetDashboardUIVOs		List of WidgetDashboardUIVO
	 * @return							Result of update widget layout.
	 */
	public ResponseEntity<WidgetDashboardUIResponseVO> updateWidgetLayout(HttpServletRequest request, List<WidgetDashboardUIVO> widgetDashboardUIVOs) {
		String userId = userToolSecuritySVC.getUserId(request).getBody();
		
		if(ValidateUtil.isEmptyData(userId)) {
			log.warn("Authentication failure. userId: {}", userId);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		for(WidgetDashboardUIVO widgetDashboardUIVO : widgetDashboardUIVOs) {
			WidgetDashboardVO widgetDashboardVO = makeWidgetDashboardVO(userId, widgetDashboardUIVO);
			
			try {
				widgetDashboardDAO.updateWidgetLayout(widgetDashboardVO);
			} catch(Exception e) {
				log.error("Fail to updateWidget.", e);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			}
		}
		
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	/**
	 * Delete widget
	 * @param dashboardId	Dashboard ID
	 * @param widgetId		Widget ID
	 * @return				Result of delete widget.
	 */
	public ResponseEntity<ClientExceptionPayloadVO> deleteWidget(HttpServletRequest request, String dashboardId, String widgetId) {
		String userId = userToolSecuritySVC.getUserId(request).getBody();
		
		if(ValidateUtil.isEmptyData(userId)) {
			log.warn("Authentication failure. userId: {}", userId);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		WidgetDashboardVO widgetDashboardVO = new WidgetDashboardVO();
		widgetDashboardVO.setDashboardId(dashboardId);
		widgetDashboardVO.setWidgetId(widgetId);
		widgetDashboardVO.setUserId(userId);
		
		try {
			// 1. delete widget
			widgetDashboardDAO.deleteWidget(widgetDashboardVO);
		} catch(Exception e) {
			log.error("Fail to deleteWidget.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
	
	/**
	 * Retrieve widget
	 * @param userId		User ID
	 * @param dashboardId	Dashboard ID
	 * @param widgetId		Widget ID
	 * @return				Widget information retrieved by dashboard ID and widget ID.
	 */
	public ResponseEntity<WidgetDashboardUIResponseVO> getWidget(String userId, String dashboardId, String widgetId) {
		WidgetDashboardUIResponseVO result = null;
		WidgetDashboardVO widgetDashboard = null;
		
		if(ValidateUtil.isEmptyData(userId)) {
			log.warn("Authentication failure. userId: {}", userId);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		WidgetDashboardVO widgetDashboardVO = new WidgetDashboardVO();
		widgetDashboardVO.setWidgetId(widgetId);
		widgetDashboardVO.setUserId(userId);
		widgetDashboardVO.setDashboardId(dashboardId);
		
		try {
			widgetDashboard = widgetDashboardDAO.getWidget(widgetDashboardVO);
		} catch(Exception e) {
			log.error("Fail to getWidget.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		if(widgetDashboard == null) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		} else {
			result = widgetDashboardVOtoWidgetDashboardUIResponseVO(widgetDashboard);
		}
		
		return ResponseEntity.ok().body(result);
	}

	/**
	 * Retrieve all widgets included in the dashboard.
	 * @param userId		User ID
	 * @param dashboardId	Dashboard ID
	 * @return				List of widget retrieved by dashboard ID.
	 */
	public ResponseEntity<List<WidgetDashboardUIResponseVO>> getAllWidget(String userId, String dashboardId) {
		List<WidgetDashboardUIResponseVO> result = null;
		List<WidgetDashboardVO> widgetDashboardVOs= new ArrayList<WidgetDashboardVO>();
		
		if(ValidateUtil.isEmptyData(userId)) {
			log.warn("Authentication failure. userId: {}", userId);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		try {
			WidgetDashboardVO widgetDashboardVO = new WidgetDashboardVO();
			widgetDashboardVO.setUserId(userId);
			widgetDashboardVO.setDashboardId(dashboardId);
			widgetDashboardVOs = widgetDashboardDAO.getWidgets(widgetDashboardVO);
		} catch(Exception e) {
			log.error("Fail to getAllWidget.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		if(ValidateUtil.isEmptyData(widgetDashboardVOs)) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		} else {
			result = widgetDashboardVOsToWidgetDashboardUIResponseVOs(widgetDashboardVOs);
		}
		
		return ResponseEntity.ok().body(result);
	}
	
	/**
	 * Create file widget
	 * @param widgetDashboardFileUIVO	WidgetDashboardFileUIVO
	 * @return							Result of file widget creation.
	 */
	public ResponseEntity<WidgetDashboardUIResponseVO> createWidgetFile(HttpServletRequest request, WidgetDashboardFileUIVO widgetDashboardFileUIVO) {
		
		String userId = userToolSecuritySVC.getUserId(request).getBody();
		
		if(ValidateUtil.isEmptyData(userId)) {
			log.warn("Authentication failure. userId: {}", userId);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		WidgetDashboardUIVO widgetDashboardUIVO = null;
		try {
			widgetDashboardUIVO = new ObjectMapper().readValue(widgetDashboardFileUIVO.getWidgetDashboardUI(), WidgetDashboardUIVO.class);
		} catch (JsonMappingException e) {
			log.error("Fail to convert WidgetDashboardFileUIVO to WidgetDashboardUIVO.", e);
		} catch (JsonProcessingException e) {
			log.error("Fail to convert WidgetDashboardFileUIVO to WidgetDashboardUIVO.", e);
		}
		
		// 1. Validate required values
		if(widgetDashboardUIVO != null 
				&& ValidateUtil.isEmptyData(widgetDashboardUIVO.getChartType())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
		
		String widgetId = UUID.randomUUID().toString();
		widgetDashboardUIVO.setWidgetId(widgetId);
		WidgetDashboardVO widgetDashboardVO = makeWidgetDashboardVO(userId, widgetDashboardUIVO);
		
		try {
			if(ValidateUtil.isEmptyData(widgetDashboardFileUIVO.getFile())) {
				log.warn("No file to upload.");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
			}
			
			byte[] fileBytes = widgetDashboardFileUIVO.getFile().getBytes();
			
			widgetDashboardVO.setFile(fileBytes);
		} catch (IOException e) {
			log.error("Fail to get file.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

		try {
			widgetDashboardDAO.createWidget(widgetDashboardVO);
		} catch(DuplicateKeyException e) {
			log.warn("Duplicate widget. widgetId: {}", widgetDashboardUIVO.getWidgetId(), e);
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		} catch(Exception e) {
			log.error("Fail to createWidget.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		WidgetDashboardUIResponseVO widgetDashboardUIResponseVO = new WidgetDashboardUIResponseVO();
		widgetDashboardUIResponseVO.setWidgetId(widgetId);
		widgetDashboardUIResponseVO.setUserId(userId);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(widgetDashboardUIResponseVO);
	}
	
	/**
	 * Convert WidgetDashboardUIVO to WidgetDashboardVO
	 * @param userId				User ID
	 * @param widgetDashboardUIVO	WidgetDashboardUIVO
	 * @return						WidgetDashboardVO
	 */
	private WidgetDashboardVO makeWidgetDashboardVO(String userId, WidgetDashboardUIVO widgetDashboardUIVO) {
		WidgetDashboardVO widgetDashboardVO = new WidgetDashboardVO();
		
		widgetDashboardVO.setWidgetId(widgetDashboardUIVO.getWidgetId());
		widgetDashboardVO.setUserId(userId);
		widgetDashboardVO.setDashboardId(widgetDashboardUIVO.getDashboardId());
		widgetDashboardVO.setChartType(widgetDashboardUIVO.getChartType());
		widgetDashboardVO.setChartOrder(widgetDashboardUIVO.getChartOrder());
		widgetDashboardVO.setChartSize(widgetDashboardUIVO.getChartSize());
		widgetDashboardVO.setDataType(widgetDashboardUIVO.getDataType());
		widgetDashboardVO.setChartTitle(widgetDashboardUIVO.getChartTitle());
		widgetDashboardVO.setChartXName(widgetDashboardUIVO.getChartXName());
		widgetDashboardVO.setChartYName(widgetDashboardUIVO.getChartYName());
		widgetDashboardVO.setYAxisRange(widgetDashboardUIVO.getYAxisRange());
		widgetDashboardVO.setUpdateInterval(widgetDashboardUIVO.getUpdateInterval());
		if(widgetDashboardUIVO.getRealtimeUpdateEnabled() != null) {
			widgetDashboardVO.setRealtimeUpdateEnabled(widgetDashboardUIVO.getRealtimeUpdateEnabled());
		}
		widgetDashboardVO.setChartAttribute(widgetDashboardUIVO.getChartAttribute());
		
		try {
			if(widgetDashboardUIVO.getEntityRetrieveVO() != null) {
				ObjectMapper mapper = new ObjectMapper();
				String searchCondition = mapper.writeValueAsString(widgetDashboardUIVO.getEntityRetrieveVO());
				widgetDashboardVO.setSearchCondition(searchCondition);
			}
		} catch (JsonProcessingException e) {
			log.warn("Fail to convert object to json string. widgetId: {}", widgetDashboardUIVO.getWidgetId());
		}
		
		widgetDashboardVO.setMapSearchConditionId(widgetDashboardUIVO.getMapSearchConditionId());
		widgetDashboardVO.setExtention1(widgetDashboardUIVO.getExtention1());
		widgetDashboardVO.setExtention2(widgetDashboardUIVO.getExtention2());
		
		return widgetDashboardVO;
	}
	
	/**
	 * Convert WidgetDashboardVO to WidgetDashboardUIResponseVO
	 * @param widgetDashboardVO		WidgetDashboardVO
	 * @return						WidgetDashboardUIResponseVO
	 */
	private WidgetDashboardUIResponseVO widgetDashboardVOtoWidgetDashboardUIResponseVO(WidgetDashboardVO widgetDashboardVO) {
		WidgetDashboardUIResponseVO widgetDashboardUIResponseVO = new WidgetDashboardUIResponseVO();
		
		widgetDashboardUIResponseVO.setWidgetId(widgetDashboardVO.getWidgetId());
		widgetDashboardUIResponseVO.setUserId(widgetDashboardVO.getUserId());
		widgetDashboardUIResponseVO.setDashboardId(widgetDashboardVO.getDashboardId());
		widgetDashboardUIResponseVO.setChartType(widgetDashboardVO.getChartType());
		widgetDashboardUIResponseVO.setChartOrder(widgetDashboardVO.getChartOrder());
		widgetDashboardUIResponseVO.setChartSize(widgetDashboardVO.getChartSize());
		widgetDashboardUIResponseVO.setDataType(widgetDashboardVO.getDataType());
		widgetDashboardUIResponseVO.setChartTitle(widgetDashboardVO.getChartTitle());
		widgetDashboardUIResponseVO.setChartXName(widgetDashboardVO.getChartXName());
		widgetDashboardUIResponseVO.setChartYName(widgetDashboardVO.getChartYName());
		widgetDashboardUIResponseVO.setYAxisRange(widgetDashboardVO.getYAxisRange());
		widgetDashboardUIResponseVO.setUpdateInterval(widgetDashboardVO.getUpdateInterval());
		if(widgetDashboardVO.getRealtimeUpdateEnabled() != null) {
			widgetDashboardUIResponseVO.setRealtimeUpdateEnabled(widgetDashboardVO.getRealtimeUpdateEnabled());
		}
		widgetDashboardUIResponseVO.setChartAttribute(widgetDashboardVO.getChartAttribute());
		
		try {
			if(!ValidateUtil.isEmptyData(widgetDashboardVO.getSearchCondition())) {
				ObjectMapper mapper = new ObjectMapper();
				EntityRetrieveUIVO entityRetrieveVO = mapper.readValue(widgetDashboardVO.getSearchCondition(), EntityRetrieveUIVO.class);
				widgetDashboardUIResponseVO.setEntityRetrieveVO(entityRetrieveVO);
			}
		} catch (Exception e) {
			log.warn("Fail to convert json string to Object. widgetId: {}", widgetDashboardVO.getWidgetId(), e);
		}
		
		try {
			if(!ValidateUtil.isEmptyData(widgetDashboardVO.getMapSearchConditionId())) {
				ResponseEntity<MapSearchConditionBaseResponseVO> mapSearchCondition = mapSVC.getMapSearchCondition(widgetDashboardVO.getUserId(), 
						widgetDashboardVO.getMapSearchConditionId());
				if(!ValidateUtil.isEmptyData(mapSearchCondition.getBody())) {
					widgetDashboardUIResponseVO.setMapSearchConditionVO(mapSearchCondition.getBody());
				}
			}
		} catch(Exception e) {
			log.warn("Fail to get map search condition. mapSearchConditionId: {}", widgetDashboardVO.getMapSearchConditionId(), e);
		}
		
		widgetDashboardUIResponseVO.setFile(widgetDashboardVO.getFile());
		widgetDashboardUIResponseVO.setMapSearchConditionId(widgetDashboardVO.getMapSearchConditionId());
		widgetDashboardUIResponseVO.setExtention1(widgetDashboardVO.getExtention1());
		widgetDashboardUIResponseVO.setExtention2(widgetDashboardVO.getExtention2());
		
		return widgetDashboardUIResponseVO;
	}
	
	/**
	 * Convert WidgetDashboardVO to WidgetDashboardResponseVO
	 * @param widgetDashboardVO		WidgetDashboardVO
	 * @return						WidgetDashboardResponseVO
	 */
	private WidgetDashboardResponseVO widgetDashboardVOtoWidgetDashboardResponseVO(WidgetDashboardVO widgetDashboardVO) {
		WidgetDashboardResponseVO widgetDashboardResponseVO = new WidgetDashboardResponseVO();
		
		widgetDashboardResponseVO.setWidgetId(widgetDashboardVO.getWidgetId());
		widgetDashboardResponseVO.setUserId(widgetDashboardVO.getUserId());
		widgetDashboardResponseVO.setDashboardId(widgetDashboardVO.getDashboardId());
		widgetDashboardResponseVO.setChartType(widgetDashboardVO.getChartType());
		widgetDashboardResponseVO.setChartOrder(widgetDashboardVO.getChartOrder());
		widgetDashboardResponseVO.setChartSize(widgetDashboardVO.getChartSize());
		widgetDashboardResponseVO.setDataType(widgetDashboardVO.getDataType());
		widgetDashboardResponseVO.setChartTitle(widgetDashboardVO.getChartTitle());
		widgetDashboardResponseVO.setChartXName(widgetDashboardVO.getChartXName());
		widgetDashboardResponseVO.setChartYName(widgetDashboardVO.getChartYName());
		widgetDashboardResponseVO.setYAxisRange(widgetDashboardVO.getYAxisRange());
		widgetDashboardResponseVO.setUpdateInterval(widgetDashboardVO.getUpdateInterval());
		if(widgetDashboardVO.getRealtimeUpdateEnabled() != null) {
			widgetDashboardResponseVO.setRealtimeUpdateEnabled(widgetDashboardVO.getRealtimeUpdateEnabled());
		}
		widgetDashboardResponseVO.setChartAttribute(widgetDashboardVO.getChartAttribute());
		
		try {
			if(!ValidateUtil.isEmptyData(widgetDashboardVO.getSearchCondition())) {
				ObjectMapper mapper = new ObjectMapper();
				EntityRetrieveVO entityRetrieveVO = mapper.readValue(widgetDashboardVO.getSearchCondition(), EntityRetrieveVO.class);
				widgetDashboardResponseVO.setEntityRetrieveVO(entityRetrieveVO);
			}
		} catch (Exception e) {
			log.warn("Fail to convert json string to Object. widgetId: {}", widgetDashboardVO.getWidgetId());
		}
		
		try {
			if(!ValidateUtil.isEmptyData(widgetDashboardVO.getMapSearchConditionId())) {
				ResponseEntity<MapSearchConditionBaseResponseVO> mapSearchCondition = mapSVC.getMapSearchCondition(widgetDashboardVO.getUserId(), 
						widgetDashboardVO.getMapSearchConditionId());
				if(!ValidateUtil.isEmptyData(mapSearchCondition.getBody())) {
					widgetDashboardResponseVO.setMapSearchConditionVO(mapSearchCondition.getBody());
				}
			}
		} catch(Exception e) {
			log.warn("Fail to get map search condition. mapSearchConditionId: {}", widgetDashboardVO.getMapSearchConditionId(), e);
		}
		
		widgetDashboardResponseVO.setFile(widgetDashboardVO.getFile());
		widgetDashboardResponseVO.setMapSearchConditionId(widgetDashboardVO.getMapSearchConditionId());
		widgetDashboardResponseVO.setExtention1(widgetDashboardVO.getExtention1());
		widgetDashboardResponseVO.setExtention2(widgetDashboardVO.getExtention2());
		
		return widgetDashboardResponseVO;
	}

	/**
	 * Convert list of WidgetDashboardVO to list of WidgetDashboardUIResponseVO
	 * @param widgetDashboardVOs	List of WidgetDashboardVO
	 * @return						List of WidgetDashboardUIResponseVO
	 */
	private List<WidgetDashboardUIResponseVO> widgetDashboardVOsToWidgetDashboardUIResponseVOs(List<WidgetDashboardVO> widgetDashboardVOs) {
		List<WidgetDashboardUIResponseVO> widgetDashboardUIResponseVOs = new ArrayList<WidgetDashboardUIResponseVO>();
		
		for(WidgetDashboardVO widgetDashboardVO : widgetDashboardVOs) {
			WidgetDashboardUIResponseVO widgetDashboardUIResponseVO = widgetDashboardVOtoWidgetDashboardUIResponseVO(widgetDashboardVO);
			
			widgetDashboardUIResponseVOs.add(widgetDashboardUIResponseVO);
		}
		
		return widgetDashboardUIResponseVOs;
	}

	/**
	 * Request and set chart data
	 * @param widgetWebSocketRegistVO	WidgetWebSocketRegistVO
	 * @param sessionId					Session ID
	 */
	public void setRequestChartResult(WidgetWebSocketRegistVO widgetWebSocketRegistVO, String sessionId) {
		String dashboardId = widgetWebSocketRegistVO.getDashboardId();
		String widgetId = widgetWebSocketRegistVO.getWidgetId();
		String userId = widgetWebSocketRegistVO.getUserId();
		String updateType = null;
		HttpServletRequest request = DataServiceBrokerSVC.userRequest.get(userId);
		
		// 1. Retrieve widget info
		WidgetDashboardResponseVO widgetDashboardVO = getWidgetDashboardVO(dashboardId, widgetId, userId);
		EntityRetrieveVO entityRetrieveVO = widgetDashboardVO.getEntityRetrieveVO();
		String chartType = widgetDashboardVO.getChartType();
		String dataType = widgetDashboardVO.getDataType();
		
		// Set widget & session info
		WidgetSessionVO widgetSessionVO = new WidgetSessionVO();
		widgetSessionVO.setChartType(chartType);
		widgetSessionVO.setDataType(dataType);
		widgetSessionVO.setSessionId(sessionId);
		widgetSessionVO.setUserId(userId);
		widgetSessionVO.setWidgetId(widgetId);
		if(!ValidateUtil.isEmptyData(widgetDashboardVO.getExtention1()) && LEGEND.equals(widgetDashboardVO.getExtention1())) {
			widgetSessionVO.setLegend(widgetDashboardVO.getExtention2());
		}
		
		if(widgetDashboardVO.getUpdateInterval() != null && widgetDashboardVO.getUpdateInterval() > 0) {
			updateType = WidgetDashboardCode.UpdateType.PERIODIC.name();
		} else if(widgetDashboardVO.getRealtimeUpdateEnabled() != null && widgetDashboardVO.getRealtimeUpdateEnabled()) {
			updateType = WidgetDashboardCode.UpdateType.REALTIME.name();
		} else {
			// do nothing.
		}
		
		// 2. Data inquiry by chart and subscription/cycle renewal setting
		// Donut, Pie chart (only historical data)
		if(WidgetDashboardCode.ChartType.DONUT.getCode().equals(chartType)
				|| WidgetDashboardCode.ChartType.PIE.getCode().equals(chartType)) {
			if(WidgetDashboardCode.DataType.LAST.getCode().equals(dataType)) {
				// 1. check required values
				if(!validateEntityRetrieveVOforType(entityRetrieveVO, widgetId, userId)) {
					return;
				}
				
				// 2. retrieve latest data & send to websocket(widget)
				widgetSessionVO.setMultiEntities(true);
				retreiveLastAttributeAndSendToWidgetSession(entityRetrieveVO, widgetSessionVO);
				
				// Create a periodical schedule
				if(updateType != null && updateType.equals(WidgetDashboardCode.UpdateType.PERIODIC.name())) {
					int period = widgetDashboardVO.getUpdateInterval();
					if(period > 0) {
						widgetSessionVO.setPeriod(period);
						createLastAttributeTaskSchedule(entityRetrieveVO, widgetSessionVO);
					}
				}
			} else {
				log.warn("Not supported chart({}) data type({})", chartType, dataType);
			}
		}
		// Bar chart (historical/latest data)
		else if(WidgetDashboardCode.ChartType.BAR.getCode().equals(chartType)) {			
			if(WidgetDashboardCode.DataType.HISTORY.getCode().equals(dataType)) {
				// 1. check required values
				if(!validateEntityRetrieveVOforId(entityRetrieveVO, widgetId, userId)) {
					return;
				}
				
				// 2. retrieve historical data & send to websocket(widget)
				retreiveHistoryAttributeAndSendToWidgetSession(entityRetrieveVO, widgetSessionVO);
			}
			else if(WidgetDashboardCode.DataType.LAST.getCode().equals(dataType)) {
				// 1. check required values
				if(!validateEntityRetrieveVOforType(entityRetrieveVO, widgetId, userId)) {
					return;
				}
				
				// 2. retrieve latest data & send to websocket(widget)
				widgetSessionVO.setMultiEntities(true);
				retreiveLastAttributeAndSendToWidgetSession(entityRetrieveVO, widgetSessionVO);
			} else {
				log.warn("Not supported chart({}) data type({})", chartType, dataType);
			}
			
			// 3. Create a subscription
			if(updateType != null && updateType.equals(WidgetDashboardCode.UpdateType.REALTIME.name())) {
				createSubscription(entityRetrieveVO, dashboardId, widgetId, request);
			}
		}
		// Line chart (historical data only)
		else if(WidgetDashboardCode.ChartType.LINE.getCode().equals(chartType)) {
			if(WidgetDashboardCode.DataType.HISTORY.getCode().equals(dataType)) {
				
				// 1. check required values
				if(!validateEntityRetrieveVOforType(entityRetrieveVO, widgetId, userId)) {
					return;
				}
				
				// 2. retrieve historical data & send to websocket(widget)
				retreiveHistoryAttributeAndSendToWidgetSession(entityRetrieveVO, widgetSessionVO);
				
				// 3. Create a subscription
				if(updateType != null && updateType.equals(WidgetDashboardCode.UpdateType.REALTIME.name())) {
					createSubscription(entityRetrieveVO, dashboardId, widgetId, request);
				}
			} else {
				log.warn("Not supported chart({}) data type({})", chartType, dataType);
			}
		}
		// Text, Boolean chart (only latest data)
		else if(WidgetDashboardCode.ChartType.TEXT.getCode().equals(chartType)
				|| WidgetDashboardCode.ChartType.BOOLEAN.getCode().equals(chartType)) {
			if(WidgetDashboardCode.DataType.LAST.getCode().equals(dataType)) {
				// 1. check required values
				if(!validateEntityRetrieveVOforId(entityRetrieveVO, widgetId, userId)) {
					return;
				}
				
				// 2. retrieve latest data & send to websocket(widget)
				widgetSessionVO.setMultiEntities(false);
				retreiveLastAttributeAndSendToWidgetSession(entityRetrieveVO, widgetSessionVO);
				
				// 3. Create a subscription
				if(updateType != null && updateType.equals(WidgetDashboardCode.UpdateType.REALTIME.name())) {
					createSubscription(entityRetrieveVO, dashboardId, widgetId, request);
				}
			} else {
				log.warn("Not supported chart({}) data type({})", chartType, dataType);
			}
		}
		// Map chart (latest)
		else if(WidgetDashboardCode.ChartType.MAP_LATEST.getCode().equals(chartType)) {
			if(WidgetDashboardCode.DataType.LAST.getCode().equals(dataType)) {
				List<EntityRetrieveVO> entityRetrieveVOs = null;
				ObjectMapper mapper = new ObjectMapper();
				
				// 1. get entityRetrieveVOs
				try {
					if(!ValidateUtil.isEmptyData(widgetDashboardVO.getMapSearchConditionVO()) 
							&& !ValidateUtil.isEmptyData(widgetDashboardVO.getMapSearchConditionVO().getSearchCondition())) {
							entityRetrieveVOs = mapper.readValue(widgetDashboardVO.getMapSearchConditionVO().getSearchCondition(), new TypeReference<List<EntityRetrieveVO>>() {});
					}
				} catch (Exception e) {
					log.warn("Fail to convert json string to Object. widgetId: {}", widgetDashboardVO.getWidgetId());
				}
				
				// 2. check required values
				if(!validateEntityRetrieveVOsforType(entityRetrieveVOs, widgetId, userId)) {
					return;
				}
				
				// 3. retrieve latest data & send to websocket(widget)
				retreiveLastMapAttributeAndSendToWidgetSession(entityRetrieveVOs, widgetSessionVO);
				
				// 4. Create a subscription
				if(!ValidateUtil.isEmptyData(widgetDashboardVO.getMapSearchConditionVO().getSubscriptionCondition())) {
					try {
						List<EntityRetrieveVO> entityRetrieves = mapper.readValue(widgetDashboardVO.getMapSearchConditionVO().getSubscriptionCondition(), new TypeReference<List<EntityRetrieveVO>>() {});
						createSubscription(entityRetrieves, dashboardId, widgetId, request);
					} catch (Exception e) {
						log.warn("Fail to convert json string to Object. widgetId: {}", widgetDashboardVO.getWidgetId());
					}
				}
			} else {
				log.warn("Not supported chart({}) data type({})", chartType, dataType);
			}
		}
		// Histogram chart (historical/latest data)
		else if(WidgetDashboardCode.ChartType.HISTOGRAM.getCode().equals(chartType)) {
			// Set if x-axis unit values exist
			if(!ValidateUtil.isEmptyData(widgetDashboardVO.getExtention1())) {
				try {
					widgetSessionVO.setXAxisUnit(Integer.valueOf(widgetDashboardVO.getExtention1()));
				} catch(NumberFormatException e) {
					log.warn("The x axis units must be numeric. xAxisUnit={}", widgetDashboardVO.getExtention1(), e);
				}
			}
			
			if(WidgetDashboardCode.DataType.HISTORY.getCode().equals(dataType)) {
				// 1. check required values
				if(!validateEntityRetrieveVOforId(entityRetrieveVO, widgetId, userId)) {
					return;
				}
				
				// 2. retrieve historical data & send to websocket(widget)
				retreiveHistoryAttributeAndSendToWidgetSession(entityRetrieveVO, widgetSessionVO);
			}
			else if(WidgetDashboardCode.DataType.LAST.getCode().equals(dataType)) {
				// 1. check required values
				if(!validateEntityRetrieveVOforType(entityRetrieveVO, widgetId, userId)) {
					return;
				}
				
				// 2. retrieve latest data & send to websocket(widget)
				widgetSessionVO.setMultiEntities(true);
				retreiveLastAttributeAndSendToWidgetSession(entityRetrieveVO, widgetSessionVO);
			} else {
				log.warn("Not supported chart({}) data type({})", chartType, dataType);
			}
		}
		// Scatter chart
		else if(WidgetDashboardCode.ChartType.SCATTER.getCode().equals(chartType)) {
			if(WidgetDashboardCode.DataType.HISTORY.getCode().equals(dataType)) {
				// 1. check required values
				if(!validateEntityRetrieveVOforId(entityRetrieveVO, widgetId, userId)) {
					return;
				}
				
				// 2. retrieve historical data & send to websocket(widget)
				retreiveHistoryAttributeAndSendToWidgetSession(entityRetrieveVO, widgetSessionVO);
			}
			else if(WidgetDashboardCode.DataType.LAST.getCode().equals(dataType)) {
				// 1. check required values
				if(!validateEntityRetrieveVOforType(entityRetrieveVO, widgetId, userId)) {
					return;
				}
				
				// 2. retrieve latest data & send to websocket(widget)
				widgetSessionVO.setMultiEntities(true);
				retreiveLastAttributeAndSendToWidgetSession(entityRetrieveVO, widgetSessionVO);
			} else {
				log.warn("Not supported chart({}) data type({})", chartType, dataType);
			}
		}
		else {
			log.warn("Not supported chart type : {}", chartType);
		}
	}

	/**
	 * Check required values ​​of EntityRetrieveVO for ID
	 * @param entityRetrieveVO	EntityRetrieveVO
	 * @param widgetId			Widget ID
	 * @param userId			User ID
	 * @return					Valid: true, Invalid: false
	 */
	private boolean validateEntityRetrieveVOforId(EntityRetrieveVO entityRetrieveVO, String widgetId, String userId) {
		
		if(ValidateUtil.isEmptyData(entityRetrieveVO)
				|| ValidateUtil.isEmptyData(entityRetrieveVO.getId())
				|| (ValidateUtil.isEmptyData(entityRetrieveVO.getDataModelId())
				&& ValidateUtil.isEmptyData(entityRetrieveVO.getTypeUri()))) {
			log.warn("EntityRetrieveVO is not correct. type:{}, id:{}", entityRetrieveVO.getType(), entityRetrieveVO.getId());
			// TODO: Error message sent to websocket.
			return false;
		}
		
		return true;
	}
	
	/**
	 * Check required values ​​of EntityRetrieveVO for type
	 * @param entityRetrieveVO	EntityRetrieveVO
	 * @param widgetId			Widget ID
	 * @param userId			User ID
	 * @return					Valid: true, Invalid: false
	 */
	private boolean validateEntityRetrieveVOforType(EntityRetrieveVO entityRetrieveVO, String widgetId, String userId) {
		
		if(ValidateUtil.isEmptyData(entityRetrieveVO)
				|| (ValidateUtil.isEmptyData(entityRetrieveVO.getDataModelId()) && ValidateUtil.isEmptyData(entityRetrieveVO.getTypeUri()))) {
			log.warn("EntityRetrieveVO is not correct. type:{}, id:{}", entityRetrieveVO.getType(), entityRetrieveVO.getId());
			// TODO: Error message sent to websocket.
			return false;
		}
		
		return true;
	}
	
	/**
	 * Check required values ​​of list of EntityRetrieveVO for type
	 * @param entityRetrieveVOs		List of EntityRetrieveVO
	 * @param widgetId				Widget ID
	 * @param userId				User ID
	 * @return						Valid: true, Invalid: false
	 */
	private boolean validateEntityRetrieveVOsforType(List<EntityRetrieveVO> entityRetrieveVOs, String widgetId, String userId) {
		
		if(!ValidateUtil.isEmptyData(entityRetrieveVOs)) {
			for(EntityRetrieveVO entityRetrieveVO : entityRetrieveVOs) {
				if(ValidateUtil.isEmptyData(entityRetrieveVO.getDataModelId()) && ValidateUtil.isEmptyData(entityRetrieveVO.getTypeUri())) {
					log.warn("EntityRetrieveVO is not correct. type:{}, id:{}", entityRetrieveVO.getType(), entityRetrieveVO.getId());
					return false;
				}
			}
		} else {
			log.warn("EntityRetrieveVOs is empty.");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Retrieve widget setting info
	 * @param dashboardId	Dashboard ID
	 * @param widgetId		Widget ID
	 * @param userId		User ID
	 * @return				Widget data retrieved by dashboard ID and widget ID
	 */
	public WidgetDashboardResponseVO getWidgetDashboardVO(String dashboardId, String widgetId, String userId) {
		WidgetDashboardVO widgetDashboardVO = new WidgetDashboardVO();
		WidgetDashboardVO widgetDashboard = null;
		
		widgetDashboardVO.setDashboardId(dashboardId);
		widgetDashboardVO.setWidgetId(widgetId);
		widgetDashboardVO.setUserId(userId);
		
		try {
			
			widgetDashboard = widgetDashboardDAO.getWidget(widgetDashboardVO);
		} catch(Exception e) {
			log.error("Fail to getWidget.", e);
		}
		
		if(widgetDashboard == null || 
				(ValidateUtil.isEmptyData(widgetDashboard.getSearchCondition()) && ValidateUtil.isEmptyData(widgetDashboard.getMapSearchConditionId()))) {
			log.warn("No widget/chart setting info. dashboardId: {}, widgetId: {}, userId: {}", dashboardId, widgetId, userId);
			return null;
		}
		
		return widgetDashboardVOtoWidgetDashboardResponseVO(widgetDashboard);
	}
	
	/**
	 * Delete schedule
	 * @param scheduledId	Schedule ID
	 */
	public void deleteScheduler(String scheduledId) {
		widgetSchedulerService.remove(scheduledId);
	}
	
	/**
	 * Delete subscription
	 * @param subscriptionId	Subscription ID
	 */
	public void deleteSubscription(String subscriptionId) {
		ResponseEntity<List<SubscriptionVO>> subscriptionVOs = subscriptionSVC.getSubscriptions();
		if(subscriptionVOs != null && subscriptionVOs.getBody() != null) {
			for(SubscriptionVO subscription : subscriptionVOs.getBody()) {
				if(subscription.getId().equals(subscriptionId)) {
					subscriptionSVC.deleteSubscription(subscriptionId);
					break;
				}
			}
		}
	}
	
	/**
	 * retrieve historical data & send to websocket session
	 * @param entityRetrieveVO	EntityRetrieveVO
	 * @param widgetSessionVO	Widget Session VO
	 */
	private void retreiveHistoryAttributeAndSendToWidgetSession(EntityRetrieveVO entityRetrieveVO, WidgetSessionVO widgetSessionVO) {
		WidgetChartHistoryDataVO widgetChartHistoryDataVO = new WidgetChartHistoryDataVO();
		
		String originalAttributeId = null;
		
		// 1 Level to search (for observedAt)
		List<String> attrs = new ArrayList<String>();
		if(entityRetrieveVO.getAttrs() != null) {
			// Only one attribute is supported in the widget chart.
			originalAttributeId = entityRetrieveVO.getAttrs().get(0);
			String[] attrList = splitAttributeId(entityRetrieveVO.getAttrs().get(0));
			
			// only for scatter chart
			if(entityRetrieveVO.getAttrs().size() == 2 
					&& ChartType.SCATTER.getCode().equals(widgetSessionVO.getChartType())) {
				originalAttributeId += ";" + entityRetrieveVO.getAttrs().get(1);
				
				String[] attrList2 = splitAttributeId(entityRetrieveVO.getAttrs().get(1));
				if(!ValidateUtil.isEmptyData(attrList)
						&& !ValidateUtil.isEmptyData(attrList2)) {
					attrs.add(attrList[0]);
					attrs.add(attrList2[0]);
					entityRetrieveVO.setAttrs(attrs);
				}
			}
			else if(!ValidateUtil.isEmptyData(attrList)) {
				attrs.add(attrList[0]);
				entityRetrieveVO.setAttrs(attrs);
			}
		}
		
		ResponseEntity<CommonEntityListResponseVO> result = 
				dataServiceBrokerSVC.getEntitiesHistory(entityRetrieveVO, null, widgetSessionVO.getUserId());
		
		
		CommonEntityListResponseVO legends = null;
		if(!ValidateUtil.isEmptyData(widgetSessionVO.getLegend())) {
			EntityRetrieveVO legendRetrieveVO = createLegendRetrieveVO(entityRetrieveVO, widgetSessionVO.getLegend());
					
			ResponseEntity<CommonEntityListResponseVO> legendResult = 
					dataServiceBrokerSVC.getEntities(false, legendRetrieveVO, null, widgetSessionVO.getUserId());
			legends = legendResult.getBody();
		}
		
		if(result == null || result.getBody() == null) {
			return;
		}
		
		List<CommonEntityVO> commonEntityVOs = result.getBody().getCommonEntityVOs();
		
		if(commonEntityVOs == null || commonEntityVOs.size() < 1) {
			return;
		}
		
		List<CommonEntityVO> legendCommonEntity = null;
		if(legends != null) {
			legendCommonEntity = legends.getCommonEntityVOs();
		}
		
		widgetChartHistoryDataVO.setTotalCount(result.getBody().getTotalCount());
		widgetChartHistoryDataVO.setData(commonEntityVOs);
		widgetChartHistoryDataVO.setWidgetId(widgetSessionVO.getWidgetId());
		widgetChartHistoryDataVO.setChartType(widgetSessionVO.getChartType());
		widgetChartHistoryDataVO.setDataType(widgetSessionVO.getDataType());
		widgetChartHistoryDataVO.setAttributeId(originalAttributeId);
		
		String message = commonEntityResponseVOtoMessage(widgetChartHistoryDataVO, legendCommonEntity, widgetSessionVO);		
		
		sendToWidgetEntitySession(widgetSessionVO.getUserId(), widgetSessionVO.getWidgetId(), message, widgetSessionVO.getSessionId());
	}

	/**
	 * Create entityRetrieveVO for legend
	 * @param entityRetrieveVO	Entity retrieve VO
	 * @param legend			Legend attribute
	 * @return					EntityRetrieveVO
	 */
	private EntityRetrieveVO createLegendRetrieveVO(EntityRetrieveVO entityRetrieveVO, String legend) {
		EntityRetrieveVO legendRetrieveVO = new EntityRetrieveVO();
		List<String> attrs = new ArrayList<String>();
		String[] attrList = splitAttributeId(legend);
		attrs.add(attrList[0]);
		
		legendRetrieveVO.setAttrs(attrs);
		legendRetrieveVO.setDataModelId(entityRetrieveVO.getDataModelId());
		legendRetrieveVO.setId(entityRetrieveVO.getId());
		legendRetrieveVO.setOptions(entityRetrieveVO.getOptions());
		legendRetrieveVO.setQ(entityRetrieveVO.getQ());
		legendRetrieveVO.setType(entityRetrieveVO.getType());
		legendRetrieveVO.setTypeUri(entityRetrieveVO.getTypeUri());
		
		return legendRetrieveVO;
	}

	/**
	 * Retrieve latest map data
	 * @param entityRetrieveVOs		List of EntityRetrieveVO
	 * @param widgetSessionVO		Widget Session VO
	 */
	private void retreiveLastMapAttributeAndSendToWidgetSession(List<EntityRetrieveVO> entityRetrieveVOs, WidgetSessionVO widgetSessionVO) {
		
		WidgetChartMapDataVO widgetChartMapDataVO = new WidgetChartMapDataVO();
		widgetChartMapDataVO.setWidgetId(widgetSessionVO.getWidgetId());
		widgetChartMapDataVO.setChartType(widgetSessionVO.getChartType());
		widgetChartMapDataVO.setDataType(widgetSessionVO.getDataType());
		
		ResponseEntity<List<CommonEntityListResponseVO>> results = 
				dataServiceBrokerSVC.getEntitiesbyMultiModel(entityRetrieveVOs, null, widgetSessionVO.getUserId());
		
		if(ValidateUtil.isEmptyData(results) || ValidateUtil.isEmptyData(results.getBody())) {
			log.debug("No data was retrieved. userId: {}, widgetId: {}", widgetSessionVO.getUserId(), widgetSessionVO.getWidgetId());
			return;
		}
		
		widgetChartMapDataVO.setData(results.getBody());
		
		String message = CommonEntityListResponseVOtoMessage(widgetChartMapDataVO);
		
		sendToWidgetEntitySession(widgetSessionVO.getUserId(), widgetSessionVO.getWidgetId(), message, widgetSessionVO.getSessionId());
	}

	/**
	 * Retrieve latest data
	 * @param entityRetrieveVO	EntityRetrieveVO
	 * @param widgetSessionVO	Widget Session VO
	 */
	private void retreiveLastAttributeAndSendToWidgetSession(EntityRetrieveVO entityRetrieveVO, WidgetSessionVO widgetSessionVO) {
		
		WidgetChartDataVO widgetChartDataVO = new WidgetChartDataVO();
		widgetChartDataVO.setWidgetId(widgetSessionVO.getWidgetId());
		widgetChartDataVO.setChartType(widgetSessionVO.getChartType());
		widgetChartDataVO.setDataType(widgetSessionVO.getDataType());
		
		String originalAttributeId = null;
		
		// 1 Level to search (for observedAt)
		List<String> attrs = new ArrayList<String>();
		if(entityRetrieveVO.getAttrs() != null) {
			// Only one attribute is supported in the widget chart.
			originalAttributeId = entityRetrieveVO.getAttrs().get(0);
			String[] attrList = splitAttributeId(entityRetrieveVO.getAttrs().get(0));
			
			// only for scatter chart
			if(entityRetrieveVO.getAttrs().size() == 2 
					&& ChartType.SCATTER.getCode().equals(widgetSessionVO.getChartType())) {
				originalAttributeId += ";" + entityRetrieveVO.getAttrs().get(1);
				
				String[] attrList2 = splitAttributeId(entityRetrieveVO.getAttrs().get(1));
				if(!ValidateUtil.isEmptyData(attrList)
						&& !ValidateUtil.isEmptyData(attrList2)) {
					attrs.add(attrList[0]);
					attrs.add(attrList2[0]);
					entityRetrieveVO.setAttrs(attrs);
				}
			}
			else if(!ValidateUtil.isEmptyData(attrList)) {
				attrs.add(attrList[0]);
				entityRetrieveVO.setAttrs(attrs);
			}
		}
		
		if(widgetSessionVO.isMultiEntities()) {
			ResponseEntity<CommonEntityListResponseVO> results = 
					dataServiceBrokerSVC.getEntities(false, entityRetrieveVO, null, widgetSessionVO.getUserId());
			
			if(ValidateUtil.isEmptyData(results) || ValidateUtil.isEmptyData(results.getBody())) {
				log.debug("No data was retrieved. userId: {}, widgetId: {}", widgetSessionVO.getUserId(), widgetSessionVO.getWidgetId());
				return;
			}
			
			widgetChartDataVO.setData(results.getBody().getCommonEntityVOs());
			
			String attributeId = results.getBody().getAttributeId(); 
			if(attributeId != null) {
				widgetChartDataVO.setAttributeId(attributeId);
			} else {
				widgetChartDataVO.setAttributeId(originalAttributeId);
			}
		} 
		else {
			ResponseEntity<CommonEntityVO> result = 
					dataServiceBrokerSVC.getEntityById(entityRetrieveVO.getId(), entityRetrieveVO, null, widgetSessionVO.getUserId());
			List<CommonEntityVO> commonEntityVOs = new ArrayList<CommonEntityVO>();
			
			if(result == null) {
				log.debug("No data was retrieved. userId: {}, widgetId: {}", widgetSessionVO.getUserId(), widgetSessionVO.getWidgetId());
				return;
			}
			
			commonEntityVOs.add(result.getBody());
			
			widgetChartDataVO.setData(commonEntityVOs);
			widgetChartDataVO.setAttributeId(originalAttributeId);
		}
		
		CommonEntityListResponseVO legends = null;
		if(!ValidateUtil.isEmptyData(widgetSessionVO.getLegend())) {
			EntityRetrieveVO legendRetrieveVO = createLegendRetrieveVO(entityRetrieveVO, widgetSessionVO.getLegend());
					
			ResponseEntity<CommonEntityListResponseVO> legendResult = 
					dataServiceBrokerSVC.getEntities(false, legendRetrieveVO, null, originalAttributeId);
			legends = legendResult.getBody();
		}
		
		List<CommonEntityVO> legendCommonEntity = null;
		if(legends != null) {
			legendCommonEntity = legends.getCommonEntityVOs();
		}
		
		// Set to original attrs.
		attrs.clear();
		attrs.add(originalAttributeId);
		entityRetrieveVO.setAttrs(attrs);
		
		String message = commonEntityResponseVOtoMessage(widgetChartDataVO, legendCommonEntity, widgetSessionVO);
		
		sendToWidgetEntitySession(widgetSessionVO.getUserId(), widgetSessionVO.getWidgetId(), message, widgetSessionVO.getSessionId());
	}

	/**
	 * create schedule (retrieve latest data)
	 * @param entityRetrieveVO	EntityRetrieveVO
	 * @param widgetSessionVO	Widget Session VO
	 */
	private void createLastAttributeTaskSchedule(EntityRetrieveVO entityRetrieveVO, WidgetSessionVO widgetSessionVO) {
		Runnable runnalbe = new Runnable() {
			@Override
			public void run() {
				retreiveLastAttributeAndSendToWidgetSession(entityRetrieveVO, widgetSessionVO);
			}
		};
		
		String scheduledId = widgetSessionVO.getUserId() + ":" + widgetSessionVO.getWidgetId();
		widgetSchedulerService.register(runnalbe, scheduledId, widgetSessionVO.getPeriod() * 1000);
	}
	
	/**
	 * create schedule (retrieve historical data)
	 * @param entityRetrieveVO	EntityRetrieveVO
	 * @param widgetSessionVO	Widget Session VO
	 */
	private void createHistoryAttributeTaskSchedule(EntityRetrieveVO entityRetrieveVO, WidgetSessionVO widgetSessionVO) {
		Runnable runnalbe = new Runnable() {
			@Override
			public void run() {
				retreiveHistoryAttributeAndSendToWidgetSession(entityRetrieveVO, widgetSessionVO);
			}
		};
		
		String scheduledId = widgetSessionVO.getUserId() + ":" + widgetSessionVO.getWidgetId();
		widgetSchedulerService.register(runnalbe, scheduledId, widgetSessionVO.getPeriod() * 1000);
	}
	
	/**
	 * create subscription
	 * @param entityRetrieveVO	EntityRetrieveVO
	 * @param dashboardId		Dashboard ID
	 * @param widgetId			Widget ID
	 */
	private void createSubscription(EntityRetrieveVO entityRetrieveVO, String dashboardId, String widgetId, HttpServletRequest request) {
		List<SubscriptionUIVO> subscriptionUIVOs = new ArrayList<SubscriptionUIVO>();
		SubscriptionUIVO subscriptionUIVO = new SubscriptionUIVO();
		
		subscriptionUIVO.setTypeUri(entityRetrieveVO.getTypeUri());
		subscriptionUIVO.setId(entityRetrieveVO.getId());
		subscriptionUIVO.setAttrs(entityRetrieveVO.getAttrs());
		subscriptionUIVO.setWidgetId(widgetId);
		subscriptionUIVO.setDashboardId(dashboardId);
		
		subscriptionUIVOs.add(subscriptionUIVO);
		
		subscriptionSVC.createSubscription(subscriptionUIVOs, request);
	}
	
	/**
	 * create subscription
	 * @param entityRetrieveVOs		List of EntityRetrieveVO
	 * @param dashboardId			Dashboard ID
	 * @param widgetId				Widget ID
	 */
	private void createSubscription(List<EntityRetrieveVO> entityRetrieveVOs, String dashboardId, String widgetId, HttpServletRequest request) {
		List<SubscriptionUIVO> subscriptionUIVOs = new ArrayList<SubscriptionUIVO>();
		
		for(EntityRetrieveVO entityRetrieveVO : entityRetrieveVOs) {
			SubscriptionUIVO subscriptionUIVO = new SubscriptionUIVO();
			
			subscriptionUIVO.setTypeUri(entityRetrieveVO.getTypeUri());
			subscriptionUIVO.setId(entityRetrieveVO.getId());
			subscriptionUIVO.setAttrs(entityRetrieveVO.getAttrs());
			subscriptionUIVO.setWidgetId(widgetId);
			subscriptionUIVO.setDashboardId(dashboardId);
			
			subscriptionUIVOs.add(subscriptionUIVO);
		}
		
		subscriptionSVC.createSubscription(subscriptionUIVOs, request);
	}
	
	/**
	 * Send websocket message
	 * @param userId		User ID
	 * @param widgetId		Widget ID
	 * @param message		Websocket message
	 * @param sessionId		Session ID
	 */
	private void sendToWidgetEntitySession(String userId, String widgetId, String message, String sessionId) {
		NotificationVO notificationVO = new NotificationVO();
		notificationVO.setSubscriptionId(userId + ":" + widgetId);
		
		WidgetWebSocketSessionManager.INSTANCE.sendToEntitySession(notificationVO, message, sessionId);
	}
	
	/**
	 * Convert WidgetChartDataVO to String message
	 * @param widgetChartDataVO	WidgetChartDataVO
	 * @return 					commonEntityResponse message
	 */
	private String commonEntityResponseVOtoMessage(WidgetChartDataVO widgetChartDataVO,
			List<CommonEntityVO> legendCommonEntity, WidgetSessionVO widgetSessionVO) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	    objectMapper.setDateFormat(new SimpleDateFormat(Constants.CONTENT_DATE_FORMAT));
	     
	    List<CommonEntityVO> commonEntities = widgetChartDataVO.getData();
	    String attributeId = widgetChartDataVO.getAttributeId();
	    List<String> entityIds = null;
	    
	    if(!ValidateUtil.isEmptyData(commonEntities)) {
	    	// Data processing for Scatter chart
	    	if(ChartType.SCATTER.getCode().equals(widgetChartDataVO.getChartType())) {
	    		String[] attrs = attributeId.split(";");
	    		
	    		if(attrs.length != 2) {
	    			log.warn("The number of attribute IDs is not 2. AttributeIds: {}", attributeId);
	    			return null;
	    		}
	    		
	    		String[] xAttrIds = splitAttributeId(attrs[0]);
	    		String[] yAttrIds = splitAttributeId(attrs[1]);
	    		
	    		if(xAttrIds != null && yAttrIds != null) {
	    			attributeId = xAttrIds[xAttrIds.length - 1] + "," + yAttrIds[yAttrIds.length - 1];
	    			entityIds = addChartValue(commonEntities, xAttrIds, yAttrIds);
	    		}
	    		
				List<CommonEntityVO> scatterChartData = convertScatterData(commonEntities);
				widgetChartDataVO.setData(scatterChartData);
	    	}
	    	else {
	    		String[] attrIds = splitAttributeId(attributeId);
		    	
		    	if(attrIds != null) {
		    		attributeId = attrIds[attrIds.length - 1];
		    		entityIds = addChartValue(commonEntities, attrIds, null);
		    	}
		    	
		    	// Data processing for Histogram chart
				if(ChartType.HISTOGRAM.getCode().equals(widgetChartDataVO.getChartType())) {
					List<CommonEntityVO> histogramChartData = convertHistogramData(commonEntities, widgetSessionVO.getXAxisUnit());
					widgetChartDataVO.setData(histogramChartData);
				}
	    	}
	    }
	    widgetChartDataVO.setAttributeId(attributeId);
	    
	    // add Entity Id
	    widgetChartDataVO.setEntityIds(entityIds);
	    
	    // add Legend value
	    if(legendCommonEntity != null && widgetSessionVO.getLegend() != null) {
	    	List<String> legendValues = extractLegends(entityIds, legendCommonEntity, widgetSessionVO.getLegend());
		    widgetChartDataVO.setLegendvalues(legendValues);
	    }
	    
	    String message = null;
		try {
			message = objectMapper.writeValueAsString(widgetChartDataVO);
		} catch(JsonProcessingException e) {
			log.error("widgetChartDataVO parse error.", e);
			return null;
		}
		
		return message;
	}
	
	/**
	 * Convert WidgetChartHistoryDataVO to String message
	 * @param widgetChartHistoryDataVO	WidgetChartHistoryDataVO
	 * @param legendCommonEntity 
	 * @param widgetSessionVO
	 * @return							WidgetChartHistoryData message 
	 */
	private String commonEntityResponseVOtoMessage(WidgetChartHistoryDataVO widgetChartHistoryDataVO, 
			List<CommonEntityVO> legendCommonEntity, WidgetSessionVO widgetSessionVO) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	    objectMapper.setDateFormat(new SimpleDateFormat(Constants.CONTENT_DATE_FORMAT));
	    
	    List<CommonEntityVO> commonEntities = widgetChartHistoryDataVO.getData();
	    String attributeId = widgetChartHistoryDataVO.getAttributeId();
	    List<String> entityIds = null;
	    
	    if(!ValidateUtil.isEmptyData(commonEntities)) {
	    	// Data processing for Scatter chart
	    	if(ChartType.SCATTER.getCode().equals(widgetChartHistoryDataVO.getChartType())) {
	    		String[] attrs = attributeId.split(";");
	    		
	    		if(attrs.length != 2) {
	    			log.warn("The number of attribute IDs is not 2. AttributeIds: {}", attributeId);
	    			return null;
	    		}
	    		
	    		String[] xAttrIds = splitAttributeId(attrs[0]);
	    		String[] yAttrIds = splitAttributeId(attrs[1]);
	    		
	    		if(xAttrIds != null && yAttrIds != null) {
	    			attributeId = xAttrIds[xAttrIds.length - 1] + "," + yAttrIds[yAttrIds.length - 1];
	    			entityIds = addChartValue(commonEntities, xAttrIds, yAttrIds);
	    		}
	    		
				List<CommonEntityVO> scatterChartData = convertScatterData(commonEntities);
				widgetChartHistoryDataVO.setData(scatterChartData);
	    	}
	    	else {
	    		String[] attrIds = splitAttributeId(attributeId);
		    	
	    		if(attrIds != null) {
	    			attributeId = attrIds[attrIds.length - 1];
		    		entityIds = addChartValue(commonEntities, attrIds, null);
		    	}
				
				// Data processing for Histogram chart
				if(ChartType.HISTOGRAM.getCode().equals(widgetChartHistoryDataVO.getChartType())) {
					List<CommonEntityVO> histogramChartData = convertHistogramData(commonEntities, widgetSessionVO.getXAxisUnit());
					widgetChartHistoryDataVO.setData(histogramChartData);
				}
	    	}
	    }
	    
	    widgetChartHistoryDataVO.setAttributeId(attributeId);
	    
	    // add Entity Id
	    widgetChartHistoryDataVO.setEntityIds(entityIds);
	    
	    // add Legend value
	    if(legendCommonEntity != null && widgetSessionVO.getLegend() != null
	    		&& !ChartType.SCATTER.getCode().equals(widgetChartHistoryDataVO.getChartType())) {
	    	List<String> legendValues = extractLegends(entityIds, legendCommonEntity, widgetSessionVO.getLegend());
		    widgetChartHistoryDataVO.setLegendvalues(legendValues);
	    }
	     
	    String message = null;
		try {
			message = objectMapper.writeValueAsString(widgetChartHistoryDataVO);
		} catch(JsonProcessingException e) {
			log.error("widgetChartDataVO parse error.", e);
			return null;
		}
		
		return message;
	}

	/**
	 * Convert WidgetChartMapDataVO to String message
	 * @param widgetChartMapDataVO	WidgetChartMapDataVO
	 * @return						WidgetChartMapData message
	 */
	private String CommonEntityListResponseVOtoMessage(WidgetChartMapDataVO widgetChartMapDataVO) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	    objectMapper.setDateFormat(new SimpleDateFormat(Constants.CONTENT_DATE_FORMAT));
	    
	    String message = null;
		try {
			message = objectMapper.writeValueAsString(widgetChartMapDataVO);
		} catch(JsonProcessingException e) {
			log.error("widgetChartDataVO parse error.", e);
			return null;
		}
		
		return message;
	}

	/**
	 * Create dashboard
	 * @param widgetDashboardBaseVO		WidgetDashboardBaseVO
	 * @return							Result of dashboard creation.
	 */
	public ResponseEntity<WidgetDashboardBaseVO> createDashboard(HttpServletRequest request,
			WidgetDashboardBaseVO widgetDashboardBaseVO) {
		String userId = userToolSecuritySVC.getUserId(request).getBody();
		
		if(ValidateUtil.isEmptyData(userId)) {
			log.warn("Authentication failure. userId: {}", userId);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		String dashboardId = UUID.randomUUID().toString();
		widgetDashboardBaseVO.setDashboardId(dashboardId);
		widgetDashboardBaseVO.setUserId(userId);

		try {
			widgetDashboardDAO.createDashboard(widgetDashboardBaseVO);
		} catch(DuplicateKeyException e) {
			log.warn("Duplicate dashboard. dashboardId: {}", widgetDashboardBaseVO.getDashboardId(), e);
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		} catch(Exception e) {
			log.error("Fail to createDashboard.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		return ResponseEntity.status(HttpStatus.CREATED).body(widgetDashboardBaseVO);
	}

	/**
	 * Update dashboard
	 * @param widgetDashboardBaseVO		WidgetDashboardBaseVO (update data)
	 * @return							Result of update dashboard.
	 */
	public ResponseEntity<WidgetDashboardBaseVO> updateDashboard(HttpServletRequest request,
			WidgetDashboardBaseVO widgetDashboardBaseVO) {
		String userId = userToolSecuritySVC.getUserId(request).getBody();
		
		if(ValidateUtil.isEmptyData(userId)) {
			log.warn("Authentication failure. userId: {}", userId);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		widgetDashboardBaseVO.setUserId(userId);
		
		try {
			if(widgetDashboardDAO.updateDashboard(widgetDashboardBaseVO) <= 0) {
				log.error("Fail to update DB for dashboard data.");
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			}
		} catch(Exception e) {
			log.error("Fail to updateDashboard.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} 
		
		return ResponseEntity.ok().body(widgetDashboardBaseVO);
	}

	/**
	 * Delete dashboard
	 * @param dashboardId		Dashboard ID
	 * @return					Result of delete dashboard.
	 */
	public ResponseEntity<ClientExceptionPayloadVO> deleteDashboard(HttpServletRequest request, String dashboardId) {
		String userId = userToolSecuritySVC.getUserId(request).getBody();
		
		if(ValidateUtil.isEmptyData(userId)) {
			log.warn("Authentication failure. userId: {}", userId);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		WidgetDashboardBaseVO widgetDashboardBaseVO = new WidgetDashboardBaseVO();
		widgetDashboardBaseVO.setDashboardId(dashboardId);
		widgetDashboardBaseVO.setUserId(userId);
		
		try {
			widgetDashboardDAO.deleteDashboard(widgetDashboardBaseVO);
		} catch(Exception e) {
			log.error("Fail to deleteDashboard.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	/**
	 * Retrieve all dashboard
	 * @return	List of all registered dashboards. 
	 */
	public ResponseEntity<List<WidgetDashboardBaseResponseVO>> getDashboards(HttpServletRequest request) {
		List<WidgetDashboardBaseResponseVO> widgetDashboardBaseResponseVOs = new ArrayList<WidgetDashboardBaseResponseVO>();
		List<WidgetDashboardBaseVO> widgetDashboardBaseVOs;
		
		String userId = userToolSecuritySVC.getUserId(request).getBody();
		
		if(ValidateUtil.isEmptyData(userId)) {
			log.warn("Authentication failure. userId: {}", userId);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		try {
			widgetDashboardBaseVOs = widgetDashboardDAO.getDashboards(userId);
		} catch(Exception e) {
			log.error("Fail to getDashboards.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		if(ValidateUtil.isEmptyData(widgetDashboardBaseVOs)) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		} else {
			for(WidgetDashboardBaseVO widgetDashboardBaseVO : widgetDashboardBaseVOs) {
				WidgetDashboardBaseResponseVO widgetDashboardBaseResponseVO = new WidgetDashboardBaseResponseVO();
				widgetDashboardBaseResponseVO.setDashboardId(widgetDashboardBaseVO.getDashboardId());
				widgetDashboardBaseResponseVO.setDashboardName(widgetDashboardBaseVO.getDashboardName());
				
				widgetDashboardBaseResponseVOs.add(widgetDashboardBaseResponseVO);
			}
		}
		
		return ResponseEntity.ok().body(widgetDashboardBaseResponseVOs);
	}

	/**
	 * Add chart value
	 * @param commonEntities	List of CommonEntityVO
	 * @param attrIds			List of attribute id
	 * @return					List of entity id
	 */
	private List<String> addChartValue(List<CommonEntityVO> commonEntities, String[] attrIds, String[] attrIds2) {
		Object entity = null;
		Map<String, String> entityIds = new HashMap<String, String>();
		List<String> result = new ArrayList();
		
		for(CommonEntityVO commonEntityVO : commonEntities) {
			CommonEntityVO tempCommonEntity = new CommonEntityVO();
			if(ValidateUtil.isEmptyData(commonEntityVO)) {
				continue;
			}
			tempCommonEntity.putAll(commonEntityVO);
			if(commonEntityVO.getId() != null) {
				entityIds.put(commonEntityVO.getId(), null);
			}
			
			int j = 0;
			for(int i = 0 ; i < attrIds.length ;) {
    			entity = tempCommonEntity.get(attrIds[i]);
	    		if(entity == null) {
	    			entity = tempCommonEntity.get("value");
	    			tempCommonEntity.remove("value");
	    		} else {
	    			i++;
	    		}
	    		
	    		convertTimeformat(tempCommonEntity, commonEntityVO);
	    		
	    		if(i < attrIds.length && entity != null) {
	    			tempCommonEntity.putAll((Map<? extends String, ? extends Object>) entity);
	    		}
	    		
	    		if(j++ > 50) break;
	    	}
			
			if(entity != null && entity instanceof Map) {
				tempCommonEntity.putAll((Map<? extends String, ? extends Object>) entity);
				
				if(tempCommonEntity.get("value") != null) {
					entity = tempCommonEntity.get("value");
				}
				
				convertTimeformat(tempCommonEntity, commonEntityVO);
			}
			
			if(entity != null) {
				commonEntityVO.put("chartValue", entity);
				commonEntityVO.remove(attrIds[0]);
			}
			
			if(attrIds2 != null) {
				j = 0;
				for(int i = 0 ; i < attrIds2.length ;) {
	    			entity = tempCommonEntity.get(attrIds2[i]);
		    		if(entity == null) {
		    			entity = tempCommonEntity.get("value");
		    			tempCommonEntity.remove("value");
		    		} else {
		    			i++;
		    		}
		    		
		    		convertTimeformat(tempCommonEntity, commonEntityVO);
		    		
		    		if(i < attrIds2.length && entity != null) {
		    			tempCommonEntity.putAll((Map<? extends String, ? extends Object>) entity);
		    		}
		    		
		    		if(j++ > 50) break;
		    	}
				
				if(entity != null && entity instanceof Map) {
					tempCommonEntity.putAll((Map<? extends String, ? extends Object>) entity);
					
					if(tempCommonEntity.get("value") != null) {
						entity = tempCommonEntity.get("value");
					}
					
					convertTimeformat(tempCommonEntity, commonEntityVO);
				}
				
				if(entity != null) {
					commonEntityVO.put("chartValue2", entity);
					commonEntityVO.remove(attrIds2[0]);
				}
			}
		}
		
		for(String key : entityIds.keySet()) {
			result.add(key);
		}
		
		return result;
	}
	
	/**
	 * List the attribute hierarchy.
	 * @param attributeId	Multiple attribute ID
	 * @return				List of attribute ID
	 */
	private String[] splitAttributeId(String attributeId) {
		String tempAttributeId;
		
		if(attributeId == null) {
			return null;
		}
		
		tempAttributeId = attributeId.replaceAll("\\.", ",");
		attributeId = tempAttributeId;
		tempAttributeId = attributeId.replaceAll("\\[", ",");
		attributeId = tempAttributeId;
		tempAttributeId = attributeId.replaceAll("\\]", "");
		attributeId = tempAttributeId;
		
		return attributeId.split(",");
	}
	
	/**
	 * Extract the legends
	 * @param entityIds				Entity ID List
	 * @param legendCommonEntity	LegendCommonEntity
	 * @param legend				Legend
	 * @return						Legend values
	 */
	private List<String> extractLegends(List<String> entityIds, List<CommonEntityVO> legendCommonEntity, String legend) {
		Map<String, String> legends = new HashMap<String, String>();
		List<String> legendValues = new ArrayList<String>();
		
		if(legendCommonEntity != null) {
	    	String[] attrs = splitAttributeId(legend);
	    	for(CommonEntityVO commonEntityVO : legendCommonEntity) {
	    		
	    		Object object = (HashMap<String, Object>) commonEntityVO.get(attrs[0]);
	    		HashMap<String, Object> map = null;
	    		
	    		if(attrs.length > 1) {
	    			for(int i = 1; i < attrs.length; i++) {
	    				if(object instanceof HashMap) {
	    					map = (HashMap<String, Object>) object;
	    					map = (HashMap<String, Object>) map.get("value");
	    				}
	    				object = map.get(attrs[i]);
	    			}
	    		}
				if (object instanceof String) {
					legends.put(commonEntityVO.getId(), (String) object);
				} else if (object instanceof Map) {
					if (((Map) object).get("value") != null) {
						legends.put(commonEntityVO.getId(), ((Map) object).get("value").toString());
					}
				}
	    	}
	    }
	    
	    for(String entityId : entityIds) {
	    	legendValues.add(legends.get(entityId));
	    }
	    
		return legendValues;
	}
	
	/**
	 * Convert histogram data
	 * @param commonEntities	Retrieved entity result data
	 * @param xAxisUnit			x-axis unit
	 * @return					histogram chart data
	 */
	private List<CommonEntityVO> convertHistogramData(List<CommonEntityVO> commonEntities, int xAxisUnit) {
		List<CommonEntityVO> result = new ArrayList<CommonEntityVO>();
		Map<Object, Integer> chartValueMap = new TreeMap<Object, Integer>();
		Map<Object, Integer> resultMap = new TreeMap<Object, Integer>();
		
		double minValue = 9999999.0;
		double maxValue = -9999999.0;
		boolean isNumberXAxisValue = false;
		
		// Counting the number of identical data
		for(CommonEntityVO commonEntityVO : commonEntities) {
			Object chartValue = commonEntityVO.get("chartValue");
			
			if(chartValue == null) continue;
			
			if(chartValue instanceof Double 
					|| chartValue instanceof Integer) {
				String strChartValue = String.valueOf(chartValue); 
				if(Double.valueOf(strChartValue) < minValue) {
					minValue = Double.valueOf(strChartValue);
				}
				if(Double.valueOf(strChartValue) > maxValue) {
					maxValue = Double.valueOf(strChartValue);
				}
				isNumberXAxisValue = true;
			}
			
			if(chartValueMap.get(chartValue) != null) {
				chartValueMap.replace(chartValue, chartValueMap.get(chartValue) + 1);
			} else {
				chartValueMap.put(chartValue, 1);
			}
		}
		
		// If the x-axis values ​​are numeric, repositions the values ​​in units of the x-axis.
		if(isNumberXAxisValue) {
			double factor = 0.0;
			double lowestValue = 0;
			
			if(xAxisUnit > 0) { 
				factor = minValue/xAxisUnit;
			}
			
			if(factor < 0) {
				lowestValue = xAxisUnit * Integer.valueOf(String.format("%.0f", factor));
			} 
			else if(factor > 0) {
				lowestValue = xAxisUnit * (int)factor;
			}
			
			int greatestValue = ((int)(maxValue / xAxisUnit) + 1) * xAxisUnit;
			
			if(lowestValue > 0) {
				lowestValue = 0;
			}
			
			for(int i = xAxisUnit; i <= greatestValue; i += xAxisUnit) {
				resultMap.put((lowestValue + i) - (xAxisUnit / 2.0), 0);
			}
					
			for(Object key : chartValueMap.keySet()) {
				if(key instanceof String) {
					break;
				}
				
				String strKey = String.valueOf(key);
				double xValue;
				
				if(Double.valueOf(strKey) >= 0) {
					xValue = (((int)(Double.valueOf(strKey) / xAxisUnit) + 1) * xAxisUnit) - (xAxisUnit / 2.0);
				} else {
					xValue = ((int)(Double.valueOf(strKey) / xAxisUnit) * xAxisUnit) - (xAxisUnit / 2.0);
				}
				resultMap.replace(xValue, resultMap.get(xValue) + 1);
			}
			chartValueMap = resultMap;
		}
		
		for(Object key : chartValueMap.keySet()) {
			CommonEntityVO commonEntityVO = new CommonEntityVO();
			
			commonEntityVO.put("x", key);
			commonEntityVO.put("y", chartValueMap.get(key));
			
			result.add(commonEntityVO);
		}
		
		return result;
	}
	
	/**
	 * make scatter data
	 * @param commonEntities	Retrieved entity result data
	 * @return					Scatter chart data
	 */
	private List<CommonEntityVO> convertScatterData(List<CommonEntityVO> commonEntities) {
		List<CommonEntityVO> result = new ArrayList<CommonEntityVO>();
		
		for(CommonEntityVO commonEntityVO : commonEntities) {
			Object chartValue = commonEntityVO.get("chartValue");
			Object chartValue2 = commonEntityVO.get("chartValue2");
			
			if(chartValue == null || chartValue2 == null) continue;
			
			String strChartValue = String.valueOf(chartValue);
			String strChartValue2 = String.valueOf(chartValue2);
			CommonEntityVO commonEntity = new CommonEntityVO();
			
			commonEntity.put("x", Double.valueOf(strChartValue));
			commonEntity.put("y", Double.valueOf(strChartValue2));
			
			result.add(commonEntity);
		}
		
		return result;
	}
	
	/**
	 * Convert time format
	 * @param tempCommonEntity		Retrieved entity result temporary data
	 * @param commonEntityVO		Retrieved entity result data
	 */
	private void convertTimeformat(CommonEntityVO tempCommonEntity, CommonEntityVO commonEntityVO) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.CONTENT_DATE_FORMAT);
		SimpleDateFormat dateFormat = new SimpleDateFormat(properties.getChartTimeFormat());
		
		if(tempCommonEntity.get("observedAt") != null) {
			LocalDateTime date = LocalDateTime.parse((String) tempCommonEntity.get("observedAt"), formatter);
			long milliSeconds = Timestamp.valueOf(date).getTime();
			commonEntityVO.put("observedAt", dateFormat.format(milliSeconds));
		}
	}
}
