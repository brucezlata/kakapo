package com.kakapo.dataSpoonDrift.storm;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class DataStorageBlot implements IRichBolt{
	
	private static final long serialVersionUID = 1L;
	private static final int redisPort = 6379;
	private static final String redisURL = "localhost";
	private static final String redis_date_key = "track_store";
	private static final Logger LOG = LoggerFactory.getLogger(DataStorageBlot.class);
	private OutputCollector collector;
    
	private JedisPool pool = null;
    public void cleanup(){
    	if(pool != null)
    		pool.destroy();	
    }

    /**
     *  为每个单词计数
     */
    
    public void execute(Tuple input) {
    	String strMember = input.getStringByField("trackjsondata");
    	String strTime = input.getStringByField("timestamp");
		Jedis jedis = null;
		try {
		  jedis = pool.getResource();
		  if (jedis != null){
			  //get score using timestamp
//			  System.out.println("***---*** strMember:" + strMember);
//			  System.out.println("***---***   strTime:" + strTime);
			  DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
			  long score = format.parse(strTime).getTime();
			  //add data to redis database
			  
			  jedis.zadd(redis_date_key, score,strMember);
			  System.out.println("add date to redis:" + strMember);
			  jedis.publish("trackchannel", strMember);
			  LOG.debug("Publish data to" + " trackchannel:" + strMember);
		  }
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("***---***" + "ParseException");
		} finally {
		  if (jedis != null) {
		    jedis.close();
		  }
		}
        collector.ack(input);
    }

    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector){
        this.collector = collector;
        pool = new JedisPool(new JedisPoolConfig(), redisURL,redisPort);
        LOG.debug("Create Jedis Connection.");
    }
    
    public void declareOutputFields(OutputFieldsDeclarer declarer) {}

	public Map<String, Object> getComponentConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}
}
