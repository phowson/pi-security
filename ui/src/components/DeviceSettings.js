import React from 'react';
import { Route, Link, withRouter } from 'react-router-dom';
import firebase from '../firebase/firebase.js';
import * as helpers from '../helpers/datehelpers.js';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'



class SimpleSensorConfig {
  constructor(key) {
    this.key = key;
    this.enabled = false;
    this.label = null;
  }


}


class SimpleSensorConfigList extends React.Component {
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
                  Enabled
              </td>
              </tr>
            </thead>

            <tbody>

              {t.state['sensorConfig'].map((e) => {

                return <tr key={(++i)}>

                  <td>
                    {e.label}
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


                </tr>;
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


class DeviceSettingsPage extends React.Component {

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

        snapshot.forEach(function (childSnapshot) {
          var monConfigItems = childSnapshot.child("monitoringConfig").child("items");

          var sensorConfig = [];
          monConfigItems.forEach(function (childSnapshot2) {
            var sc = new SimpleSensorConfig(childSnapshot2.key);

            sc.label = childSnapshot2.child("label").val();
            sc.enabled = childSnapshot2.child("enabled").val();

            sensorConfig.push(sc);
          });

          monitorConfig.set(childSnapshot.key, sensorConfig);





        });


        t.setState({ monitorConfig: monitorConfig });
      }
    });



  }


  saveState(key) {
    const t = this;

    return (lst) => {
      const query = firebase.database().ref('locations/' + t.locationHolder.getLocation() 

      + "/device-config/" + key +"/monitoringConfig/items");      

      lst.forEach( v => {

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
      cards.push(

        <div className="card mb-3" key={++i}>
          <div className="card-header">
            <i className="fa fa-eye"></i>&nbsp;Sensor Settings for {k}</div>
          <div className="card-body ">
            <SimpleSensorConfigList sensorConfig={v} onSaved={t.saveState(k)} />


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
        <li className="breadcrumb-item active">Device Setup</li>
      </ol>

      {cards}


    </div>


  }

}
export default DeviceSettingsPage;
