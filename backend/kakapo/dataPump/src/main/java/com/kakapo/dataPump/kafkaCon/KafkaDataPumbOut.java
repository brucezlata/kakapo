package com.kakapo.dataPump.kafkaCon;

import java.util.*;
import org.apache.kafka.clients.producer.*;

public class KafkaDataPumbOut {

	public Producer<String, String> kafkaSpoondriftOut = null;
	public KafkaDataPumbOut()
	{
		Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        
        kafkaSpoondriftOut = new KafkaProducer<String, String>(props);
	}
	public void publish(String topic, String content)
	{
		if(kafkaSpoondriftOut == null)
		{
			System.out.println("kafkaSpoondriftOut is NULL!");
			return;	
		}
		kafkaSpoondriftOut.send(new ProducerRecord<String, String>(topic, content));
	}
	
	
}
