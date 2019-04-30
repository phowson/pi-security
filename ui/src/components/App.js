import React from 'react';
import {
  BrowserRouter as Router,
  Route,
} from 'react-router-dom';


import PlanPage from './Plan';
import Navigation from './Navigation';
import LandingPage from './Landing';
import SignUpPage from './SignUp';
import SignInPage from './SignIn';
import PasswordForgetPage from './PasswordForget';
import LocationsPage from './Locations';
import AccountPage from './Account';
import VulnerabilitiesPage from './Vulnerabilities'

import InstallerDeviceSettingsPage from './InstallerDeviceSettings';
import SensorHealthPage from './SensorHealth';


import StatusPage from './Status';
import ActivityPage from './Activity';
import StatsPage from './Stats';

import DeviceSettingsPage from './DeviceSettings';
import MonitorSettingsPage from './MonitorSettings';
import DeviceHealthPage from './DeviceHealth';

import PermissionsPage from './Permissions';
import CallLogPage from './CallLog';
import ArmDisarmPage from './ArmDisarm';


import LocationContext from './LocationContext';

import * as routes from '../constants/routes';
import withAuthentication from './withAuthentication';

import firebase from '../firebase/firebase.js';
import { library } from '@fortawesome/fontawesome-svg-core'
import {faCheck, faTimes, faEye } from '@fortawesome/free-solid-svg-icons'

library.add( faCheck)
library.add( faTimes)
library.add( faEye)


class MainApp extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      location: null
    };
    this.setLocation = this.setLocation.bind(this);

  }


  setLocation(newLocation) {
    this.setState(
      {
        location : newLocation
      }
    );
  }


  getLocation() {
    const {location} = this.state;
    return location;
  }

  render() {

    const t = this;
    const {location} = this.state;
    return (
      <Router>
        <div>


          <LocationContext.Provider value={location}>
            <Navigation />
          </LocationContext.Provider>
          <div className="content-wrapper">
            <div className="container-fluid">
              <Route exact path={routes.LANDING} component={() => <LandingPage />} />
              <Route exact path={routes.PLAN} component={() => <PlanPage locationHolder={t}/>} />
              <Route exact path={routes.SIGN_UP} component={() => <SignUpPage />} />
              <Route exact path={routes.SIGN_IN} component={() => <SignInPage locationHolder={t}/>} />
              <Route exact path={routes.PASSWORD_FORGET} component={() => <PasswordForgetPage />} />
              <Route exact path={routes.LOCATIONS} component={() => <LocationsPage locationHolder = {t}/>} />
              <Route exact path={routes.ACCOUNT} component={() => <AccountPage />} />


              <Route exact path={routes.STATUS} component={() => <StatusPage locationHolder = {t}/>} />
              <Route exact path={routes.ACTIVITY} component={() => <ActivityPage locationHolder = {t}/>} />
              <Route exact path={routes.STATS} component={() => <StatsPage locationHolder = {t}/>} />


              <Route exact path={routes.DEVICESETTINGS} component={() => <DeviceSettingsPage locationHolder = {t}/>} />
              <Route exact path={routes.INSTALLERDEVICESETTINGS} component={() => <InstallerDeviceSettingsPage locationHolder = {t}/>} />


              <Route exact path={routes.MONITORINGSETTINGS} component={() => <MonitorSettingsPage locationHolder = {t}/>} />

              <Route exact path={routes.SENSORHEALTH} component={() => <SensorHealthPage locationHolder = {t}/>} />

              <Route exact path={routes.DEVICEHEALTH} component={() => <DeviceHealthPage locationHolder = {t}/>} />

              <Route exact path={routes.ARMDISARM} component={() => <ArmDisarmPage locationHolder = {t}/>} />

              <Route exact path={routes.PERMISSIONS} component={() => <PermissionsPage locationHolder = {t}/>} />
              <Route exact path={routes.CALLLOG} component={() => <CallLogPage locationHolder = {t}/>} />

              <Route exact path={routes.VULNERABILITIES} component={() => <VulnerabilitiesPage locationHolder = {t}/>} />


            </div>
          </div>


        </div>

      </Router> );
    }


  }

  const App = () => <MainApp/>


export default withAuthentication(App);
