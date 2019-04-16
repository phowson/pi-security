import React from 'react';
import { withRouter, Link } from 'react-router-dom';
import AuthUserContext from './AuthUserContext';
import { PasswordForgetForm } from './PasswordForget';
import PasswordChangeForm from './PasswordChange';
const AccountPage = () =>
  <AuthUserContext.Consumer>
    {authUser =>

      <div>
        <ol className="breadcrumb">
          <li className="breadcrumb-item">
            <Link to="account">Account</Link>
          </li>
        </ol>

        <div className="card mb-3">
          <div className="card-header">
            Account details
        </div>
          <div className="card-body">
            <div className="lead">Account:   {authUser ? authUser.email : ""}</div>

            <PasswordChangeForm />
          </div>
        </div>
      </div>

    }
  </AuthUserContext.Consumer>

/*


    <div>
      <h1>Account:   {authUser ? authUser.email: ""}</h1>
      <PasswordForgetForm />
      <PasswordChangeForm />
    </div>
*/


export default AccountPage;
