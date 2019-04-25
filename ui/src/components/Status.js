import React from 'react';
import firebase from '../firebase/firebase.js';
import { Route, Link, withRouter } from 'react-router-dom';
import * as helpers from '../helpers/datehelpers.js';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import AuthUserContext from './AuthUserContext.js'


class RecentCallsList extends React.Component {
  constructor(params) {
    super();
    this.getRecentCalls = params['getRecentCalls'];
    this.locationHolder = params['locationHolder']
  }

  render() {
    let l = this.getRecentCalls();
    let c = this;

    return (
      <table
        className="table-striped table-hover table-bordered table-condensed"
        cellPadding="2px"
      >
        <thead>
          <tr>
            <th>
              Time
            </th>
            <th>
              Number
            </th>
            <th>
              Comment
            </th>
            <th>
              Message
            </th>
            <th>
              Answered
            </th>
          </tr>

        </thead>
        <tbody>
          {

            l ?
              l.map((item) => {
                return (
                  <tr key={item.key}>
                    <td key={item.key} className="small">
                      {item.time}
                    </td>


                    <td >
                      {item.number}
                    </td>

                    <td >
                      {item.label}
                    </td>

                    <td >
                      <div style={{ "maxWidth": 200 }} >
                        {item.message}
                      </div>
                    </td>

                    <td >
                      {item.answered ? (<FontAwesomeIcon icon="check" />) : ""}
                    </td>

                  </tr>
                )
              })
              : ""

          }
        </tbody>
      </table>);
  }

}


class RecentEventsList extends React.Component {

  constructor(params) {
    super();
    this.getRecentEvents = params['getRecentEvents'];
    this.locationHolder = params['locationHolder']
  }


  render() {
    let l = this.getRecentEvents();
    let c = this;

    return (
      <table
        className="table-striped table-hover table-bordered table-condensed"
        cellPadding="2px"
      >
        <thead>
          <tr>
            <th>
              Time
            </th>

            <th>
              Device
            </th>
            <th>
              Event Description
            </th>
            <th>
              Can trigger alarm
            </th>

          </tr>
        </thead>
        <tbody>
          {

            l ?
              l.map((item) => {
                return (
                  <tr key={item.key}>
                    <td key={item.key} className="small">
                      {item.time}
                    </td>


                    <td >
                      {item.deviceId}
                    </td>

                    <td >
                      {item.label}
                    </td>

                    <td >

                      <div style={(item.notify) ? { color: "red" } : {}} >
                        {item.notify ? (<FontAwesomeIcon icon="check" />) : ""}

                      </div>
                    </td>

                  </tr>
                )
              })
              : ""

          }
        </tbody>
      </table>
    );


  }

}




class DeviceList extends React.Component {

  constructor(params) {
    super();
    this.getHeartbeats = params['getHeartbeats'];
    this.locationHolder = params['locationHolder'];
    this.username = params['username'];
    this.onArmClicked = this.onArmClicked.bind(this);
    this.onDisarmClicked = this.onDisarmClicked.bind(this);
    this.onTriggerBell = this.onTriggerBell.bind(this);
    this.onReset = this.onReset.bind(this);
  }


  sendCommand(dbKey, cmd) {
    const t = this;
    return (e) => {
      const command = firebase.database().ref('locations/' + this.locationHolder.getLocation() + "/device-config/" + dbKey + "/command");
      command.set(
        {
          "command": cmd,
          "applied": false,
          "user": t.username,
          "timestamp": Date.now()

        }
      );

    }

  }

  onArmClicked(dbKey) {
    return this.sendCommand(dbKey, "ARM");

  }



  onDisarmClicked(dbKey) {
    return this.sendCommand(dbKey, "DISARM");
  }

  onTriggerBell(dbKey) {
    return this.sendCommand(dbKey, "TRIGGER_ALARM");
  }

  onReset(dbKey) {

    return this.sendCommand(dbKey, "RESET_ALARM");

  }

