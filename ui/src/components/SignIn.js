import React, { Component } from 'react';
import { withRouter, Link } from 'react-router-dom';

import { SignUpLink } from './SignUp';
import { auth } from '../firebase';
import * as routes from '../constants/routes';
import { PasswordForgetLink } from './PasswordForget';


const SignInPage = ({ history, locationHolder }) =>
  <div>
    <div>
      <ol className="breadcrumb">
        <li className="breadcrumb-item">
          <Link to="signin">Sign In</Link>
        </li>
      </ol>

      <div className="card mb-3">
        <div className="card-header">
          Log in
        </div>
        <div className="card-body">
          <SignInForm history={history} locationHolder={locationHolder} />
          <PasswordForgetLink />
          <SignUpLink />
        </div>
      </div>
    </div>
  </div>


const byPropKey = (propertyName, value) => () => ({
  [propertyName]: value,
});



const INITIAL_STATE = {
  email: '',
  password: '',
  error: null,
};

class SignInForm extends Component {
  constructor(props) {
    super(props);
    this.locationHolder = props['locationHolder'];
    this.state = { ...INITIAL_STATE };
    this._mounted = false;
  }

  componentDidMount() {
    this._mounted = true;
  }

  componentWillUnmount() {
    this._mounted = false;
  }

  onSubmit = (event) => {

    this.locationHolder.setLocation(null);
    const {
      email,
      password,
    } = this.state;

    const {
      history,
    } = this.props;

    const t = this;

    auth.doSignInWithEmailAndPassword(email, password)
      .then(() => {
        if (t._mounted)
          this.setState(() => ({ ...INITIAL_STATE }));
        history.push(routes.LOCATIONS);
      })
      .catch(error => {
        if (t._mounted)
          this.setState(byPropKey('error', error));
      });

    event.preventDefault();
  }

  render() {
    const {
      email,
      password,
      error,
    } = this.state;

    const isInvalid =
      password === '' ||
      email === '';

    return (


      <form onSubmit={this.onSubmit}>
        <input
          value={email}
          onChange={event => { if (this._mounted) this.setState(byPropKey('email', event.target.value)) }}
          type="text"
          placeholder="Email Address"
        />
        <input
          value={password}
          onChange={event => {
            if (this._mounted)
              this.setState(byPropKey('password', event.target.value))
          }}
          type="password"
          placeholder="Password"
        />

        <div className="btn-group" role="group">
          <button className="btn btn-primary" disabled={isInvalid} type="submit">
            Sign In
        </button>
        </div>

        {error && <p>{error.message}</p>}
      </form>

    );
  }
}

export default withRouter(SignInPage);

export {
  SignInForm,
};
