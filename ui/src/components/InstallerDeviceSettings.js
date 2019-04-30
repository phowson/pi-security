import React from 'react';
import { Route, Link, withRouter } from 'react-router-dom';
import firebase from '../firebase/firebase.js';
import * as helpers from '../helpers/datehelpers.js';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'



class SensorConfig {
  constructor(key) {
    this.key = key;
    this.enabled = false;
    this.label = null;
    this.gpioPin = null;
    this.pullResistance = null;
    this.edges = null;
    this.raiseImmediately = null;
    this.raisesAlert = null;
    this.reportingEnabled = null;
    this.sensorType = null;

  }


}


class AuxSettingsForm extends React.Component {

  constructor(params) {
    super(params);
    this.state = {
      auxSettings: params['auxSettings']
    };
  }


  render() {
    const t = this;

    const e = this.state['auxSettings'];

    return <div>

      <div>
        <label>De-bounce delay for sensors :&nbsp;</label>
        <input type="text" value={e.deBounceDelay} size="5"
          onChange={
            (evt) => {
              e.deBounceDelay = evt.target.value;
              t.setState({ auxSettings: this.state['auxSettings'] });
            }} />
        <label>&nbsp;milliseconds</label>
      </div>
      <hr></hr>
      <div className="btn-group" role="group">
        <button className="btn btn-primary" type="submit" >
          Save
                </button>
      </div>

    </div>;
  }
}


class ExpandedSensorConfigList extends React.Component {
  constructor(params) {
    super(params);
    this.onSaved = params['onSaved'];
    this.state = {
      sensorConfig: params['sensorConfig']
    };
    this.onSavedClicked = this.onSavedClicked.bind(this)
  }

  onSavedClicked(evt) {

    evt.preventDefault();
    this.onSaved(this.state['sensorConfig']);
  }


  render() {
    const t = this;
    var i = 0;
    return (
      <div>
        <form onSubmit={t.onSavedClicked}>

          <table
            className="table-striped table-hover table-bordered table-condensed"
            cellPadding="5px"
          >
            <thead>
              <tr>
                <td>
                  Sensor Name
              </td>

                <td>
                  Sensor Type
              </td>

                <td>
                  Enabled
              </td>

                <td>
                  GPIO Pin
              </td>

                <td>
                  Edges for activation
              </td>

                <td>
                  Resistive Pull up / down
              </td>

                <td>
                  Raises alert immediately
              </td>

                <td>
                  Raises alert
              </td>

                <td>
                  Reporting enabled
              </td>


                <td>

                </td>

              </tr>
            </thead>

            <tbody>

              {t.state['sensorConfig'].map((e) => {

                return <tr key={(++i)}>

                  <td>
                    <input type="text" value={e.label} size="30"
                      onChange={
                        (evt) => {
                          e.label = evt.target.value;
                          t.setState({ sensorConfig: this.state['sensorConfig'] });
                        }} />
                  </td>

                  <td>
                    <select value={e.sensorType}
                      onChange={
                        (evt) => {
                          e.sensorType = evt.target.value;
                          t.setState({ sensorConfig: this.state['sensorConfig'] });
                        }}>
                      <option key="MOTION_SENSOR" value="MOTION_SENSOR">Motion sensor</option>
                      <option key="DOOR_SENSOR" value="DOOR_SENSOR">Door sensor</option>
                      <option key="TAMPER" value="TAMPER">Tamper</option>

                    </select>
                  </td>

                  <td>
                    {e.enabled}
                    <input type="checkbox" checked={e.enabled}
                      onChange={



                        (evt) => {

                          e.enabled = evt.target.checked;
                          t.setState({ sensorConfig: this.state['sensorConfig'] });
                        }}

                    />

                  </td>

                  <td>
                    <input type="text" value={e.gpioPin} maxLength="2" size="3"
                      onChange={
                        (evt) => {
                          e.gpioPin = evt.target.value;
                          t.setState({ sensorConfig: this.state['sensorConfig'] });
                        }} />
                  </td>


                  <td>
                    <select value={e.edges}
                      onChange={
                        (evt) => {
                          e.edges = evt.target.value;
                          t.setState({ sensorConfig: this.state['sensorConfig'] });
                        }}>
                      <option key="UP" value="UP">Up edge</option>
                      <option key="DOWN" value="DOWN">Down edge</option>
                      <option key="BOTH" value="BOTH">Both edges</option>

                    </select>
                  </td>

                  <td>
                    <select value={e.pullResistance}
                      onChange={
                        (evt) => {
                          e.pullResistance = evt.target.value;
                          t.setState({ sensorConfig: this.state['sensorConfig'] });
                        }}>
                      <option key="OFF" value="OFF">Off</option>
                      <option key="PULL_UP" value="PULL_UP">Pull up</option>
                      <option key="PULL_DOWN" value="PULL_DOWN">Pull down</option>

                    </select>
                  </td>

                  <td>
                    <input type="checkbox" checked={e.raiseImmediately}
                      onChange={
                        (evt) => {
                          e.raiseImmediately = evt.target.checked;
                          t.setState({ sensorConfig: this.state['sensorConfig'] });
                        }}
                    />

                  </td>


                  <td>
                    <input type="checkbox" checked={e.raisesAlert}
                      onChange={
                        (evt) => {
                          e.raisesAlert = evt.target.checked;
                          t.setState({ sensorConfig: this.state['sensorConfig'] });
                        }}
                    />

                  </td>

                  <td>
                    <input type="checkbox" checked={e.reportingEnabled}
                      onChange={
                        (evt) => {
                          e.reportingEnabled = evt.target.checked;
                          t.setState({ sensorConfig: this.state['sensorConfig'] });
                        }}
                    />

                  </td>

                  <td>
                    <div className="btn-group" role="group">
                      <button className="btn btn-primary">Remove</button>
                    </div>


                  </td>

                </tr>
              }
              )}


            </tbody>

          </table>
          <hr></hr>
          <div className="btn-group" role="group">
            <button className="btn btn-primary" type="submit" >
              Save
                </button>
          </div>


        </form>
      </div>);
  }


}


