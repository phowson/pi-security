
import React from 'react';
import firebase from '../firebase/firebase.js';
import * as helpers from '../helpers/datehelpers.js';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { Route, Link, withRouter } from 'react-router-dom';

class RecentCallsList extends React.Component {
  constructor(params) {
    super();
    this.getRecentCalls = params['getRecentCalls'];
    this.locationHolder = params['locationHolder']
  }

  render() {
    let l = this.getRecentCalls();
    let c = this;

    return (
      <table
        className="table-striped table-hover table-bordered table-condensed"
        cellPadding="2px"
      >
        <thead>
          <tr>
            <th>
              Time
            </th>
            <th>
              Number
            </th>
            <th>
              Comment
            </th>
            <th>
              Message
            </th>
            <th>
              Answered
            </th>
          </tr>

        </thead>
        <tbody>
          {

            l ?
              l.map((item) => {
                return (
                  <tr key={item.key}>
                    <td key={item.key}>
                      {item.time}
                    </td>


                    <td >
                      {item.number}
                    </td>

                    <td >
                      {item.label}
                    </td>

                    <td >
                      <div style={{ "maxWidth": 200 }} >
                        {item.message}
                      </div>
                    </td>

                    <td >
                      {item.answered ? (<FontAwesomeIcon icon="check" />) : ""}
                    </td>

                  </tr>
                )
              })
              : ""

          }
        </tbody>
      </table>);
  }

}

class CallLogPage extends React.Component {
  constructor(props) {
    super(props);


    this.locationHolder = props['locationHolder'];

    this.state = {
      calls: [],
    };

    this.offset = 0;
    this.limitTo = 20;
    this.knownMax = 0;


  }
  queryRecentCalls(maxEventSeq) {
    var c = this;
    const query2 = firebase.database().ref('/calls').orderByChild("sequenceId").
      startAt(maxEventSeq + this.offset).endAt(maxEventSeq + this.offset + this.limitTo);
    query2.on("value", function (snapshot) {
      if (c._mounted) {


        var calls = [];


        snapshot.forEach(function (childSnapshot) {
          calls.push({
            key: childSnapshot.key,
            id: childSnapshot.key,
            time: helpers.convertTS(childSnapshot.child("time").val()),
            answered: childSnapshot.child("answered").val(),
            label: childSnapshot.child("label").val(),
            message: childSnapshot.child("message").val(),
            number: childSnapshot.child("number").val(),

          });





          return false;

        });
        c.setState({ calls: calls });
      }
    });
  }


  runQuery() {
    const c = this;
    const query = firebase.database().ref('/callSequence').on("value",
    
    function (snapshot) {
      c.knownMax = snapshot.val();
      c.queryRecentCalls(snapshot.val());

    }

  );


  }


  componentDidMount() {
    this._mounted = true;
    const c = this;

    this.runQuery();
  }


  onLeftClicked() {
    const t = this;
    return (e) => {
      if (t.offset > 0) {
        t.offset = t.offset - t.limitTo;
      }
      t.runQuery();
    };

  }


  onRightClicked() {
    const t = this;
    return (e) => {

      if (t.offset + t.knownMax + t.limitTo < 0) {
        t.offset = t.offset + t.limitTo;
      }
      t.runQuery();
    };

  }
  render() {
    const t = this;
    const loc = this.locationHolder.getLocation();
    return (
      <div>
        <ol className="breadcrumb">
          <li className="breadcrumb-item">
            <Link to="locations">Locations</Link>
          </li>
          <li className="breadcrumb-item">
            <Link to="status">{loc}</Link>
          </li>
          <li className="breadcrumb-item active">Call Log</li>
        </ol>

        <div className="card mb-3">
          <div className="card-header">
            <i className="fa fa-phone"></i>&nbsp;Call Log for {loc}</div>
          <div className="card-body">

            <div className="table-responsive">
              <RecentCallsList getRecentCalls={() => t.state["calls"]} locationHolder={t.locationHolder} />
            </div>

            <div className="btn-group" role="group">
              <button type="button" className="btn btn-secondary" onClick={t.onLeftClicked()} > <i className="fa fa-chevron-left" /></button>
              <button type="button" className="btn btn-secondary" onClick={t.onRightClicked()} ><i className="fa fa-chevron-right" /></button>

            </div>
          </div>
        </div>
      </div>






    );

  }

}
export default CallLogPage;
