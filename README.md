Please visit https://brucezlata.github.io/

Title:  sensor数据的实时采集，传输和显示  

Author: Bruce Sun （zlata.bruce@qq.com）  

Date:   Dec, 25, 2016

![webportal](/kakapo_overview.png)

![webportal](/kakapo_monitor.png)

原计划基于一些传感器数据，针对冷链行业，做一个可以追溯冷链运输车辆的解决方案，包括硬件和软件，及云服务系统。最后完成了一个原型系统，可以实时显示温度和湿度的数据，在百度地图上显示运输车辆的GPS轨迹。当然后续如果在货柜上加个开关门的传感器，然后接上一个摄像头，传输一些图像数据到PDA上，也是没有问题的。  

这篇文章只是为了记录这个原型系统用到的技术，请大牛们看看我们的设计，是不是符合实时，高并发的要求，还请大家吐槽，指正。

# 0. 环境搭建

这个原型用到的技术比较多，罗列一下有mosquitto，kafka，storm，redis等，还有nodejs，socketIO，前端开发webpack，vuejs等。

环境搭建遇到最困难的storm，当然还算比较顺利。遇到一些问题都是前人踩过的坑，一步一步都能找到解决的方法。另外我会在写一些文章总结总结。

# 1. 架构

传感器数据采集依赖硬件平台的MQTT Publish，数据通过3G传输到mosquitto，mqtt broker。云端的kafka和storm配合做数据的传输和清洗。最后Redis存储一个月的数据，另外mongodb存储所有数据，当然关系数据库mysql可以存储结构化数据，例如车辆信息，配送任务等。

![架构图](/arch.png)

这个设计是否合理，能否扛住几千台设备发传感器数据，原型也没有测试。不过mqtt servre和云端的kafka，storm等均可以负载均衡，增加机器来获得性能提升。具体还是要深入下去考虑。有大牛不吝赐教。

软件系统搭建在Ubuntu 16.04 Desktop上面，后来移到微软Azure Ubuntu Server 16.04

# 2. 硬件系统

MTK的无线路由解决平台，MT7620A，MTK single SOC solution for smart phone, AR9331，3G/GPS: Quectel UC20，温度和湿度传感器是德州仪器 TI Sensor Tag。Sensor Tag是依靠BLE和MTK平台数据交互的。MTK平台安装了openwrt，完全是一个路由器。

# 3. 云端软件服务

## MQTT(mosquitto)  

