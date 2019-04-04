import React, { Component } from 'react';
import { Link, withRouter, } from 'react-router-dom';


import { auth } from '../firebase';
import * as routes from '../constants/routes';

const INITIAL_STATE = {
  username: '',
  email: '',
  passwordOne: '',
  passwordTwo: '',
  error: null,
};
const byPropKey = (propertyName, value) => () => ({
  [propertyName]: value,
});


const SignUpPage = ({ history }) =>
  <div>
    <div>
      <ol className="breadcrumb">
        <li className="breadcrumb-item">
          <Link to="signup">Sign Up</Link>
        </li>
      </ol>

      <div className="card mb-3">
        <div className="card-header">
          New Account Details
        </div>
        <div className="card-body">
          <SignUpForm history={history} />
        </div>
      </div>
    </div>
  </div>

class SignUpForm extends Component {
  constructor(props) {
    super(props);
    this.state = { ...INITIAL_STATE };
  }

  onSubmit = (event) => {
    const {
      username,
      email,
      passwordOne,
    } = this.state;

    const {
      history,
    } = this.props;

    auth.doCreateUserWithEmailAndPassword(email, passwordOne)
      .then(authUser => {
        this.setState(() => ({ ...INITIAL_STATE }));
        history.push(routes.LOCATIONS);
      })
      .catch(error => {
        this.setState(byPropKey('error', error));
      });

    event.preventDefault();
  }

  render() {
    const {
      username,
      email,
      passwordOne,
      passwordTwo,
      error,
    } = this.state;

    const isInvalid =
      passwordOne !== passwordTwo ||
      passwordOne === '' ||
      email === '' ||
      username === '';


    var labelStyle = {

        /* To make sure that all labels have the same size and are properly aligned */
        display: 'inline-block',
        width: '150px',
        'text-align': 'right'


    
    };


    return (
      <form onSubmit={this.onSubmit}>
        <div>
          <label style = {labelStyle} for="name">Full name&nbsp;</label>

          <input
            value={username}
            onChange={event => this.setState(byPropKey('username', event.target.value))}
            type="text"
            placeholder="Full Name"
          />
        </div>


        <div>
          <label style = {labelStyle} for="name">E-mail address&nbsp;</label>
          <input
            value={email}
            onChange={event => this.setState(byPropKey('email', event.target.value))}
            type="text"
            placeholder="Email Address"
          />
        </div>

        <div>
          <label style = {labelStyle} for="name">Password&nbsp;</label>
          <input
            value={passwordOne}
            onChange={event => this.setState(byPropKey('passwordOne', event.target.value))}
            type="password"
            placeholder="Password"
          />
        </div>

        <div>
          <label style = {labelStyle} for="name">Re-enter password&nbsp;</label>
          <input
            value={passwordTwo}
            onChange={event => this.setState(byPropKey('passwordTwo', event.target.value))}
            type="password"
            placeholder="Confirm Password"
          />

        </div>

        <div className="btn-group" role="group">
          <button className="btn btn-primary" type="submit" disabled={isInvalid}>
            Sign Up
        </button>
        </div>

        {error && <p>{error.message}</p>}
      </form>
    );
  }
}

const SignUpLink = () =>
  <p>
    Don't have an account?
  {' '}
    <Link to={routes.SIGN_UP}>Sign Up</Link>
  </p>

export default withRouter(SignUpPage);

export {
  SignUpForm,
  SignUpLink,
};
