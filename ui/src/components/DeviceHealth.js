
import React from 'react';
import { Route, Link, withRouter } from 'react-router-dom';
import firebase from '../firebase/firebase.js';
import * as helpers from '../helpers/datehelpers.js';
import { Line } from "react-chartjs-2";

class DeviceHealthPage extends React.Component {



  constructor(props) {
    super(props);

    this._mounted = false;
    this.locationHolder = props['locationHolder'];

    this.limitTo = 100;
    this.offset = 0;
    this.knownMax = 0;

    this.state = {
      records: new Map(),
    };

  }


  queryRecentData(maxEventSeq) {

    maxEventSeq = maxEventSeq + 1;
    this.knownMax = maxEventSeq;
    var c = this;
    var maxDisp = maxEventSeq + this.offset;
    var minDisp = maxEventSeq + this.offset + this.limitTo;

    const query2 = firebase.database().ref('locations/' + this.locationHolder.getLocation() + "/humidityTemperature").orderByChild("sequenceId").startAt(maxDisp).endAt(minDisp);
    query2.on("value", function (snapshot) {
      if (c._mounted) {


        var records = new Map();




        snapshot.forEach(function (childSnapshot) {
       
          var devId = childSnapshot.child("deviceId").val();
          var locId = childSnapshot.child("location").val();

          var key =  devId + "," + locId;
            

          var lst = records.get(key);
          if (lst == null) {
            lst = [];
            records.set(key, lst);
          }


          lst.push({
            humidity: childSnapshot.child("humidityPercent").val(),
            temp: childSnapshot.child("temparatureCelcius").val(),
            time: new Date(childSnapshot.child("time").val()),

          });


          return false;

        });



        if (c._mounted) {
          c.setState({
            records: records,
            minDisp: -minDisp, maxDisp: -maxDisp
          });
        }


      }
    });
  }
  componentDidMount() {
    this._mounted = true;

    this.runQuery();

  }

  
  componentWillUnmount() {
    this._mounted = false;
  }


  runQuery() {
    const c = this;
    const query3 = firebase.database().ref('locations/' + this.locationHolder.getLocation() + "/humidityTemperatureSequence").on("value",
      function (snapshot) {
        c.queryRecentData(snapshot.val());



      }

    );

  }



  render() {
    const loc = this.locationHolder.getLocation();
    var out = [];
    var lineChartOptions = {
      scales: {
        yAxes: [{
          id: 'A',
          type: 'linear',
          position: 'left',
        }, {
          id: 'B',
          type: 'linear',
          position: 'right',
          ticks: {
            max: 100,
            min: 0
          }
        }]
      },
      scaleShowGridLines: true,
      scaleGridLineColor: 'rgba(0,0,0,.05)',
      scaleGridLineWidth: 1,
      scaleShowHorizontalLines: true,
      scaleShowVerticalLines: true,
      bezierCurve: true,
      bezierCurveTension: 0.4,
      pointDot: true,
      pointDotRadius: 4,
      pointDotStrokeWidth: 1,
      pointHitDetectionRadius: 20,
      datasetStroke: true,
      datasetStrokeWidth: 2,
      datasetFill: true,
      legendTemplate: '<ul class=\"<%=name.toLowerCase()%>-legend\"><% for (var i=0; i<datasets.length; i++){%><li><span style=\"background-color:<%=datasets[i].strokeColor%>\"></span><%if(datasets[i].label){%><%=datasets[i].label%><%}%></li><%}%></ul>',
    };
    var k = 0;

    this.state['records'].forEach((v, k) => {

      var allTimes = [];
      var tempPoints = [];
      var humidityPoints = [];
      var splt = k.split(',');
      var device = splt[0];
      var location = splt[1];

      v.forEach(v => {

        allTimes.push(helpers.convertTS(v['time']));
        tempPoints.push(v['temp']);
        humidityPoints.push(v['humidity']);




      });
      
      var dataLine = {
        labels: allTimes,
        datasets: [
          {
            label: 'Device Temperature',
            pointBackgroundColor: 'rgba(200,50,50,0.8)',
            backgroundColor :'rgba(200,50,50,0.1)',


            data: tempPoints,
            yAxisID: 'A',
          },

          {
            label: 'Ambient Humidity',
            pointBackgroundColor: 'rgba(50,50,220,0.8)',
            backgroundColor :'rgba(50,50,220,0.1)',
           
            data: humidityPoints,
            yAxisID: 'B',
          },

        ]
      };
      allTimes.reverse();
      tempPoints.reverse();
      humidityPoints.reverse();
      k = k+1;
      out.push(
        <div className="card mb-3" key='{k}'>
          <div className="card-header">
            <i className="fa fa-exclamation-circle"></i>&nbsp;Device Health Monitoring for {device} in {location}</div>
          <div className="card-body ">
            <div className="table-responsive nohscroll">
              <Line data={dataLine} options={lineChartOptions} />
            </div>
          </div>
        </div>

      );
    });

    return <div>
      <ol className="breadcrumb">
        <li className="breadcrumb-item">
          <Link to="locations">Locations</Link>
        </li>
        <li className="breadcrumb-item">
          <Link to="status">{loc}</Link>
        </li>
        <li className="breadcrumb-item active">Device Health</li>
      </ol>


      {out}

      <div className="card mb-3">
          <div className="card-header">
            <i className="fa fa-thermometer-quarter"></i>&nbsp;Current Device Status at {loc}</div>
          <div className="card-body">
            All devices in normal bounds
          </div>

      </div>

    </div>

  }

}
export default DeviceHealthPage;
