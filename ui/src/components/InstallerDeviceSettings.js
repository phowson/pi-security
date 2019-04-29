import React from 'react';
import { Route, Link, withRouter } from 'react-router-dom';
import firebase from '../firebase/firebase.js';
import * as helpers from '../helpers/datehelpers.js';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'


class InstallerDeviceSettingsPage extends React.Component {

  constructor(params) {
    super(params);
    this.locationHolder = params['locationHolder'];
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

      }
    });

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
        <li className="breadcrumb-item active">Installer's Settings</li>
      </ol>



      <div className="card mb-3">
        <div className="card-header">
          <i className="fa fa-eye"></i>&nbsp;Installer's Sensor Settings</div>
        <div className="card-body ">
        </div>
      </div>
    </div>


  }

}
export default InstallerDeviceSettingsPage;
