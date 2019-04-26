import React from 'react';
import { Route, Link, withRouter } from 'react-router-dom';
import firebase from '../firebase/firebase.js';
import * as helpers from '../helpers/datehelpers.js';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'


class DeviceSensorHealth {
  constructor() {
    this.lastActivity = new Map();
    this.deviceOk = new Map();
  }

}




class SensorHealthList extends React.Component {

  constructor(params) {
    super();
    this.sensorHealth = params['sensorHealth'];
    this.locationHolder = params['locationHolder']
  }


  render() {

    let c = this;
    const la = this.sensorHealth.lastActivity;
    const devOk = this.sensorHealth.deviceOk;

    var l = Array.from(this.sensorHealth.lastActivity.keys());
    console.log(l);

    return (
      <table
        className="table-striped table-hover table-bordered table-condensed"
        cellPadding="5px"
      >
        <thead>
          <tr>

            <th>
              Sensor
            </th>
            <th>
              Last activity
            </th>
            <th>
              Status
            </th>

          </tr>
        </thead>
        <tbody>
          {

            l ?
              l.map((item) => {

                const lastActivity = la.get(item);
                const isOk = devOk.get(item) == true; // so this is always not null
                return (
                  <tr key={item}>
                    <td>{item}</td>
                    <td className={isOk? "text-success" : "text-danger"}>{lastActivity ? helpers.convertTS(lastActivity) : "No activity"}</td>
                    <td>{isOk ?
                      <div className="text-success">OK</div> : <div className="text-danger">SUSPECT - Check sensor and settings</div>
                    }</td>


                  </tr>


                )
              }) : ""

          }


        </tbody>
      </table>
    );


  }

}




class SensorHealthPage extends React.Component {

  constructor(props) {
    super(props);
    this.locationHolder = props['locationHolder'];
    this.limitTo = 1000;
    this.offset = 0;
    this.knownMax = 0;
    this.state = {
      sensorHealth: new Map(),
    };
  }

  queryRecentEvents(maxEventSeq) {

    const STALE_TIMEOUT = 3 * 24 * 60 * 60 * 1000;
    maxEventSeq = maxEventSeq + 1;
    this.knownMax = maxEventSeq;
    var c = this;
    var maxDisp = maxEventSeq + this.offset;
    var minDisp = maxEventSeq + this.offset + this.limitTo;

    const query2 = firebase.database().ref('locations/' + this.locationHolder.getLocation() + "/events").orderByChild("sequenceId").startAt(maxDisp).endAt(minDisp);
    query2.on("value", function (snapshot) {
      if (c._mounted) {


        const now = new Date().getTime();
        var sensorHealth = new Map();

        snapshot.forEach(function (childSnapshot) {

          const eventType = childSnapshot.child("type").val();
          const deviceId = childSnapshot.child("deviceId").val();
          const label = childSnapshot.child("label").val();
          const time = childSnapshot.child("timestamp").val();

          if (eventType == "ACTIVITY") {

            var sh = sensorHealth.get(deviceId);
            if (!sh) {
              sensorHealth.set(deviceId, sh = new DeviceSensorHealth());
            }

            const ctime = sh.lastActivity.get(label);
            if (ctime == null || time > ctime) {
              sh.lastActivity.set(label, time);
              sh.deviceOk.set(label, now - time < STALE_TIMEOUT);
            }
          }


          return false;
        });
        c.setState({ sensorHealth: sensorHealth });
      }
    });
  }




  runEventsQuery() {
    const c = this;
    firebase.database().ref('locations/' + this.locationHolder.getLocation() + "/eventsSequence").on("value",
      function (snapshot) {
        c.queryRecentEvents(snapshot.val());

      }

    );

  }
  componentWillUnmount() {
    this._mounted = false;
  }

  componentDidMount() {
    this._mounted = true;

    this.runEventsQuery();

  }

  render() {
    const loc = this.locationHolder.getLocation();
    const t = this;
    const sh = this.state['sensorHealth']
    console.log(sh);

    var cards = [];

    if (sh) {
      var i = 0;
      sh.forEach((v, k) => {
        ++i;
        cards.push((
          <div className="card mb-3  " key={i}>
            <div className="card-header">
              <i className="fa fa-exclamation-circle"></i>Sensor health for {k}</div>
            <div className="card-body ">
              <div className="table-responsive nohscroll">
                <SensorHealthList sensorHealth={v} locationHolder={t.locationHolder} />

              </div>
            </div>
          </div>
        ));

      });
    }




    return <div>
      <ol className="breadcrumb">
        <li className="breadcrumb-item">
          <Link to="locations">Locations</Link>
        </li>
        <li className="breadcrumb-item">
          <Link to="status">{loc}</Link>
        </li>
        <li className="breadcrumb-item active">Sensor Health</li>
      </ol>


      {cards}


    </div>

  }

}
export default SensorHealthPage;
