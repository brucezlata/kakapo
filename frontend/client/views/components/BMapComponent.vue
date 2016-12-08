<style scoped>



</style>

<template>
  <div id="allmap" v-bind:style="mapStyle">
  </div>
</template>

<script>
import BMap from 'BMap'
//require('paho-mqtt');
import paho from 'paho-mqtt'

// import io from 'socket.io-client'

var truck_ICON = require("../../assets/truck.png")

export default {
  data: function () {
    return {
      mapStyle: {
        width: '100%',
        height: this.mapHeight + 'px'
      }
    }
  },
  props: {
    mapPolylineData:Object,
    mapHeight: {
      type: Number,
      default: 600
    },
    longitude: {
      type: Number,
      default: 116.404
    },
    latitude: {
      type: Number,
      default: 39.915
    },
    description: {
      type: String,
      default: '天安门'
    }
  },
  methods: {
    startRedisClient: function (bmap){
      var baiduMap = bmap;
      var arrPois =[];
      var prePoint = null;
      var _marker = null;
      var io = require('socket.io-client');
      var socket = io.connect('http://localhost:3000', {reconnect: true});
      socket.on('connect', function (data) {
          console.log('Connected!');
          socket.emit('initgps30','init');
      });
      // listen sensor Data from server side
      socket.on('gps_data', function (message) {
        // console.log(data + "---from redis---");
        var gpslist = message.split(":");
        var lng = parseFloat(gpslist[1].slice(0, -1))/100.0;
        var lat = parseFloat(gpslist[0].slice(0, -1))/100.0;
        console.log("gps_data:" + lng + ":" + lat);
        var newPoint = new BMap.Point(lng, lat);

        // console.log("gps_data:" + gpslist[1] + ":" + gpslist[0]);
        // var newPoint = new BMap.Point(Number(gpslist[1]), Number(gpslist[0]));
        BMap.Convertor.translate(newPoint,2,translateOptions);
      });

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

      function translateOptions(newPoint){
        arrPois.push(newPoint);
        if(arrPois.length == 1){
          prePoint = newPoint;
        }
        var polyline = new BMap.Polyline(arrPois, {strokeColor:"red", strokeWeight:4, strokeOpacity:0.5});   //创建折线
        baiduMap.addOverlay(polyline);   //增加折线
        // console.log(baiduMap.getDistance(prePoint,newPoint));
        if(baiduMap.getDistance(prePoint,newPoint)>200){
          baiduMap.setCenter(newPoint);
          prePoint = newPoint;
        }
        //add marker
        if(_marker){
          baiduMap.removeOverlay(_marker);
        }
        var myIcon = new BMap.Icon(truck_ICON, new BMap.Size(25,25));
        var marker = new BMap.Marker(newPoint,{icon:myIcon});
        if(arrPois.length>3)
        {
          var index = arrPois.length;
          var curPos = arrPois[index-2]
          var targetPos = arrPois[index-1]
          setRotation(marker,curPos,targetPos)
        }
        baiduMap.addOverlay(marker);
        _marker = marker;
      }

    },
    startMQTTClient: function (bmap){
      var client = new Paho.MQTT.Client('199.63.154.198', Number(8081), "clientId");
      var baiduMap = bmap;
      var arrPois =[];
      var prePoint = null;
      var _marker = null;
      // set callback handlers
      client.onConnectionLost = onConnectionLost;
      client.onMessageArrived = onMessageArrived;

      // connect the client
      client.connect({onSuccess:onConnect});


      // called when the client connects
      function onConnect() {
        // Once a connection has been made, make a subscription and send a message.
        console.log("onConnect");
        client.subscribe("testdata");
        // message = new Paho.MQTT.Message("Hello");
        // message.destinationName = "testdata";
        // client.send(message);
      }

      // called when the client loses its connection
      function onConnectionLost(responseObject) {
        if (responseObject.errorCode !== 0) {
          console.log("onConnectionLost:"+responseObject.errorMessage);
        }
      }

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

      function translateOptions(newPoint){
        arrPois.push(newPoint);
        if(arrPois.length == 1){
          prePoint = newPoint;
        }
        var polyline = new BMap.Polyline(arrPois, {strokeColor:"red", strokeWeight:4, strokeOpacity:0.5});   //创建折线
        baiduMap.addOverlay(polyline);   //增加折线
        // console.log(baiduMap.getDistance(prePoint,newPoint));
        if(baiduMap.getDistance(prePoint,newPoint)>200){
          baiduMap.setCenter(newPoint);
          prePoint = newPoint;
        }
        //add marker
        if(_marker){
          baiduMap.removeOverlay(_marker);
        }
        var myIcon = new BMap.Icon(truck_ICON, new BMap.Size(25,25));
        var marker = new BMap.Marker(newPoint,{icon:myIcon});
        if(arrPois.length>3)
        {
          var index = arrPois.length;
          var curPos = arrPois[index-2]
          var targetPos = arrPois[index-1]
          setRotation(marker,curPos,targetPos)
        }
        baiduMap.addOverlay(marker);
        _marker = marker;
      }


      // called when a message arrives
      function onMessageArrived(message) {
        var gpslist = message.payloadString.split(":");
        // console.log("onMessageArrived:" + gpslist[1] + ":" + gpslist[0]);
        var newPoint = new BMap.Point(Number(gpslist[1]), Number(gpslist[0]));
        BMap.Convertor.translate(newPoint,2,translateOptions);
      }
    }
  },
  mounted () {
    var map = new BMap.Map("allmap");
    map.centerAndZoom(new BMap.Point(120.6732273, 31.3218573), 15);
    map.enableScrollWheelZoom();
    //this.startMQTTClient(map);
    this.startRedisClient(map);

  }

}
</script>
