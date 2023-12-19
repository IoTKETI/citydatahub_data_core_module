package kr.re.keti.sc.datacoreusertool.api.widgetdashboard.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import kr.re.keti.sc.datacoreusertool.api.widgetdashboard.service.WidgetDashboardSVC;
import kr.re.keti.sc.datacoreusertool.api.widgetdashboard.vo.WidgetDashboardBaseResponseVO;
import kr.re.keti.sc.datacoreusertool.api.widgetdashboard.vo.WidgetDashboardBaseVO;
import kr.re.keti.sc.datacoreusertool.api.widgetdashboard.vo.WidgetDashboardFileUIVO;
import kr.re.keti.sc.datacoreusertool.api.widgetdashboard.vo.WidgetDashboardUIResponseVO;
import kr.re.keti.sc.datacoreusertool.api.widgetdashboard.vo.WidgetDashboardUIVO;
import kr.re.keti.sc.datacoreusertool.common.code.Constants;
import kr.re.keti.sc.datacoreusertool.common.vo.ClientExceptionPayloadVO;
import kr.re.keti.sc.datacoreusertool.security.service.UserToolSecuritySVC;
import kr.re.keti.sc.datacoreusertool.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Class for management of widget dash board.
 *
 * @FileName WidgetDashboardController.java
 * @Project datacore-usertool
 * @Brief
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Slf4j
@Controller
public class WidgetDashboardController {

    @Autowired
    private WidgetDashboardSVC widgetDashboardSVC;

    @Autowired
    private UserToolSecuritySVC userToolSecuritySVC;