原来的计划是使用HTTP RESTFul。云端完成RESTFul的API，提供给硬件来调用。在仔细研究Restful和mqtt后，觉得mqtt更适合。HTTP协议是短连接，消耗更多的资源，对于移动设备来说，电能始终是个缺陷，相反mqtt的TCP长连接能够节省一点电能。当然云端部署一个mosquitto，消息的传输大大方便。更多看 [这篇文章](http://stephendnicholas.com/posts/power-profiling-mqtt-vs-https)。
  - MQTT Publish  

  用Eclipse paho的C语言客户端，采集sensor的数据，然后send出来。自行脑补。

  - MQTT Subsrible   

  用paho的Java客户端，不打算贴长段的代码了。后面可以下载整个工程。

  ``` java  
  private static final String TOPIC = "topic_cctt_mqtt";

  MqttClient subClient;

  String broker       = "tcp://localhost:1883";

  String clientId     = "cdata_sub";

  MemoryPersistence persistence = new MemoryPersistence();

  myKafkaDataPumbOut = new KafkaDataPumbOut();    	

  subClient = new MqttClient(broker,clientId,persistence);

  subClient.connect();

  subClient.subscribe(TOPIC);

  subClient.setCallback(this);
  ```

  原型刚开始还使用了Javascript的mqtt sub，获得硬件上传来的数据，然后调用百度地图的绘制功能，做了一个汽车轨迹的应用。具体的代码在“frontend/client/views/components/BMapComponent.vue”里面，startMQTTClient方法。  

  在实际使用中，web应用是直接使用mqtt sub获取数据，还是使用socketio从redis里面或者ajax http请求到其他db里面拿，优缺点是什么，此处也没有深究。


## kafka  

mqtt sub接受到数据后，直接把数据发送到kafka的queue中，交给kafka处理。

``` java
public void messageArrived(String topic, MqttMessage message) throws Exception {

    myKafkaDataPumbOut.publish("topic_cctt_kafka", message.toString());

    System.out.println("Publish to Kafka:" + message.toString());

}
```

## storm  

storm直接使用KafkaSpout，从kafka的queue拿到对应的topic数据。这里做了两个blot，一个做数据的标准化，从字符串中分割出各个传感器的数据，第二是做存储，直接想前面的数据，JSON后存入Redis中。



  ``` java
  String zkConnString= "localhost:2181";

  String topicName = "topic_cctt_kafka"; //this is kafka topic

  BrokerHosts hosts = new ZkHosts(zkConnString);

  SpoutConfig spoutConfig = new SpoutConfig(hosts,
  topicName, "" , "spout_bruce");

  spoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());

  spoutConfig.startOffsetTime = kafka.api.OffsetRequest.LatestTime();

  TopologyBuilder builder = new TopologyBuilder();

  DataKafkaSpout dataKafkaOut = new DataKafkaSpout(spoutConfig);

  builder.setSpout("data-kafka-spout", dataKafkaOut);

  builder.setBolt("data-normalizer-blot", new DataNormalizerBlot())
  .shuffleGrouping("data-kafka-spout");

  builder.setBolt("data-storage-blot", new DataStorageBlot())
  .shuffleGrouping("data-normalizer-blot");

  ```

## Redis

如何存储数据也是思考了很久，对于关系数据库而言，很难达到目的，在纠结很久后，觉得redis的Sorted sets，基于评分(score)的有序列表比较适合存储这类数据。  

这类数据的特点是和基于时间轴的数据集，查询的时候也是根据时间的范围来查询。我将timestamp转化成long类型，作为有序列表的score，每条数据java object to Json后存储。

  ``` java
  DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",

   Locale.ENGLISH);

  long score = format.parse(strTime).getTime();

  jedis.zadd(redis_date_key, score,strDataObject);

  ```

## Web protal

  - vuejs & vue admin  

  之前对jquery和bootstrap还算熟悉，前端这几年的巨大变化，有太多有意思的东西出现，vuejs就是其中之一。这次做原型，看看这个js的前端框架，优势不必多说。对于我个人而言，玩的还不是很溜。其中觉得，优秀的和它配合的css/UI框架很缺少。远远没有之前随便花个几美金买一个优秀的UI来的爽。vue admin是vuejs的一个免费的UI主题，我拿来读代码学习vuejs用的。  

  学习vuejs一个痛苦的地方，vue admin用到的一些语法，vuejs官方文档很难找到，也是困惑的，估计是我的前端基础比较差，看的比较少。

  - baidu echarts和map  

不容质疑百度这两个应用，非常的厉害，echarts非常强大。有人已经写了echarts和map的veujs接口。在npm上面搜索即可。

不过百度地图目前还没有，我搜索了一片如何在vuejs中使用高德地图的文章，将百度地图加进去了。

  - gps运行轨迹的实现

  首先轨迹实际上是在地图上根据轨迹点，绘制折线。类似与滴滴打车的车头，会在地图上转换方向，其实实现的原理就是根据目前的点和下一个要绘制的点的计算出方向向量，然后旋转地图over layer，也就是marker。



    ``` Javascript

    function setRotation (marker,curPos,targetPos){

        var me = this;

        var deg = 0;

        //start!

        curPos =  baiduMap.pointToPixel(curPos);

        targetPos =  baiduMap.pointToPixel(targetPos);

        if(targetPos.x != curPos.x){

                var tan = (targetPos.y - curPos.y)/(targetPos.x - curPos.x),

                atan  = Math.atan(tan);

                deg = atan*360/(2*Math.PI);

                //degree  correction;

                if(targetPos.x < curPos.x){

                    deg = -deg + 90 + 90;

                } else {

                    deg = -deg;

                }

                marker.setRotation(-deg);



        }else {

                var disy = targetPos.y- curPos.y ;

                var bias = 0;

                if(disy > 0)

                    bias=-1

                else

                    bias = 1

                marker.setRotation(-bias * 90);

        }

        return;

    }

    ```





# 4. 后续的工作

* 硬件集成

* 数据安全和身份验证  

原型没有考虑数据安全，没有任何身份验证。后续要考虑mqtt broker的身份验证，kafka和storm的auth方法。



# 5. 开源链接

https://coding.net/u/zlata/p/kakapo/git

https://github.com/brucezlata/kakapo.git
