import React from 'react';
import { Route, Link, withRouter } from 'react-router-dom';
import firebase from '../firebase/firebase.js';
import * as helpers from '../helpers/datehelpers.js';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'

class NumberRec {
  constructor(number, label) {
    this.number = number;
    this.label = label;
  }

}

class PhoneNumberList extends React.Component {


  constructor(params) {
    super(params);
    this.getListHandler = params['getListHandler'];
    this.saveListHandler = params['saveListHandler'];

    this.onNumberAdd = this.onNumberAdd.bind(this);
    this.onNumberRemove = this.onNumberRemove.bind(this);
    this.numberInput = null;
    this.labelInput = null;
  }


  onNumberAdd(event) {

    event.preventDefault()


    var numbersList = this.getListHandler();
    numbersList.push(new NumberRec(this.numberInput.value, this.labelInput.value));

    this.saveListHandler(numbersList);


  }

  onNumberRemove(numberRec) {
    const t = this;
    return (evt) => {
      var numbersList = t.getListHandler();
      numbersList.splice(numbersList.indexOf(numberRec), 1);
      t.saveListHandler(numbersList);

    }
  }

  render() {

    const t = this;

    var numbersList = this.getListHandler();


    var idx = 0;

    var labelStyle = {

      /* To make sure that all labels have the same size and are properly aligned */
      display: 'inline-block',
      width: '150px',
      textAlign: 'right'



    };

    return <div>


      <table
        className="table-striped table-hover table-bordered"
        cellPadding="5px"
      >
        <thead>
          <tr>
            <th>Number</th>
            <th>Description</th>
            <th></th>




          </tr>
        </thead>
        <tbody>

          {numbersList.length > 0 ?
            numbersList.map((e) => {
              return <tr key={idx++}>
                <td>{e.number}</td>
                <td>{e.label}</td>
                <td>
                  <button className="btn btn-primary" onClick={t.onNumberRemove(e)} >
                    Remove
                  </button>

                </td>

              </tr>

            })

            :

            <tr ><td colSpan="2">No numbers</td></tr>

          }

        </tbody>


      </table>

      <hr></hr>
      <div className="lead">Add new number</div>
      <form onSubmit={t.onNumberAdd}>


        <div>
          <label style={labelStyle}>New number :&nbsp;</label>
          <input key="number" type="text" defaultValue="" maxLength="20" size="12"
            ref={node => (this.numberInput = node)}
          />
        </div>

        <div>
          <label style={labelStyle}>Description :&nbsp;</label>
          <input key="number" type="text" defaultValue="" maxLength="20" size="12"
            ref={node => (this.labelInput = node)}
          />
        </div>

        <div className="btn-group" role="group">
          <button className="btn btn-primary" type="submit" >
            Add
      </button>
        </div>

      </form>

    </div>

  }

}



class MonitorSettingsPage extends React.Component {


  constructor(params) {
    super(params);
    this.locationHolder = params['locationHolder']
    this.state = {
      alarmDelaySeconds: 0,
      heartbeatTimeoutMillis: 0,
      callRetries: 1,

      alarmList: [],
      notificationList: [],

      sendTextsForAlarm: false,
      sendTextsForNotification: false,
      sendTextsOnArmDisarm: false

    };

    this.saveAlarmList = this.saveAlarmList.bind(this);

    this.getNotificationList = this.getNotificationList.bind(this);
    this.saveNotificationList = this.saveNotificationList.bind(this);
    this.onNotificationSettingsUpdated = this.onNotificationSettingsUpdated.bind(this);

  }




  componentWillUnmount() {
    this._mounted = false;
  }


  componentDidMount() {
    this._mounted = true;
    var t = this;


    const query3 = firebase.database().ref('locations/' + this.locationHolder.getLocation() + "/notificationConfig");
    query3.on("value", function (snapshot) {
      if (t._mounted) {
        t.setState({
          callRetries: snapshot.child("callRetries").val(),
          heartbeatTimeoutMillis: snapshot.child("heartbeatTimeoutMillis").val(),
          sendTextsForAlarm: snapshot.child("sendTextsForAlarm").val(),
          sendTextsForNotification: snapshot.child("sendTextsForNotification").val(),
          sendTextsOnArmDisarm: snapshot.child("sendTextsOnArmDisarm").val(),
        });


      }
    });

    const query4 = firebase.database().ref('locations/' + this.locationHolder.getLocation() + "/cloudMonitoringConfig");
    query4.on("value", function (snapshot) {
      if (t._mounted) {
        t.setState({
          alarmDelaySeconds: snapshot.child("alarmDelaySeconds").val(),

        });
      }
    });





    const query = firebase.database().ref('locations/' + this.locationHolder.getLocation() + "/notificationConfig/alarmNotificationList");
    query.on("value", function (snapshot) {
      if (t._mounted) {

        var lst = [];
        snapshot.forEach(function (childSnapshot) {
          lst.push(new NumberRec(childSnapshot.child("number").val(), childSnapshot.child("label").val()));
        });

        t.setState({
          alarmList: lst

        });
      }
    });


    const query2 = firebase.database().ref('locations/' + this.locationHolder.getLocation() + "/notificationConfig/notificationList");
    query2.on("value", function (snapshot) {
      if (t._mounted) {

        var lst = [];
        snapshot.forEach(function (childSnapshot) {
          lst.push(new NumberRec(childSnapshot.child("number").val(), childSnapshot.child("label").val()));
        });

        t.setState({
          notificationList: lst

        });
      }
    });

  }

