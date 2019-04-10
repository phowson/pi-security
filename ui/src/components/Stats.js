import React from 'react';
import { Route, Link, withRouter } from 'react-router-dom';
import firebase from '../firebase/firebase.js';
import * as helpers from '../helpers/datehelpers.js';
import { Bar, Pie, Doughnut } from "react-chartjs-2";


class StatsPage extends React.Component {

  constructor(props) {
    super(props);

    this._mounted = false;
    this.locationHolder = props['locationHolder'];

    this.limitTo = 100;
    this.offset = 0;
    this.knownMax = 0;

    this.state = {
      frequencyTable: new Map(),
      hourOfDayTable: new Map(),
      dayOfWeekTable: new Map(),
      perDayOfWeekTable: new Map()
    };

  }


  queryRecentEvents(maxEventSeq) {

    maxEventSeq = maxEventSeq + 1;
    this.knownMax = maxEventSeq;
    var c = this;
    var maxDisp = maxEventSeq + this.offset;
    var minDisp = maxEventSeq + this.offset + this.limitTo;

    const query2 = firebase.database().ref('locations/' + this.locationHolder.getLocation() + "/events").orderByChild("sequenceId").startAt(maxDisp).endAt(minDisp);
    query2.on("value", function (snapshot) {
      if (c._mounted) {


        var events = [];

        var hourOfDayTable = new Map();
        var frequencyTable = new Map();
        var dayOfWeekTable = new Map();
        var perDayOfWeekTable = new Map();

        for (var i = 0; i < 24; ++i) {
          hourOfDayTable.set(i, 0);
        }
        for (var i = 0; i < 7; ++i) {
          dayOfWeekTable.set(i, 0);
          var m = new Map();
          for (var j = 0; j < 24; ++j) {
            m.set(j, 0);
          }

          perDayOfWeekTable.set(i, m);
        }




        snapshot.forEach(function (childSnapshot) {
          if ("ACTIVITY" == childSnapshot.child("type").val()) {
            var d = new Date(childSnapshot.child("timestamp").val());
            hourOfDayTable.set(d.getHours(), hourOfDayTable.get(d.getHours()) + 1);
            var dl = perDayOfWeekTable.get(d.getDay());
            var v = dl.get(d.getHours());
            if (!v) {
              v = 0;
            }
            dl.set(d.getHours(), v + 1);

            dayOfWeekTable.set(d.getDay(), dayOfWeekTable.get(d.getDay()) + 1);



            var l = childSnapshot.child("label").val();
            if (frequencyTable.has(l)) {
              frequencyTable.set(l, frequencyTable.get(l) + 1);
            } else {
              frequencyTable.set(l, 1);
            }

          }




          return false;

        });




        if (c._mounted) {
          c.setState({
            frequencyTable: frequencyTable,
            hourOfDayTable: hourOfDayTable,
            dayOfWeekTable: dayOfWeekTable,
            perDayOfWeekTable: perDayOfWeekTable,

            minDisp: -minDisp, maxDisp: -maxDisp
          });
        }


      }
    });
  }

  runQuery() {
    const c = this;
    const query3 = firebase.database().ref('locations/' + this.locationHolder.getLocation() + "/eventsSequence").on("value",
      function (snapshot) {
        c.queryRecentEvents(snapshot.val());

      }

    );

  }

  componentDidMount() {
    this._mounted = true;

    this.runQuery();

  }


  componentWillUnmount() {
    this._mounted = false;
  }


  makeDataBar(hodt) {
    var hods = [];
    for (var i = 0; i < 24; ++i) {
      var d = hodt.get(i);
      if (d != null) {
        hods.push(d);
      } else {
        hods.push(0);

      }
    }

    var hourLabels = [];
    for (var i = 0; i < 24; ++i) {
      hourLabels.push(i);
    }

    var dataBar2 = {
      labels: hourLabels,
      datasets: [
        {
          label: "Activations",
          data: hods,
          backgroundColor: "rgba(245, 74, 85, 0.8)",
          borderWidth: 1
        },

      ]
    };

    return dataBar2;

  }