    /**
     * Create widget
     *
     * @param widgetDashboardUIVO WidgetDashboardUIVO
     * @throws Exception Throw an exception when an error occurs.
     * @return Result of create widget.
     */
    @PostMapping(value = "/widget")
    public ResponseEntity<WidgetDashboardUIResponseVO> createWidget(HttpServletRequest request, HttpServletResponse response,
                                                                    @RequestBody WidgetDashboardUIVO widgetDashboardUIVO) throws Exception {

        // 1. Validate required values
        if (ValidateUtil.isEmptyData(widgetDashboardUIVO.getDashboardId())
                || ValidateUtil.isEmptyData(widgetDashboardUIVO.getChartType())
                || ValidateUtil.isEmptyData(widgetDashboardUIVO.getChartAttribute())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // 2. create widget
        ResponseEntity<WidgetDashboardUIResponseVO> result = widgetDashboardSVC.createWidget(request, widgetDashboardUIVO);

        return result;
    }

    /**
     * Update widget
     *
     * @param widgetDashboardUIVO
     * @throws Exception Throw an exception when an error occurs.
     * @return Result of update widget.
     */
    @PutMapping(value = "widget")
    public ResponseEntity<WidgetDashboardUIResponseVO> updateWidget(HttpServletRequest request, HttpServletResponse response,
                                                                    @RequestBody WidgetDashboardUIVO widgetDashboardUIVO) throws Exception {

        // 1. Validate required values
        if (!validateParameter(widgetDashboardUIVO)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // 2. update widget
        ResponseEntity<WidgetDashboardUIResponseVO> result = widgetDashboardSVC.updateWidget(request, widgetDashboardUIVO);

        return result;
    }

    /**
     * Update widget layout
     *
     * @param widgetDashboardUIVOs List of WidgetDashboardUIVO
     * @throws Exception Throw an exception when an error occurs.
     * @return Result of update widget layout.
     */
    @PutMapping(value = "widget/layout")
    public ResponseEntity<WidgetDashboardUIResponseVO> updateWidgetLayout(HttpServletRequest request, HttpServletResponse response,
                                                                          @RequestBody List<WidgetDashboardUIVO> widgetDashboardUIVOs) throws Exception {

        // 1. update widget layout
        ResponseEntity<WidgetDashboardUIResponseVO> result = widgetDashboardSVC.updateWidgetLayout(request, widgetDashboardUIVOs);

        return result;
    }

    /**
     * Delete widget
     *
     * @param dashboardId Dashboard ID
     * @param widgetId    Widget ID
     * @throws Exception Throw an exception when an error occurs.
     * @return Result of delete widget.
     */
    @DeleteMapping(value = "widget")
    public ResponseEntity<ClientExceptionPayloadVO> deleteWidget(HttpServletRequest request, HttpServletResponse response,
                                                                 @RequestParam String dashboardId, @RequestParam String widgetId) throws Exception {

        // 1. delete widget
        ResponseEntity<ClientExceptionPayloadVO> result = widgetDashboardSVC.deleteWidget(request, dashboardId, widgetId);

        return result;
    }

    /**
     * Retrieve widget
     *
     * @param dashboardId Dashboard ID
     * @param widgetId    Widget ID
     * @throws Exception Throw an exception when an error occurs.
     * @return Widget information retrieved by dashboard ID and widget ID.
     */
    @GetMapping(value = "/widget")
    public ResponseEntity<WidgetDashboardUIResponseVO> getWidget(HttpServletRequest request, HttpServletResponse response,
                                                                 @RequestParam String dashboardId, @RequestParam String widgetId) throws Exception {

        // 1. retrieve userId
        String userId = userToolSecuritySVC.getUserId(request).getBody();

        // 2. retrieve widget
        ResponseEntity<WidgetDashboardUIResponseVO> result = widgetDashboardSVC.getWidget(userId, dashboardId, widgetId);

        return result;
    }

    /**
     * Retrieve all widgets included in the dashboard.
     *
     * @param dashboardId Dashboard ID
     * @throws Exception Throw an exception when an error occurs.
     * @return List of widget retrieved by dashboard ID.
     */
    @GetMapping(value = "/widgets")
    public ResponseEntity<List<WidgetDashboardUIResponseVO>> getAllWidget(HttpServletRequest request, HttpServletResponse response,
                                                                          @RequestParam String dashboardId) throws Exception {

        // 1. retrieve userId
        String userId = userToolSecuritySVC.getUserId(request).getBody();

        // 1. retrieve all widget
        ResponseEntity<List<WidgetDashboardUIResponseVO>> result = widgetDashboardSVC.getAllWidget(userId, dashboardId);

        return result;
    }

    /**
     * Create file widget
     *
     * @param widgetDashboardFileUIVO WidgetDashboardFileUIVO
     * @throws Exception Throw an exception when an error occurs.
     * @return Result of file widget creation.
     */
    @PostMapping(value = "widget/file")
    public ResponseEntity<WidgetDashboardUIResponseVO> createWidgetFile(HttpServletRequest request, HttpServletResponse response,
                                                                        WidgetDashboardFileUIVO widgetDashboardFileUIVO) throws Exception {

        ResponseEntity<WidgetDashboardUIResponseVO> result = widgetDashboardSVC.createWidgetFile(request, widgetDashboardFileUIVO);

        return result;
    }

    /**
     * Get image of widget
     *
     * @param fileId File ID
     * @throws Exception Throw an exception when an error occurs.
     */
    @GetMapping(value = "widget/image")
    public void getWidgetFile(HttpServletRequest request, HttpServletResponse response,
                              @RequestParam String dashboardId, @RequestParam String widgetId) throws Exception {

        // 1. retrieve userId
        String userId = userToolSecuritySVC.getUserId(request).getBody();

        // 2. retrieve widget
        ResponseEntity<WidgetDashboardUIResponseVO> result = widgetDashboardSVC.getWidget(userId, dashboardId, widgetId);

        if (result != null && result.getStatusCode().equals(HttpStatus.OK)) {
            WidgetDashboardUIResponseVO widgetDashboardUIResponseVO = result.getBody();
            response.setContentType(Constants.CONTENT_TYPE_IMAGE_JPEG);
            response.getOutputStream().write(widgetDashboardUIResponseVO.getFile());
        } else {
            response.setStatus(result.getStatusCodeValue());
        }
    }

    /**
     * Create Dashboard
     *
     * @param widgetDashboardBaseVO WidgetDashboardBaseVO
     * @throws Exception Throw an exception when an error occurs.
     * @return Result of dashboard creation.
     */
    @PostMapping(value = "dashboard")
    public ResponseEntity<WidgetDashboardBaseVO> createDashboard(HttpServletRequest request, HttpServletResponse response,
                                                                 @RequestBody WidgetDashboardBaseVO widgetDashboardBaseVO) throws Exception {

        // 1. Validate required values
        if (ValidateUtil.isEmptyData(widgetDashboardBaseVO.getDashboardName())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // 2. create dashboard
        ResponseEntity<WidgetDashboardBaseVO> result = widgetDashboardSVC.createDashboard(request, widgetDashboardBaseVO);

        return result;
    }

    /**
     * Update Dashboard
     *
     * @param widgetDashboardBaseVO WidgetDashboardBaseVO
     * @throws Exception Throw an exception when an error occurs.
     * @return Result of update dashboard.
     */
    @PutMapping(value = "dashboard")
    public ResponseEntity<WidgetDashboardBaseVO> updateDashboard(HttpServletRequest request, HttpServletResponse response,
                                                                 @RequestBody WidgetDashboardBaseVO widgetDashboardBaseVO) throws Exception {

        // 1. Validate required values
        if (ValidateUtil.isEmptyData(widgetDashboardBaseVO.getDashboardId())
                || ValidateUtil.isEmptyData(widgetDashboardBaseVO.getDashboardName())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // 2. update dashboard
        ResponseEntity<WidgetDashboardBaseVO> result = widgetDashboardSVC.updateDashboard(request, widgetDashboardBaseVO);

        return result;
    }

    /**
     * Delete Dashboard
     *
     * @param dashboardId Dashboard ID
     * @throws Exception Throw an exception when an error occurs.
     * @return Result of delete dashboard.
     */
    @DeleteMapping(value = "dashboard/{dashboardId}")
    public ResponseEntity<ClientExceptionPayloadVO> deleteDashboard(HttpServletRequest request, HttpServletResponse response,
                                                                    @PathVariable("dashboardId") String dashboardId) throws Exception {

        // 1. delete widget
        ResponseEntity<ClientExceptionPayloadVO> result = widgetDashboardSVC.deleteDashboard(request, dashboardId);

        return result;
    }

    /**
     * Retrieve Dashboards
     *
     * @throws Exception Throw an exception when an error occurs.
     * @return List of all registered dashboards.
     */
    @GetMapping(value = "dashboard")
    public ResponseEntity<List<WidgetDashboardBaseResponseVO>> getDashboards(HttpServletRequest request, HttpServletResponse response) throws Exception {

        // 1. retrieve all widget
        ResponseEntity<List<WidgetDashboardBaseResponseVO>> result = widgetDashboardSVC.getDashboards(request);

        return result;
    }

    // Validate the chart required values
    private boolean validateParameter(WidgetDashboardUIVO widgetDashboardUIVO) {
        if (ValidateUtil.isEmptyData(widgetDashboardUIVO.getWidgetId())
                || ValidateUtil.isEmptyData(widgetDashboardUIVO.getChartType())
                || ValidateUtil.isEmptyData(widgetDashboardUIVO.getChartAttribute())) {
            return false;
        }
        return true;
    }
}