import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import { auth } from '../firebase';
import { Button } from 'reactstrap';

import * as routes from '../constants/routes';
class SignOutButtonInner extends Component {
  constructor(props) {
    super(props);
    console.log(props);
  }

  render() {
    const {
      history,
    } = this.props;

    return <Button
      color="primary"
      history ={history}
      onClick={this.doSignOut}
      >
      Sign Out
    </Button>
  }

  doSignOut(event) {
    auth.doSignOut();
    this.history.push(routes.LANDING);
  }

}


const SignOutButton = withRouter(({ history }) => <SignOutButtonInner history={history}/> );

export default SignOutButton;