  onNotificationSettingsUpdated() {

    const t = this;
    return (evt) => {

      evt.preventDefault();

      const ref = firebase.database().ref('locations/' + t.locationHolder.getLocation() + "/notificationConfig");
      ref.child("callRetries").set(Number(t.state['callRetries']));
      ref.child("heartbeatTimeoutMillis").set(Number(t.state['heartbeatTimeoutMillis']));
      ref.child("sendTextsForAlarm").set(t.state['sendTextsForAlarm']);
      ref.child("sendTextsForNotification").set(t.state['sendTextsForNotification']);
      ref.child("sendTextsOnArmDisarm").set(t.state['sendTextsOnArmDisarm']);

      const ref2 = firebase.database().ref('locations/' + this.locationHolder.getLocation() + "/cloudMonitoringConfig");
      ref2.child("alarmDelaySeconds").set(Number(t.state["alarmDelaySeconds"]));
    }




  }


  saveTo(ref, lst) {
    var o = {};
    var i = 0;

    lst.forEach((e) => {
      o[(i++)] = e;
    });

    ref.set(o);
  }



  saveAlarmList(lst) {
    const ref = firebase.database().ref('locations/' + this.locationHolder.getLocation() + "/notificationConfig/alarmNotificationList");
    this.saveTo(ref, lst);

  }


  getNotificationList() {
    return this.state['notificationList'];
  }

  saveNotificationList(lst) {

    const ref = firebase.database().ref('locations/' + this.locationHolder.getLocation() + "/notificationConfig/notificationList");
    this.saveTo(ref, lst);
  }



  render() {
    const loc = this.locationHolder.getLocation();

    const t = this;
    return <div>
      <ol className="breadcrumb">
        <li className="breadcrumb-item">
          <Link to="locations">Locations</Link>
        </li>
        <li className="breadcrumb-item">
          <Link to="status">{loc}</Link>
        </li>
        <li className="breadcrumb-item active">Cloud Monitoring Setup</li>
      </ol>



      <div className="card mb-3">
        <div className="card-header">
          <i className="fa fa-phone"></i>&nbsp;Mobile notification settings</div>
        <div className="card-body ">
          <form onSubmit={t.onNotificationSettingsUpdated()}>


            <div className="lead">Call Preferences</div>

            <div>
              <label>Retry each call&nbsp;</label>

              <select
                onChange={



                  (e) => {
                    t.setState({ callRetries: e.target.value });
                  }}

                key="timesSelect1" value={t.state['callRetries']}>
                <option key="1" value="1">1</option>
                <option key="2" value="2">2</option>
                <option key="3" value="3">3</option>
                <option key="4" value="4">4</option>
                <option key="5" value="5">5</option>
              </select>

              <label>&nbsp;times</label>
            </div>


            <div>
              <label>Upon seeing activity on sensors, notify users after &nbsp;</label>
             
              <input type="text" value={t.state['alarmDelaySeconds']} maxLength="4" size="4"
               

                onChange={
                  (e) => {
                    t.setState({ alarmDelaySeconds: e.target.value });
                  }}

              />

              <label>&nbsp;seconds, if system is not disarmed.</label>


            </div>


            <div>
              <label>Alert after&nbsp;</label>



              <input key="heartbeatTimeoutMillisInput" type="text"
                onChange={
                  (e) => { 
                    t.setState({ heartbeatTimeoutMillis: (e.target.value * 1000) });
                  }}
                value={t.state['heartbeatTimeoutMillis'] / 1000} maxLength="4" size="4"  />
              


              <label>&nbsp;seconds, if connection is lost to alarm system</label>
            </div>


            <hr />
            <div className="lead">Text Message Preferences</div>

            <div>
              <input type="checkbox" name="sendTextsForAlarmCheck" checked={t.state['sendTextsForAlarm']}

                onChange={
                  (e) => {
                    t.setState({ sendTextsForAlarm: e.target.checked });
                  }}
                
              />
              <label>Send text messages when the alarm is triggered&nbsp;</label>
            </div>


            <div>
              <input type="checkbox" name="sendTextsOnArmDisarmCheck" checked={t.state['sendTextsOnArmDisarm']}
                onChange={
                  (e) => {
                    t.setState({ sendTextsOnArmDisarm: e.target.checked });
                  }}

                
              />
              <label>Send text messages when system is armed or disarmed&nbsp;</label>
            </div>


            <div>
              <input type="checkbox" name="sendTextsForNotification" checked={t.state['sendTextsForNotification']}

                onChange={
                  (e) => {
                    t.setState({ sendTextsForNotification: e.target.checked });
                  }}


              />
              <label>Send text messages for notifications&nbsp;</label>

            </div>

            <div className="btn-group" role="group">
              <button className="btn btn-primary" type="submit" >
                Save
                </button>
            </div>

          </form>

        </div>

      </div>

      <div className="card mb-3">
        <div className="card-header">
          <i className="fa fa-phone"></i>&nbsp;Alarm Phone Numbers</div>
        <div className="card-body ">

          <PhoneNumberList getListHandler={() => { return t.state['alarmList']; }} saveListHandler={t.saveAlarmList} />

        </div>

      </div>


      <div className="card mb-3">
        <div className="card-header">
          <i className="fa fa-phone"></i>&nbsp;Notification Phone Numbers</div>
        <div className="card-body ">


          <PhoneNumberList getListHandler={t.getNotificationList} saveListHandler={t.saveNotificationList} />


        </div>

      </div>

    </div>


  }

}
export default MonitorSettingsPage;
