import React from 'react';
import {
  BrowserRouter as Router,
  Route,
} from 'react-router-dom';

import Navigation from './Navigation';
import LandingPage from './Landing';
import SignUpPage from './SignUp';
import SignInPage from './SignIn';
import PasswordForgetPage from './PasswordForget';
import LocationsPage from './Locations';
import AccountPage from './Account';


import StatusPage from './Status';
import ActivityPage from './Activity';
import StatsPage from './Stats';
import ControlPage from './Control';
import SettingsPage from './Settings';


import LocationContext from './LocationContext';

import * as routes from '../constants/routes';
import withAuthentication from './withAuthentication';

import firebase from '../firebase/firebase.js';

class MainApp extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      location: null
    };
    this.setLocation = this.setLocation.bind(this);

  }


  setLocation(newLocation) {
    console.log("Setting location to : " + newLocation);
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

    console.log("Rendering with location = : " +location);
    return (
      <Router>
        <div>


          <LocationContext.Provider value={location}>
            <Navigation />
          </LocationContext.Provider>
          <div className="content-wrapper">
            <div className="container-fluid">
              <Route exact path={routes.LANDING} component={() => <LandingPage />} />
              <Route exact path={routes.SIGN_UP} component={() => <SignUpPage />} />
              <Route exact path={routes.SIGN_IN} component={() => <SignInPage locationHolder={t}/>} />
              <Route exact path={routes.PASSWORD_FORGET} component={() => <PasswordForgetPage />} />
              <Route exact path={routes.LOCATIONS} component={() => <LocationsPage locationHolder = {t}/>} />
              <Route exact path={routes.ACCOUNT} component={() => <AccountPage />} />


              <Route exact path={routes.STATUS} component={() => <StatusPage locationHolder = {t}/>} />
              <Route exact path={routes.ACTIVITY} component={() => <ActivityPage locationHolder = {t}/>} />
              <Route exact path={routes.STATS} component={() => <StatsPage locationHolder = {t}/>} />
              <Route exact path={routes.CONTROL} component={() => <ControlPage locationHolder = {t}/>} />
              <Route exact path={routes.SETTINGS} component={() => <SettingsPage locationHolder = {t}/>} />


            </div>
          </div>


        </div>

      </Router> );
    }


  }

  const App = () => <MainApp/>


export default withAuthentication(App);
