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

import * as routes from '../constants/routes';
import withAuthentication from './withAuthentication';


const App = () =>
<Router>

  <div>
    <Navigation />

    <div className="content-wrapper">
      <div className="container-fluid">

        <Route exact path={routes.LANDING} component={() => <LandingPage />} />
        <Route exact path={routes.SIGN_UP} component={() => <SignUpPage />} />
        <Route exact path={routes.SIGN_IN} component={() => <SignInPage />} />
        <Route exact path={routes.PASSWORD_FORGET} component={() => <PasswordForgetPage />} />
        <Route exact path={routes.LOCATIONS} component={() => <LocationsPage />} />
        <Route exact path={routes.ACCOUNT} component={() => <AccountPage />} />
      </div>
    </div>
  </div>

</Router>


export default withAuthentication(App);
