package com.kakapo.dataSpoonDrift.storm;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kakapo.dataSpoonDrift.dataModel.TrackingData;

import com.google.gson.Gson;

public class DataNormalizerBlot implements IRichBolt{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private OutputCollector collector;
	private static final Logger LOG = LoggerFactory.getLogger(DataNormalizerBlot.class);
    public void cleanup(){
    }
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector=collector;
    }
    public void execute(Tuple input){
    
    	//System.out.println("Data from Kafka:" + input.toString());
        String rawData = input.getStringByField("datawave");
        System.out.println("burce Recived:" + rawData);
        LOG.debug("From Kafka Spout Recived:" + rawData);
        String[] trackDataArray = rawData.split(",");
        
        TrackingData  tDataObj = new TrackingData();
        
        for(String trackData : trackDataArray){
        	trackData = trackData.trim(); 	
            if(!trackData.isEmpty()){
            	//here get key:value
            	String[] pairs = trackData.split(":");
            	if(pairs.length == 2){
            		String key = pairs[0].trim();
            		String value = pairs[1].trim();
            		if(key.equals("sn")){
            			tDataObj.deviceSN = value;
            		}else if(key.equals("dt")){
            			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.ENGLISH);
            			try {
            				Date newdate= format.parse(value);
            				//2011-10-05T14:48:00.000Z
            				format.applyPattern("yyyy-MM-dd'T'HH:mm:ss");
            				tDataObj.timestamp = format.format(newdate);
            				
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
            		}else if(key.equals("la")){
            			tDataObj.latitude = value;
            		}else if(key.equals("lg")){
            			tDataObj.longitude = value;
            		}else if(key.equals("t")){
            			tDataObj.temperature = value;
            		}else if(key.equals("h")){
            			tDataObj.humidity = value;
            		}else{
            			//nothing
            		}
            	}
            }
        }
        
    	//convert data to json
    	Gson gson = new Gson();
    	String json = gson.toJson(tDataObj);  
    	//emit json string and teimstamp
//    	System.out.println("***---***   json:" + json);
//    	System.out.println("***---***   timestamp:" + tDataObj.timestamp);
    	this.collector.emit(new Values(json,tDataObj.timestamp));
    	
        //answer the tuple
        collector.ack(input);
    }
    /**
      * blot publish fields
      */
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("trackjsondata","timestamp"));
    }
	public Map<String, Object> getComponentConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

}
