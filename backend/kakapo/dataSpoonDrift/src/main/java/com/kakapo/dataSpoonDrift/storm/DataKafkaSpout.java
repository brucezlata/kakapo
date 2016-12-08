package com.kakapo.dataSpoonDrift.storm;

import org.apache.storm.kafka.KafkaSpout;
import org.apache.storm.kafka.SpoutConfig;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;

public class DataKafkaSpout extends KafkaSpout{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public DataKafkaSpout(SpoutConfig spoutConf) {
		super(spoutConf);
		// TODO Auto-generated constructor stub
	}
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("datawave"));
    }
}
