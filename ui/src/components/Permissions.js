
import React from 'react';
import { Route, Link, withRouter } from 'react-router-dom';
import firebase from '../firebase/firebase.js';
import * as helpers from '../helpers/datehelpers.js';


class PermissionsPage extends React.Component {

  constructor(props) {
    super(props);


    this.state = {
      roles: [],
      userRoles:[]
    };

    this._mounted = false;
    this.locationHolder = props['locationHolder'];
  }

  componentWillUnmount() {
    this._mounted = false;
  }


  componentDidMount() {
    this._mounted = true;
    const c = this;


    const query = firebase.database().ref('locations/' + this.locationHolder.getLocation() + "/roles").orderByKey();
    var t = this;
    query.on("value", function (snapshot) {
      if (c._mounted) {


        var roles = [];


        snapshot.forEach(function (childSnapshot) {
          roles.push({
            key: childSnapshot.key,
            id: childSnapshot.key,
            roleName: childSnapshot.child("roleName").val(),
            arm: childSnapshot.child("canArm").val(),
            changeSettings: childSnapshot.child("canChangeSettings").val(),
            disarm: childSnapshot.child("canDisarm").val(),
            resetAlarm: childSnapshot.child("canResetAlarm").val(),
            triggerAlarm: childSnapshot.child("canTriggerAlarm").val(),
          });




          return false;

        });
        c.setState({ roles: roles });
      }
    });

    const query2 = firebase.database().ref('locations/' + this.locationHolder.getLocation() + "/roleUserAssignment").orderByKey();
    var t = this;
    query2.on("value", function (snapshot) {
      if (c._mounted) {


        var userRoles = [];


        snapshot.forEach(function (childSnapshot) {
          userRoles.push({
            key: childSnapshot.key,
            id: childSnapshot.key,
            roleName: childSnapshot.child("roleName").val(),
            arm: childSnapshot.child("canArm").val(),
            changeSettings: childSnapshot.child("canChangeSettings").val(),
            disarm: childSnapshot.child("canDisarm").val(),
            resetAlarm: childSnapshot.child("canResetAlarm").val(),
            triggerAlarm: childSnapshot.child("canTriggerAlarm").val(),
          });




          return false;

        });
        c.setState({ userRoles: userRoles });
      }
    });



  }


  onUpdateClicked(dbKey) {
    return evt => {

    };
  }

  onDeleteClicked(dbKey) {
    return evt => {

    };
  }

  onAddClicked(evt) {


  }


  renderCheckBox(id, name, b) {
    return b ? <input type="checkbox" name={id + "_" + name} defaultChecked />
      : <input type="checkbox" name={id + "_" + name} />
  }

  render() {
    const loc = this.locationHolder.getLocation();

    const c = this;
    var roles = this.state['roles'];

    return <div>
      <ol className="breadcrumb">
        <li className="breadcrumb-item">
          <Link to="locations">Locations</Link>
        </li>
        <li className="breadcrumb-item">
          <Link to="status">{loc}</Link>
        </li>
        <li className="breadcrumb-item active">Permissions</li>
      </ol>


      <div className="card mb-3">
        <div className="card-header">
          <i className="fa fa-user"></i>&nbsp;User Roles</div>
        <div className="card-body ">

          <div className="lead">Existing roles</div>
          <table
            className="table-striped table-hover table-bordered"
            cellPadding="5px"
          >
            <thead>
              <tr>
                <th>Role Name</th>
                <th>Arm</th>
                <th>Disarm</th>
                <th>Reset</th>
                <th>Trigger bell</th>
                <th>Change Settings</th>
                <th></th>

              </tr>
            </thead>
            <tbody>


              {

                roles ?
                  roles.map((item) => {
                    return (
                      <tr key={item.key}>
                        <td key={item.key}>
                          {item.roleName}<div style={{ width: 15, height: "auto", display: "inline-block" }} />
                        </td>

                        <td>
                          {c.renderCheckBox(item.key, "arm", item.arm)}
                        </td>

                        <td>
                          {c.renderCheckBox(item.key, "disarm", item.disarm)}
                        </td>

                        <td>
                          {c.renderCheckBox(item.key, "resetAlarm", item.resetAlarm)}
                        </td>


                        <td>
                          {c.renderCheckBox(item.key, "triggerAlarm", item.triggerAlarm)}
                        </td>


                        <td>
                          {c.renderCheckBox(item.key, "changeSettings", item.changeSettings)}
                        </td>

                        <td>
                          <div className="btn-group" role="group">
                            <button className="btn btn-primary" onClick={c.onUpdateClicked(item.key)}>Update</button>
                            <div style={{ width: 5, height: "auto", display: "inline-block" }} />
                            <button className="btn btn-primary" onClick={c.onDeleteClicked(item.key)}>Delete</button>
                          </div>
                        </td>


                      </tr>
                    )
                  })
                  : ""
              }
            </tbody>
          </table>
          <hr></hr>

          <div className="lead">Add new role</div>
          <table
            className="table-striped table-hover table-bordered"
            cellPadding="5px"
          >
            <thead>
              <tr>
                <th>Role Name</th>
                <th>Arm</th>
                <th>Disarm</th>
                <th>Reset</th>
                <th>Trigger bell</th>
                <th>Change Settings</th>
                <th></th>

              </tr>
            </thead>
            <tbody>
              <tr >
                <td>
                <input type="text"/>
                </td>

                <td>
                  {c.renderCheckBox("new", "arm", false)}
                </td>

                <td>
                  {c.renderCheckBox("new", "disarm", false)}
                </td>

                <td>
                  {c.renderCheckBox("new", "resetAlarm", false)}
                </td>


                <td>
                  {c.renderCheckBox("new", "triggerAlarm", false)}
                </td>


                <td>
                  {c.renderCheckBox("new", "changeSettings", false)}
                </td>

                <td>
                  <div className="btn-group" role="group">
                    <button className="btn btn-primary" onClick={c.onAddClicked}>Add</button>
                  
                  </div>
                </td>


              </tr>
            </tbody>
          </table>


        </div>
      </div>


      <div className="card mb-3">
        <div className="card-header">
          <i className="fa fa-users"></i>&nbsp;Role assignment</div>
        <div className="card-body ">
          <table
            className="table-striped table-hover table-bordered"
            cellPadding="5px"
          >
            <thead>
              <tr>
                <th>User</th>
                <th>Role</th>
                <th>Time restrictions</th>
                <th></th>

              </tr>
            </thead>
            <tbody>

            </tbody>
          </table>
        </div>
      </div>


    </div>;

  }

}
export default PermissionsPage;
