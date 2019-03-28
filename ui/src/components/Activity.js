import React from 'react';
import { Route, Link, withRouter } from 'react-router-dom';
import firebase from '../firebase/firebase.js';
import * as helpers from '../helpers/datehelpers.js';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'



class RecentEventsList extends React.Component {

  constructor(params) {
    super();
    this.getRecentEvents = params['getRecentEvents'];
    this.locationHolder = params['locationHolder']
  }


  render() {
    let l = this.getRecentEvents();
    let c = this;

    return (
      <table
        className="table-striped table-hover table-bordered table-condensed"
        cellPadding="5px"
      >
        <thead>
          <tr>
            <th>
              Time
            </th>

            <th>
              Device
            </th>
            <th>
              Event Description
            </th>
            <th>
              Can trigger alarm
            </th>
            <th>
              Device I/O pin
            </th>

            <th>
              Event Type
            </th>

            <th>
              Alert Type
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
                      {item.deviceId}
                    </td>

                    <td >
                      {item.label}
                    </td>

                    <td >

                      <div style={(item.notify) ? { color: "red" } : {}} >
                        {item.notify ? (<FontAwesomeIcon icon="check" />) : ""}

                      </div>
                    </td>
                    <td >
                      {item.ioPin >0 ? item.ioPin  : ""}
                    </td>
                    <td >
                      {item.eventType}
                    </td>


                    <td >
                      {item.alertType}
                    </td>

                  </tr>
                )
              })
              : ""

          }
        </tbody>
      </table>
    );


  }

}



class ActivityPage extends React.Component {

  constructor(props) {
    super(props);

    this.locationHolder = props['locationHolder'];

    this.limitTo = 10;
    this.offset = 0;
    this.knownMax = 0;

    this.state = {
      events: [],
    };

  }


  queryRecentEvents(maxEventSeq) {

    maxEventSeq = maxEventSeq+1;
    this.knownMax = maxEventSeq;
    var c = this;
    var maxDisp = maxEventSeq + this.offset;
    var minDisp = maxEventSeq + this.offset + this.limitTo;

    const query2 = firebase.database().ref('locations/' + this.locationHolder.getLocation() + "/events").orderByChild("sequenceId").startAt(maxDisp).endAt(minDisp);
    query2.on("value", function (snapshot) {
      if (c._mounted) {


        var events = [];


        snapshot.forEach(function (childSnapshot) {
          events.push({
            key: childSnapshot.key,
            id: childSnapshot.key,
            time: helpers.convertTS(childSnapshot.child("timestamp").val()),
            eventType: childSnapshot.child("type").val(),
            deviceId: childSnapshot.child("deviceId").val(),
            label: childSnapshot.child("label").val(),
            notify: childSnapshot.child("notify").val(),
            ioPin: childSnapshot.child("gpioPin").val(),
            alertType: childSnapshot.child("alertType").val(),

          });




          return false;

        });
        c.setState({ events: events, minDisp : -minDisp, maxDisp: -maxDisp });        
      }
    });
  }

  runQuery() {
    const c = this;
    const query3 = firebase.database().ref('locations/' + this.locationHolder.getLocation() + "/eventsSequence").on("value",
      function (snapshot) {
        c.queryRecentEvents(snapshot.val());

      }

    );

  }


  componentDidMount() {
    this._mounted = true;

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

      if (t.offset + t.knownMax + t.limitTo<0) {
        t.offset = t.offset + t.limitTo;
      }
      t.runQuery();
    };

  }

  render() {
    const loc = this.locationHolder.getLocation();
    const t = this;

    

    return (

      <div>
        <ol className="breadcrumb">
          <li className="breadcrumb-item">
            <Link to="locations">Locations</Link>
          </li>
          <li className="breadcrumb-item">
            <Link to="status">{loc}</Link>
          </li>
          <li className="breadcrumb-item active">Acitvity</li>
        </ol>


        <div className="card mb-3  ">
          <div className="card-header">
            <i className="fa fa-exclamation-circle"></i>&nbsp;Events</div>
          <div className="card-body ">
            <div className="table-responsive nohscroll">
              <RecentEventsList getRecentEvents={() => t.state["events"]} locationHolder={t.locationHolder} />
              <div className="btn-group" role="group">
                <button type="button" className="btn btn-secondary" onClick={t.onLeftClicked()} > <i className="fa fa-chevron-left" /></button>
                <button type="button" className="btn btn-secondary" onClick={t.onRightClicked()} ><i className="fa fa-chevron-right" /></button>

              </div>
              <div>
                Events {t.state['minDisp'] } to {t.state['maxDisp']}

              </div>

            </div>
          </div>
        </div>
      </div>
    );
  }
}
export default ActivityPage;
