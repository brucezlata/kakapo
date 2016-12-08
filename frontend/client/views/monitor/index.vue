<template>
  <div>
    <div class="tile is-ancestor">
      <div class="tile is-4 is-vertical is-parent">
        <div class="tile is-child box">
          <p class="title">temperature</p>
          <chart :options="temperature_opt"></chart>
        </div>
        <div class="tile is-child box">
          <p class="title">humidity</p>
          <chart :options="humidity_opt"></chart>
        </div>
      </div>
      <div class="tile is-parent">
        <div class="tile is-child box">
          <p class="title"> {{ Map }}</p>
          <b-map-component></b-map-component>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import BMapComponent from '../components/BMapComponent.vue'

import ECharts from 'vue2-echarts/src/ECharts/ECharts.vue'

var  humidity_data = {
    tooltip : {
        trigger: 'axis'
    },
    // legend: {
    //     data:['humidity']
    // },
    toolbox: {
        show : true,
        feature : {
            mark : {show: true},
            dataView : {show: true, readOnly: false},
            magicType : {show: true, type: ['line', 'bar']},
            restore : {show: true},
            saveAsImage : {show: true}
        }
    },
    calculable : true,
    xAxis : [
        {
            type : 'category',
            boundaryGap : false,
            data : []
        }
    ],
    yAxis : [
      {
        type : 'value',
        //scale:true,
        splitNumber:5,
        boundaryGap: [0, 0.5],
        splitArea : {show : true},
        axisLabel : {
            formatter: '{value}% rh'
        }
      }
    ],
    series : [
        {
            name:'humidity',
            type:'line',
            itemStyle: {normal: {areaStyle: {type: 'default'}}},
            data:[]
        }
    ]
};

var temperature_data =  {
  // title : {
  //     text: 'temperature',
  //     subtext: '纯属虚构'
  // },
  tooltip : {
      trigger: 'axis'
  },
  // legend: {
  //     data:['temperature']
  // },
  toolbox: {
      show : true,
      feature : {
          mark : {show: true},
          dataView : {show: true, readOnly: false},
          magicType : {show: true, type: ['line', 'bar']},
          restore : {show: true},
          saveAsImage : {show: true}
      }
  },
  calculable : true,
  xAxis : [
      {
          type : 'category',
          boundaryGap : false,
          data : []
      }
  ],
  yAxis : [
      {
          type : 'value',
          axisLabel : {
              formatter: '{value} °C'
          }
      }
  ],
  series : [
      {
          name:'temperature',
          type:'line',
          data:[],
          itemStyle: {normal: {areaStyle: {type: 'default'}}},
          markPoint : {
              data : [
                  {type : 'max', name: 'Max'},
                  {type : 'min', name: 'Min'}
              ]
          },
          markLine : {
              data : [
                  {type : 'average', name: 'average'}
              ]
          }
      }
  ]
};


// register the component to use
export default {
  components: { BMapComponent, chart: ECharts },
  // components: { chart: ECharts },
  data: function () {

    return {
      Map: 'Remote Tracking',
      temperature_opt: temperature_data,
      humidity_opt: humidity_data
    }
  },
  mounted () {

    var io = require('socket.io-client');
    var socket = io.connect('http://localhost:3000', {reconnect: true});
    socket.on('connect', function (data) {
        console.log('Charts Connected!');
        socket.emit('initcharts30','init');
    });

    socket.on('temperature_data', function (message) {
      var datalist = message.split(";");
      if(datalist.length=2){
        console.log("temperature data:" + datalist[0] + "|" + datalist[1]);
        if(temperature_data.series[0].data.length>30)
        {
          temperature_data.series[0].data.shift();
          temperature_data.xAxis[0].data.shift();
        }
        temperature_data.series[0].data.push(datalist[0]);
        temperature_data.xAxis[0].data.push(datalist[1]);
      }
    });


    socket.on('humidity_data', function (message) {
        var datalist = message.split(";");
        if(datalist.length=2){
          console.log("humidity data:" + datalist[0] + "|" + datalist[1]);
          if(humidity_data.series[0].data.length>30)
          {
            humidity_data.series[0].data.shift();
            humidity_data.xAxis[0].data.shift();
          }
          humidity_data.series[0].data.push(datalist[0].slice(0, -1)); //remove %
          humidity_data.xAxis[0].data.push(datalist[1]);
        }
    });
  }
}
</script>

<style>
.echarts {
  height: 50px;
}
</style>