class InstallersDeviceSettingsPage extends React.Component {

  constructor(params) {
    super(params);
    this.locationHolder = params['locationHolder'];

    this.state = {

      monitorConfig: new Map()

    };
    this.saveState = this.saveState.bind(this);
  }

  componentWillUnmount() {
    this._mounted = false;
  }


  componentDidMount() {
    this._mounted = true;
    var t = this;


    const query = firebase.database().ref('locations/' + this.locationHolder.getLocation() + "/device-config");
    query.on("value", function (snapshot) {
      if (t._mounted) {

        var monitorConfig = new Map();
        var auxSettingsMap = new Map();

        snapshot.forEach(function (childSnapshot) {

          var auxSettings = {};
          const mc = childSnapshot.child("monitoringConfig");
          auxSettings.deBounceDelay = mc.child("deBounceDelay").val();
          if (!auxSettings.deBounceDelay) {
            auxSettings.deBounceDelay=0;
          }
          auxSettings.autoTriggerAlarm = mc.child("autoTriggerAlarm").val();
          auxSettings.bellEnabled = mc.child("bellEnabled").val();
          auxSettings.dhtSensorEnabled = mc.child("dhtSensorEnabled").val();
          auxSettings.dhtSensorLocationName = mc.child("dhtSensorLocationName").val();
          auxSettings.dhtSensorPin = mc.child("dhtSensorPin").val();
          auxSettingsMap.set(childSnapshot.key, auxSettings);


          var monConfigItems = mc.child("items");

          var sensorConfig = [];
          monConfigItems.forEach(function (childSnapshot2) {
            var sc = new SensorConfig(childSnapshot2.key);

            sc.label = childSnapshot2.child("label").val();
            sc.enabled = childSnapshot2.child("enabled").val();

            sc.gpioPin = childSnapshot2.child("gpioPin").val();
            sc.pullResistance = childSnapshot2.child("pullResistance").val();
            sc.edges = childSnapshot2.child("edges").val();
            sc.raiseImmediately = childSnapshot2.child("raiseImmediately").val();
            sc.raisesAlert = childSnapshot2.child("raisesAlert").val();
            sc.reportingEnabled = childSnapshot2.child("reportingEnabled").val();
            sc.sensorType = childSnapshot2.child("type").val();

            sensorConfig.push(sc);
          });

          monitorConfig.set(childSnapshot.key, sensorConfig);





        });

        t.setState({ monitorConfig: monitorConfig, auxSettingsMap: auxSettingsMap });
      }
    });



  }


  saveState(key) {
    const t = this;

    return (lst) => {
      const query = firebase.database().ref('locations/' + t.locationHolder.getLocation()

        + "/device-config/" + key + "/monitoringConfig/items");

      lst.forEach(v => {

        query.child(v.key).child("enabled").set(v.enabled);

      });


    }

  }

  render() {
    const loc = this.locationHolder.getLocation();

    const t = this;

    var cards = [];
    var i = 0;

    t.state['monitorConfig'].forEach((v, k) => {

      const auxSettings = t.state['auxSettingsMap'].get(k);

      cards.push(

        <div className="card mb-3" key={++i}>
          <div className="card-header">
            <i className="fa fa-eye"></i>&nbsp;Installer level sensor setup for {k}</div>
          <div className="card-body ">
            <ExpandedSensorConfigList sensorConfig={v} onSaved={t.saveState(k)} />






          </div>
        </div>);
      cards.push(
        <div className="card mb-3" key={++i}>
          <div className="card-header">
            <i className="fa fa-eye"></i>&nbsp;Installer level auxillary setup for {k}</div>
          <div className="card-body ">
            <AuxSettingsForm auxSettings={auxSettings} />
          </div>
        </div>


      );
    })

    return <div>
      <ol className="breadcrumb">
        <li className="breadcrumb-item">
          <Link to="locations">Locations</Link>
        </li>
        <li className="breadcrumb-item">
          <Link to="status">{loc}</Link>
        </li>
        <li className="breadcrumb-item active">Installer's Settings</li>
      </ol>

      {cards}


    </div>


  }

}
export default InstallersDeviceSettingsPage;
