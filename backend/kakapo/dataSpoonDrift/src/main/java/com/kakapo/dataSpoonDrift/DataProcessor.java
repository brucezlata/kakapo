package com.kakapo.dataSpoonDrift;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.kafka.BrokerHosts;
import org.apache.storm.kafka.SpoutConfig;
import org.apache.storm.kafka.StringScheme;
import org.apache.storm.kafka.ZkHosts;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.topology.TopologyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kakapo.dataSpoonDrift.storm.*;


/**
 * Hello world!
 *
 */
public class DataProcessor 
{
	private static final String TOPOLOGY_NAME = "data-spoondrift";
	
	private static final Logger LOG = LoggerFactory.getLogger(DataProcessor.class);
	
    public static void main( String[] args ) throws InterruptedException, AlreadyAliveException, InvalidTopologyException, AuthorizationException
    {
    	
    	LOG.debug("Start DataProcessor Topology");
    	//System.out.println( "Hello World!" );
    	//config kafka spout 
//    	String zkConnString= "199.63.154.198:2181"; //this is zkeeper connection string
    	String zkConnString= "localhost:2181";
    	String topicName = "topic_cctt_kafka"; //this is kafka topic
    	
    	BrokerHosts hosts = new ZkHosts(zkConnString);
    	SpoutConfig spoutConfig = new SpoutConfig(hosts, topicName, "" , "spout_bruce");
    	spoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
    	spoutConfig.startOffsetTime = kafka.api.OffsetRequest.LatestTime();
    	
    	TopologyBuilder builder = new TopologyBuilder();
    	DataKafkaSpout dataKafkaOut = new DataKafkaSpout(spoutConfig);
    	builder.setSpout("data-kafka-spout", dataKafkaOut);
    	
    	builder.setBolt("data-normalizer-blot", new DataNormalizerBlot())
    	.shuffleGrouping("data-kafka-spout");
    	
    	builder.setBolt("data-storage-blot", new DataStorageBlot())
    	.shuffleGrouping("data-normalizer-blot");
    	
    	Config conf = new Config();
    	//conf.setNumWorkers(2);
    	if(args.length == 0){
    		LocalCluster cluster = new LocalCluster();
    		cluster.submitTopology(TOPOLOGY_NAME, conf,builder.createTopology());
    		Thread.sleep(3000*1000);
    		cluster.killTopology(TOPOLOGY_NAME);
    		cluster.shutdown();
    	}else{
    		StormSubmitter.submitTopology(args[0], conf, builder.createTopology());
    	}
    	
    	
    }
}
