package kr.re.keti.sc.datacoreusertool.api.subscription.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import kr.re.keti.sc.datacoreusertool.api.subscription.service.SubscriptionSVC;
import kr.re.keti.sc.datacoreusertool.api.subscription.vo.SubscriptionResponseVO;
import kr.re.keti.sc.datacoreusertool.api.subscription.vo.SubscriptionUIVO;
import kr.re.keti.sc.datacoreusertool.api.subscription.vo.SubscriptionVO;
import lombok.extern.slf4j.Slf4j;

/**
 * Class for management of subscription.
 * @FileName SubscriptionController.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Slf4j
@Controller
public class SubscriptionController {
	
	@Autowired
	private SubscriptionSVC subscriptionSVC;

	/**
	 * Create subscription
	 * @param subscriptionUIVOs 	List of SubscriptionUIVO (subscription condition)
	 * @return						Result of create subscription.
	 * @throws Exception			Throw an exception when an error occurs.
	 */
	@PostMapping(value="/subscriptions")
	@ApiOperation(value = "Create subscription", notes = "Request for subscription creation")
	public ResponseEntity<SubscriptionResponseVO> createSubscription(HttpServletRequest request, HttpServletResponse response, @RequestBody List<SubscriptionUIVO> subscriptionUIVOs) 
			throws Exception {
		
		ResponseEntity<SubscriptionResponseVO> subscriptionResponseVO = subscriptionSVC.createSubscription(subscriptionUIVOs, request);
		
		return subscriptionResponseVO;		
	}
	
	/**
	 * Delete subscription
	 * @param subscriptionId	Subscription id
	 * @return					Result of delete subscription.
	 */
	@DeleteMapping(value="/subscriptions/{subscriptionId}")
	@ApiOperation(value = "Delete subscription", notes = "Request for subscription deletion")
	public ResponseEntity<Void> deleteSubscription(HttpServletRequest request, HttpServletResponse response, @PathVariable("subscriptionId") String subscriptionId) 
			throws Exception {
		
		ResponseEntity<Void> result = subscriptionSVC.deleteSubscription(subscriptionId);
		
		return result;		
	}
	
	/**
	 * Retrieve multiple subscription
	 * @return				All subscription information
	 * @throws Exception	Throw an exception when an error occurs.
	 */
	@GetMapping(value="/subscriptions")
	@ApiOperation(value = "Retrieve multi subscription", notes = "Retrieve multi subscription")
	@ResponseBody
	public ResponseEntity<List<SubscriptionVO>> getSubscriptions(HttpServletRequest request, HttpServletResponse response) 
			throws Exception {
		
		ResponseEntity<List<SubscriptionVO>> subscriptionVOs = subscriptionSVC.getSubscriptions();
		
		return subscriptionVOs;
	}
}
