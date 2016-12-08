var path = require('path')
var express = require('express')
var webpack = require('webpack')
var config = require('../config')
var proxyMiddleware = require('http-proxy-middleware')
var webpackConfig = process.env.NODE_ENV === 'testing'
  ? require('./webpack.prod.conf')
  : require('./webpack.dev.conf')

// default port where dev server listens for incoming traffic
var port = process.env.PORT || config.dev.port
// Define HTTP proxies to your custom API backend
// https://github.com/chimurai/http-proxy-middleware
var proxyTable = config.dev.proxyTable

var app = express()
var compiler = webpack(webpackConfig)

var devMiddleware = require('webpack-dev-middleware')(compiler, {
  publicPath: webpackConfig.output.publicPath,
  stats: {
    colors: true,
    chunks: false
  }
})

var hotMiddleware = require('webpack-hot-middleware')(compiler)
// force page reload when html-webpack-plugin template changes
compiler.plugin('compilation', function (compilation) {
  compilation.plugin('html-webpack-plugin-after-emit', function (data, cb) {
    hotMiddleware.publish({ action: 'reload' })
    cb()
  })
})

// proxy api requests
Object.keys(proxyTable).forEach(function (context) {
  var options = proxyTable[context]
  if (typeof options === 'string') {
    options = { target: options }
  }
  app.use(proxyMiddleware(context, options))
})

// handle fallback for HTML5 history API
app.use(require('connect-history-api-fallback')())

// serve webpack bundle output
app.use(devMiddleware)

// enable hot-reload and state-preserving
// compilation error display
app.use(hotMiddleware)

// serve pure static assets
var staticPath = path.posix.join(config.dev.assetsPublicPath, config.dev.assetsSubDirectory)
app.use(staticPath, express.static('./assets'))

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
  const subscribe = redis.createClient("redis://199.63.154.198:6379");
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
      socket.emit('temperature_data', trackingData.temperature + ';' + trackingData.timestamp);
      socket.emit('humidity_data', trackingData.humidity + ';' + trackingData.timestamp);
    }
  });

  subscribe.subscribe('trackchannel'); //    listen to messages from channel pubsub
  subscribe.on("message", function(channel, message) {
      // console.log(channel + ": " + message + " then send to client");
      //parser data
      var parser = require('json-parser');
      var trackingData = parser.parse(message);
      socket.emit('gps_data', trackingData.latitude+':'+trackingData.longitude);
      socket.emit('temperature_data', trackingData.temperature + ';' + trackingData.timestamp);
      socket.emit('humidity_data', trackingData.humidity + ';' + trackingData.timestamp);
  });
});