  render() {
    let l = this.getHeartbeats();
    let c = this;

    return (
      <table
        className="table-striped table-hover table-bordered table-condensed"
        cellPadding="2px"
      >
        <thead>
          <tr>
            <th>
              Device Name
            </th>
            <th>
              Last Heartbeat
            </th>
            <th>
              Last Alarm
            </th>
            <th>
              Currently Armed
            </th>
            <th>
              Alarm bell
            </th>

            <th>
              Controls
            </th>

          </tr>
        </thead>
        <tbody>
          {

            l ?
              l.map((item) => {
                return (
                  <tr key={item.key}>
                    <td key={item.key}>
                      {item.id}
                    </td>

                    <td className="small">

                      <div className={(item.ok) ? "text-success" : "text-danger"} >
                      {item.lastHeartbeat}
                      </div>
                    </td>

                    
                    <td className="small">
                      {item.lastAlarmTime}
                    </td>
                    <td>

                    <div className={(item.armed) ? "text-danger" : "text-success"} >
                        {(item.armed) ? "ARMED" : "DISARMED"}
                      </div>

                    </td>
                    <td>
                      {(item.alarmTriggered) ? "RINGING" : "SILENT"}

                    </td>

                    <td>

                      <div className="btn-group" role="group">
                        <button className="btn btn-primary" onClick={c.onArmClicked(item.key)}>Arm</button>
                        <div style={{ width: 5, height: "auto", display: "inline-block" }} />
                        <button className="btn btn-primary" onClick={c.onDisarmClicked(item.key)}>Disarm</button>
                        <div style={{ width: 5, height: "auto", display: "inline-block" }} />
                        <button className="btn btn-primary" onClick={c.onTriggerBell(item.key)}>Trigger bell</button>
                        <div style={{ width: 5, height: "auto", display: "inline-block" }} />
                        <button className="btn btn-primary" onClick={c.onReset(item.key)}>Reset</button>
                      </div>

                    </td>

                  </tr>
                )
              })
              : ""

          }
        </tbody>
      </table>
    );


  }

}




class StatusPage extends React.Component {

  constructor(props) {
    super(props);


    this.locationHolder = props['locationHolder'];

    this.state = {
      heartbeats: [],
      calls: [],
      events: [],
      lastCloudHb: null
    };
    this._mounted = false;

    this.limitTo = 10;
    this.limitToCalls = 4;

    this.onTestCloud.bind(this);
    this.queryRecentEvents.bind(this);
  }

  componentWillUnmount() {
    this._mounted = false;
  }



  queryRecentEvents(maxEventSeq) {
    var c = this;
    const query2 = firebase.database().ref('locations/' + this.locationHolder.getLocation() + "/events").orderByChild("sequenceId").startAt(maxEventSeq).endAt(maxEventSeq + this.limitTo);
    query2.on("value", function (snapshot) {
      if (c._mounted) {


        var events = [];


        snapshot.forEach(function (childSnapshot) {
          events.push({
            key: childSnapshot.key,
            id: childSnapshot.key,
            time: helpers.convertTS(childSnapshot.child("timestamp").val()),
            eventType: childSnapshot.child("type").val(),
            deviceId: childSnapshot.child("deviceId").val(),
            label: childSnapshot.child("label").val(),
            notify: childSnapshot.child("notify").val(),

          });





          return false;

        });
        c.setState({ events: events });
      }
    });
  }

  queryRecentCalls(maxEventSeq) {
    var c = this;
    const query2 = firebase.database().ref('/calls').orderByChild("sequenceId").startAt(maxEventSeq).endAt(maxEventSeq + this.limitToCalls);
    query2.on("value", function (snapshot) {
      if (c._mounted) {


        var calls = [];


        snapshot.forEach(function (childSnapshot) {
          calls.push({
            key: childSnapshot.key,
            id: childSnapshot.key,
            time: helpers.convertTS(childSnapshot.child("time").val()),
            answered: childSnapshot.child("answered").val(),
            label: childSnapshot.child("label").val(),
            message: childSnapshot.child("message").val(),
            number: childSnapshot.child("number").val(),

          });





          return false;

        });
        c.setState({ calls: calls });
      }
    });
  }


