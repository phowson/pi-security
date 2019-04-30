import React from 'react';
import firebase from '../firebase/firebase.js';
import { Route, Link, withRouter } from 'react-router-dom';
import * as helpers from '../helpers/datehelpers.js';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import AuthUserContext from './AuthUserContext.js'

class SimpleMapSensorConfig {

  constructor(key) {
    this.key = key;
    this.x = null;
    this.y = null;
    this.label = null;
  }

}


class PlanPage extends React.Component {

  constructor(props) {
    super(props);


    this.locationHolder = props['locationHolder'];

    this.state = {
      sensorConfig: [],
      lastActivity: new Map()
    };
    this._mounted = false;

    this.limitTo = 100;
    this.queryRecentEvents.bind(this);
  }

  componentWillUnmount() {
    this._mounted = false;
    clearInterval(this.interval);
  }



  queryRecentEvents(maxEventSeq) {
    var c = this;
    const query2 = firebase.database().ref('locations/' + this.locationHolder.getLocation() + "/events").orderByChild("sequenceId").startAt(maxEventSeq).endAt(maxEventSeq + this.limitTo);
    query2.on("value", function (snapshot) {
      if (c._mounted) {


        var events = [];
        var lastActivity = new Map();

        snapshot.forEach(function (childSnapshot) {


          var time = childSnapshot.child("timestamp").val();
          var label = childSnapshot.child("label").val();

          var lastTime = lastActivity.get(label);

          if (!lastTime || time > lastTime) {
            lastActivity.set(label, time);
          }





          return false;

        });

        c.setState({ lastActivity: lastActivity });
      }
    });
  }


  componentDidMount() {
    this._mounted = true;
    const t = this;

    const query = firebase.database().ref('locations/' + this.locationHolder.getLocation() + "/device-config");
    query.on("value", function (snapshot) {
      if (t._mounted) {

        var sensorConfig = [];
        snapshot.forEach(function (childSnapshot) {
          var monConfigItems = childSnapshot.child("monitoringConfig").child("items");


          monConfigItems.forEach(function (childSnapshot2) {
            var sc = new SimpleMapSensorConfig(childSnapshot2.key);

            if (childSnapshot2.child("enabled").val() == true) {
              sc.label = childSnapshot2.child("label").val();
              sc.x = childSnapshot2.child("mapX").val();
              sc.y = childSnapshot2.child("mapY").val();

              if (sc.x && sc.y) {
                sensorConfig.push(sc);
              }
            }


          });





        });


        t.setState({ sensorConfig: sensorConfig });
      }
    });

    const query3 = firebase.database().ref('locations/' + this.locationHolder.getLocation() + "/eventsSequence").on("value",
      function (snapshot) {

        if (t._mounted) {
          t.queryRecentEvents(snapshot.val());
        }

      }

    );

    this.interval = setInterval(() => this.setState({ time: Date.now() }), 10000);
  }


  render() {
    const t = this;
    const loc = this.locationHolder.getLocation();
    const now = new Date().getTime();
    var id = 0;
    return (
      <div>
        <ol className="breadcrumb">
          <li className="breadcrumb-item">
            <Link to="locations">Locations</Link>
          </li>
          <li className="breadcrumb-item">
            <Link to="status">{loc}</Link>
          </li>
          <li className="breadcrumb-item active">Site Plan</li>
        </ol>

        <div className="row">
          <div className="col">

            <div className="card mb-3">
              <div className="card-header">
                <i className="fa fa-eye"></i>&nbsp;Site plan at {loc}</div>
              <div className="card-body">


                <div style={{
                  top: 0,
                  left: 0,
                  zOrder: 0,
                  position: 'relative'
                }}>

                  <img src={require("../assets/images/office-building-plan.png")} alt="Logo" />
                  {t.state['sensorConfig'].map(
                    (e) => {
                      const ets = t.state['lastActivity'].get(e.label);
                      return <div
                      key={++id}
                        style={{
                          left: e.x,
                          top: e.y,
                          position: 'absolute',
                          zOrder: 1,

                        }}

                      >
                        <i className="fa fa-eye"></i>
                        <div
                          className="text-light small"
                          style={{ opacity: 0.75, backgroundColor: "#000000", }}>
                          <div>{e.label}</div>
                          <div className={ets && (now - ets<20000) ? "text-danger" : "text-light"}>
                            Last Activity : {ets ? helpers.convertTS(ets) : "No recent activity"} </div>
                        </div>

                      </div>;
                    })}


                </div>





              </div>
            </div>
          </div>




        </div >
      </div>


    );

  }

}


export default PlanPage;