  render() {

    const loc = this.locationHolder.getLocation();
    var dows = [];
    var dowt = this.state['dayOfWeekTable'];

    var perDayOfWeekTable = this.state['perDayOfWeekTable'];


    var perDayList = [];

    for (var i = 0; i < 7; ++i) {
      var d = dowt.get(i);
      if (d != null) {
        dows.push(d);
      } else {
        dows.push(0);

      }
    }


    var dataBar1 = {
      labels: helpers.dowNames,
      datasets: [
        {
          label: "Activations",
          data: dows,
          backgroundColor: "rgba(245, 74, 85, 0.5)",
          borderWidth: 1
        },

      ]
    };


    var hodt = this.state['hourOfDayTable'];


    var dataBar2 = this.makeDataBar(hodt);

    var barChartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        xAxes: [
          {
            barPercentage: 1,
            gridLines: {
              display: true,
              color: "rgba(0, 0, 0, 0.1)"
            }
          }
        ],
        yAxes: [
          {
            gridLines: {
              display: true,
              color: "rgba(0, 0, 0, 0.1)"
            },
            ticks: {
              beginAtZero: true
            }
          }
        ]
      }


    };
    var k = 100;
    for (var i = 0; i < 7; ++i) {

      var t = perDayOfWeekTable.get(i);
      if (t) {

        var dataBarD = this.makeDataBar(t);

        perDayList.push(<div key={k++} className="lead">{helpers.dowNames[i]}</div>);
        perDayList.push(<div key={k++}><Bar key={k++} data={dataBarD} options={barChartOptions} /></div>);
      }


    }



    var pieLabels = [];
    var pieData = [];

    this.state['frequencyTable'].forEach((v, k) => {
      pieLabels.push(k);
      pieData.push(v);
    });

    var dataPie = {
      labels: pieLabels,
      datasets: [
        {
          data: pieData,
          backgroundColor: [
            "#F7464A",
            "#46BFBD",
            "#FDB45C",
            "#949FB1",
            "#4D5360",
            "#ac64ad"
          ],
          hoverBackgroundColor: [
            "#FF5A5E",
            "#5AD3D1",
            "#FFC870",
            "#A8B3C5",
            "#616774",
            "#da92db"
          ]
        }
      ]
    };




    return <div>
      <ol className="breadcrumb">
        <li className="breadcrumb-item">
          <Link to="locations">Locations</Link>
        </li>
        <li className="breadcrumb-item">
          <Link to="status">{loc}</Link>
        </li>
        <li className="breadcrumb-item active">Statistics</li>
      </ol>

      <div className="row">
        <div className="col">
          <div className="card">
            <div className="card-header">
              <i className="fa fa-calendar"></i>&nbsp;Sensor Activity by Hour of Day</div>
            <div className="card-body ">
              <Bar data={dataBar2} options={barChartOptions} />
            </div>
          </div>
        </div>
      </div>

      <div className="row">

        <div className="col-sm">
          <div className="card ">
            <div className="card-header">
              <i className="fa fa-calendar"></i>&nbsp;Sensor Activity by Day of Week</div>
            <div className="card-body ">
              <Bar data={dataBar1} options={barChartOptions} />
            </div>
          </div>
        </div>

        <div className="col-sm">
          <div className="card ">
            <div className="card-header">
              <i className="fa fa-search"></i>&nbsp;Sensor Activity Breakdown</div>
            <div className="card-body ">

              <div style={{
                margin: "auto",
                "marginLeft": "auto",
                "marginRight": "auto",
                width: "400px",
                height: "400px",

              }}>
                <Doughnut data={dataPie} options={{
                  responsive: true,
                  maintainAspectRatio: false,

                }} />

              </div>
            </div>
          </div>

        </div>
      </div>

      <div className="row">
        <div className="col">
          <div className="card">
            <div className="card-header">
              <i className="fa fa-search"></i>&nbsp;Detailed per day Breakdown
              </div>
            <div className="card-body ">

              {perDayList}


            </div>
          </div>
        </div>
      </div>


    </div>

  }

}
export default StatsPage;
