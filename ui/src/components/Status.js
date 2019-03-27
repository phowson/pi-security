import React from 'react';
import firebase from '../firebase/firebase.js';
import { Route, Link, withRouter } from 'react-router-dom';
import * as routes from '../constants/routes';
import * as helpers from '../helpers/datehelpers.js';



class DeviceList extends React.Component {

  constructor(params) {
    super();
    this.getHeartbeats = params['getHeartbeats'];
    this.locationHolder = params['locationHolder']
    this.onArmClicked = this.onArmClicked.bind(this);
    this.onDisarmClicked = this.onDisarmClicked.bind(this);
    this.onTriggerBell = this.onTriggerBell.bind(this);
    this.onReset = this.onReset.bind(this);
  }

  onArmClicked(dbKey) {


    return (e) => {
      const command = firebase.database().ref('locations/' + this.locationHolder.getLocation()  +"/device-config/" + dbKey + "/command");
      command.set(
        {
            "command" : "ARM",
            "applied" : false


        }
      );
     
    }

  }


  onDisarmClicked(dbKey) {

    return (e) => {
      const command = firebase.database().ref('locations/' + this.locationHolder.getLocation()  +"/device-config/" + dbKey + "/command");
      command.set(
        {
            "command" : "DISARM",
            "applied" : false


        }
      );
     
    }


  }

  onTriggerBell(dbKey) {

    return (e) => {
      const command = firebase.database().ref('locations/' + this.locationHolder.getLocation()  +"/device-config/" + dbKey + "/command");
      command.set(
        {
            "command" : "TRIGGER_ALARM",
            "applied" : false


        }
      );
     
    }

  }

  onReset(dbKey) {

    return (e) => {
      const command = firebase.database().ref('locations/' + this.locationHolder.getLocation()  +"/device-config/" + dbKey + "/command");
      command.set(
        {
            "command" : "RESET_ALARM",
            "applied" : false


        }
      );
     
    }

    
  }

  render() {
    let l = this.getHeartbeats();
    let c = this;

    return (
      <table
        className="table-striped table-hover table-bordered table-condensed"
        cellPadding="2pt"
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

                    <td>
                    {item.lastHeartbeat}
                  </td>
                    <td>
                    {item.lastAlarmTime}
                  </td>
                    <td>

                      <div style={ (item.armed)  ?  {color:"red"}: {color:"green"} } >
                      {   (item.armed)  ? "ARMED":  "DISARMED" }
                      </div>

                  </td>
                    <td>
                    {   (item.alarmTriggered)  ? "RINGING":  "SILENT" }
                      
                  </td>

                  <td>

                    <button onClick={ c.onArmClicked(item.key) }>Arm</button>
                    <div style={ {width : 5 , height:"auto" , display : "inline-block"} } />                    
                    <button onClick={ c.onDisarmClicked(item.key) }>Disarm</button>
                    <div style={ {width : 5 , height:"auto" , display : "inline-block"} } />       
                    <button  onClick={ c.onTriggerBell(item.key) }>Trigger bell</button>
                    <div style={ {width : 5 , height:"auto" , display : "inline-block"} } />       
                    <button  onClick={ c.onReset(item.key) }>Reset</button>

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
    };
  }

  componentWillUnmount() {
    this._mounted = false;
  }




  componentDidMount() {
    this._mounted = true;
    const c = this;
    const query = firebase.database().ref('locations/' + this.locationHolder.getLocation() + "/heartbeat").orderByKey();
    var t = this;
    query.on("value", function (snapshot) {
      if (c._mounted) {


        var heartbeats = [];


        snapshot.forEach(function (childSnapshot) {
          heartbeats.push({
            key: childSnapshot.key,
            id: childSnapshot.key,
            lastHeartbeat: helpers.convertTS(childSnapshot.child("timestamp").val()),
            lastAlarmTime :helpers.convertTS(childSnapshot.child("lastAlarmTime").val()),
            armed: childSnapshot.child("armed").val(),
            alarmTriggered: childSnapshot.child("alarmTriggered").val(),
          });

          console.log(heartbeats);

          c.setState({ heartbeats: heartbeats });

          return false;

        });
      }


    });
  }

  render() {

    const t = this;
    const loc = this.locationHolder.getLocation();
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

        <div className="card mb-3">
          <div className="card-header">
            <i className="fa fa-table"></i>&nbsp;Current System Status at {loc}</div>
          <div className="card-body">
            <div className="table-responsive">
              <DeviceList getHeartbeats={() => t.state["heartbeats"]} locationHolder={t.locationHolder} />
            </div>
          </div>
        </div>
      </div>

    );

  }

}


export default StatusPage;
