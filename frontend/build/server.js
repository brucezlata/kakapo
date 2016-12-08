var path = require('path')
var express = require('express')

var port = 8080

var app = express()

app.use('/assets', express.static(path.join(__dirname, 'assets')))

app.get('/',function(req,res){
  res.sendFile(path.join(__dirname+'/index.html'));
});

module.exports = app.listen(port, function (err) {
  if (err) {
    console.log(err)
    return
  }
  console.log('Listening at http://localhost:' + port + '\n')
})

var app1 = require('express')();
var server = require('http').Server(app1);
var io = require('socket.io')(server);

server.listen(3000);
// const socket  = io.listen(server);
var redis = require("redis");
io.on('connection', function (socket) {
  const subscribe = redis.createClient("redis://localhost:6379");
  var initDataObj = [];

  subscribe.zrange('track_store',0,-1,function(err,results){

    // console.log("initgps30 - results:" + results.length);
    initDataObj = results;
  });

  // socket.emit('news', { hello: 'world' });
  socket.on('initgps30', function (data) {
    var parser = require('json-parser');
    for(var i=0 ;i <initDataObj.length; i++){
      var trackingData = parser.parse(initDataObj[i]);
      socket.emit('gps_data', trackingData.latitude+':'+trackingData.longitude);
    }
  });
  socket.on('initcharts30', function (data) {
    var parser = require('json-parser');
    for(var i=0 ;i <initDataObj.length; i++){
      var trackingData = parser.parse(initDataObj[i]);
      if (typeof trackingData.temperature != 'undefined' && trackingData.temperature) {
        socket.emit('temperature_data', trackingData.temperature + ';' + trackingData.timestamp);
      }
      
      if (typeof trackingData.humidity != 'undefined' && trackingData.humidity) {
        socket.emit('humidity_data', trackingData.humidity + ';' + trackingData.timestamp);
      }
    }
  });

  subscribe.subscribe('trackchannel'); //    listen to messages from channel pubsub
  subscribe.on("message", function(channel, message) {
      // console.log(channel + ": " + message + " then send to client");
      //parser data
      var parser = require('json-parser');
      var trackingData = parser.parse(message);
      socket.emit('gps_data', trackingData.latitude+':'+trackingData.longitude);
      // socket.emit('temperature_data', trackingData.temperature + ';' + trackingData.timestamp);
      // socket.emit('humidity_data', trackingData.humidity + ';' + trackingData.timestamp);
      if (typeof trackingData.temperature != 'undefined' && trackingData.temperature) {
        socket.emit('temperature_data', trackingData.temperature + ';' + trackingData.timestamp);
      }
      
      if (typeof trackingData.humidity != 'undefined' && trackingData.humidity) {
        socket.emit('humidity_data', trackingData.humidity + ';' + trackingData.timestamp);
      }
  });
});
