package kr.re.keti.sc.pushagent.notification.sender.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MqttEventHandler implements MqttCallback {

	public void connectionLost(Throwable cause) {
		log.warn("MqttEventHandler connectionLost. ", cause);
	}

	public void messageArrived(String topic, MqttMessage message) throws Exception {
		log.info("MqttEventHandler messageArrived. topic={}, message={}", topic, message);
	}

	public void deliveryComplete(IMqttDeliveryToken token) {
		log.info("MqttEventHandler deliveryComplete. token={}", token);
	}
}
