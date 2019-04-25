import React, { Component } from 'react';

import { auth } from '../firebase';

const byPropKey = (propertyName, value) => () => ({
  [propertyName]: value,
});

const INITIAL_STATE = {
  passwordOne: '',
  passwordTwo: '',
  error: null,
};

class PasswordChangeForm extends Component {
  constructor(props) {
    super(props);

    this.state = { ...INITIAL_STATE };
  }

  onSubmit = (event) => {
    const { passwordOne } = this.state;

    auth.doPasswordUpdate(passwordOne)
      .then(() => {
        this.setState(() => ({ ...INITIAL_STATE }));
      })
      .catch(error => {
        this.setState(byPropKey('error', error));
      });

    event.preventDefault();
  }

  render() {
    const {
      passwordOne,
      passwordTwo,
      error,
    } = this.state;

    const labelStyle = {

      /* To make sure that all labels have the same size and are properly aligned */
      display: 'inline-block',
      width: '250px',
      textAlign: 'right'



    };
    const isInvalid =
      passwordOne !== passwordTwo ||
      passwordOne === '';

    return (
      <form onSubmit={this.onSubmit}>

        <div>
          <label style={labelStyle} for="name">New Password&nbsp;</label>
          <input
            value={passwordOne}
            onChange={event => this.setState(byPropKey('passwordOne', event.target.value))}
            type="password"
            placeholder="New Password"
          />
        </div>

        <div>
          <label style={labelStyle} for="name">Re-enter New Password&nbsp;</label>
          <input
            value={passwordTwo}
            onChange={event => this.setState(byPropKey('passwordTwo', event.target.value))}
            type="password"
            placeholder="Confirm New Password"
          />
        </div>

        <div className="btn-group" role="group">
          <button className="btn btn-primary" disabled={isInvalid} type="submit">
            Reset My Password
        </button>
        </div>

        {error && <p>{error.message}</p>}
      </form>
    );
  }
}

export default PasswordChangeForm;
