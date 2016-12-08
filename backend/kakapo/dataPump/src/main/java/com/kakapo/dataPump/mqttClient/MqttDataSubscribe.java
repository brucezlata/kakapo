package com.kakapo.dataPump.mqttClient;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.kakapo.dataPump.kafkaCon.*;

public class MqttDataSubscribe implements MqttCallback {

	private static final String TOPIC = "topic_cctt_mqtt";
	MqttClient subClient;
	String broker       = "tcp://localhost:1883";
    String clientId     = "cdata_sub";
    MemoryPersistence persistence = new MemoryPersistence();
    
    KafkaDataPumbOut myKafkaDataPumbOut = null;
    
    public  void listen() {
	    try {
	    	
	    	myKafkaDataPumbOut = new KafkaDataPumbOut();    	
	    	subClient = new MqttClient(broker,clientId,persistence);
	    	subClient.connect();
	    	subClient.subscribe(TOPIC);
	    	subClient.setCallback(this);
	        
	        
	      } catch (MqttException e) {
	        e.printStackTrace();
	      } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public void close()
	{
		try{
			subClient.disconnect();
		}
		catch (MqttException e) {
	        e.printStackTrace();
	      } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	public void connectionLost(Throwable t) {
	    t.printStackTrace();
	}

	public void deliveryComplete(IMqttDeliveryToken arg0) {

	}

	public void messageArrived(String topic, MqttMessage message) throws Exception {
	
	    if(!message.toString().contains("debugmsg")){
	    	System.out.println("Received Message from broker");
			myKafkaDataPumbOut.publish("topic_cctt_kafka", message.toString());
			System.out.println("Publish to Kafka:" + message.toString());
	    }
	}
}
