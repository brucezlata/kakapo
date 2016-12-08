package com.kakapo.dataPump;

import com.kakapo.dataPump.mqttClient.*;

public class DataWaveProducer {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		new MqttDataSubscribe().listen();
	}

}