  componentDidMount() {
    this._mounted = true;
    const c = this;


    const query = firebase.database().ref('locations/' + this.locationHolder.getLocation() + "/heartbeat").orderByKey();
    var t = this;
    query.on("value", function (snapshot) {
      if (c._mounted) {


        var heartbeats = [];

        const now = new Date().getTime();


        snapshot.forEach(function (childSnapshot) {
          const lastTs= childSnapshot.child("timestamp").val();
          const ok = now - lastTs <60000;
          heartbeats.push({
            key: childSnapshot.key,
            id: childSnapshot.key,
            lastHeartbeat: helpers.convertTS(lastTs),
            lastAlarmTime: helpers.convertTS(childSnapshot.child("lastAlarmTime").val()),
            armed: childSnapshot.child("armed").val(),
            ok : ok,
            alarmTriggered: childSnapshot.child("alarmTriggered").val(),
          });




          return false;

        });
        c.setState({ heartbeats: heartbeats });
      }
    });


    const query3 = firebase.database().ref('locations/' + this.locationHolder.getLocation() + "/eventsSequence").on("value",
      function (snapshot) {
        c.queryRecentEvents(snapshot.val());

      }

    );

    const query4 = firebase.database().ref('/callSequence').on("value",
      function (snapshot) {
        c.queryRecentCalls(snapshot.val());

      }

    );

    const query5 = firebase.database().ref('locations/' + this.locationHolder.getLocation() + "/lastCloudHeartbeat");
    query5.on("value", function (snapshot) {
      if (c._mounted) {
        c.setState({ lastCloudHb: new Date(snapshot.val()) });
      }
    });



  }


  onTestCloud() {


    return (evt) => {
      const cmd = firebase.database().ref('locations/' + this.locationHolder.getLocation() + "/cloudCommand")

      cmd.set(
        {
          "command": "TEST_CALL",
          "applied": false,
        }
      );
    }

  }

  render() {
    var loggedInUser;

    loggedInUser = "unknown";
    const t = this;
    const loc = this.locationHolder.getLocation();

    const lastCloudHb = this.state['lastCloudHb'];
    const cloudOk =
      lastCloudHb ? new Date().getTime() - lastCloudHb.getTime() < 20000 :
        false;



    return (
      <div>
        <ol className="breadcrumb">
          <li className="breadcrumb-item">
            <Link to="locations">Locations</Link>
          </li>
          <li className="breadcrumb-item">
            <Link to="status">{loc}</Link>
          </li>
          <li className="breadcrumb-item active">Current Status</li>
        </ol>

        <div className="row">
          <div className="col">

            <div className="card mb-3">
              <div className="card-header">
                <i className="fa fa-eye"></i>&nbsp;Current System Status at {loc}</div>
              <div className="card-body">
                <div className="table-responsive">

                  <AuthUserContext.Consumer>
                    {authUser =>
                      authUser ?
                        <DeviceList
                          username={authUser.email}
                          getHeartbeats={() => t.state["heartbeats"]} locationHolder={t.locationHolder} />
                        : ""
                    }
                  </AuthUserContext.Consumer>

                </div>

              </div>
            </div>
          </div>

          <div className="col">

            <div className="card mb-3">
              <div className="card-header">
                <i className="fa fa-cloud"></i>&nbsp;Current Cloud Status at {loc}</div>
              <div className="card-body">
                <table
                  className="table-hover table-bordered table-condensed"
                  cellPadding="2px"
                >
                  <thead>
                    <tr>

                      <th>Last Heartbeat</th>
                      <th>Status</th>
                      <th>Controls</th>

                    </tr>
                  </thead>
                  <tbody>
                    <tr>

                      <td className="small">
                        {lastCloudHb && lastCloudHb > 0 ? helpers.convertTS(lastCloudHb) : "None"}


                      </td>

                      <td style={(cloudOk) ? { color: "green" } : { color: "red" }} >
                        {cloudOk ? "OK" : "DOWN"}
                      </td>


                      <td>
                        <div className="btn-group" role="group">
                          <button className="btn btn-primary" onClick={t.onTestCloud()}>Test</button>
                        </div>
                      </td>


                    </tr>

                  </tbody>

                </table>


              </div>
            </div>
          </div>

        </div>

        <div className="row">
          <div className="col">
            <div className="card mb-3">
              <div className="card-header">
                <i className="fa fa-calendar"></i>&nbsp;Recent events at {loc}</div>
              <div className="card-body">
                <div className="table-responsive">
                  <RecentEventsList getRecentEvents={() => t.state["events"]} locationHolder={t.locationHolder} />
                </div>
              </div>
            </div>
          </div>

          <div className="col">
            <div className="card mb-3">
              <div className="card-header">
                <i className="fa fa-phone"></i>&nbsp;Recent calls at {loc}</div>
              <div className="card-body">
                <div className="table-responsive">
                  <RecentCallsList getRecentCalls={() => t.state["calls"]} locationHolder={t.locationHolder} />
                </div>
              </div>
            </div>
          </div>
        </div>



      </div >


    );

  }

}


export default StatusPage;
