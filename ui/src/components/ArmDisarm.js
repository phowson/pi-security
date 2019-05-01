
import React from 'react';
import { Route, Link, withRouter } from 'react-router-dom';
import firebase from '../firebase/firebase.js';
import * as helpers from '../helpers/datehelpers.js';
import * as qpq from '../helpers/QuietPeriodQuery.js'

class ArmDisarmPage extends React.Component {
  constructor(props) {
    super(props);

    this._mounted = false;
    this.locationHolder = props['locationHolder'];
    this.limitTo = 1000;
    this.offset = 0;
    this.knownMax = 0;
    this.listKey = 0;

    this.state = {
      perDayOfWeekTable: new Map(),
      quietPeriods: []
    };
  }



  queryRecentEvents(maxEventSeq) {

    maxEventSeq = maxEventSeq + 1;
    this.knownMax = maxEventSeq;
    var c = this;
    var maxDisp = maxEventSeq + this.offset;
    var minDisp = maxEventSeq + this.offset + this.limitTo;

    const query2 = firebase.database().ref('locations/' + this.locationHolder.getLocation() + "/events").orderByChild("sequenceId").startAt(maxDisp).endAt(minDisp);
    new qpq.QuietPeriodQuery().getQuietPeriods(query2,

      (periods) => {

        if (c._mounted) {
          c.setState({
            quietPeriods: periods,
            minDisp: -minDisp, maxDisp: -maxDisp
          });



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


componentWillUnmount() {
  this._mounted = false;
}


onApplyClicked(evt) {


}

onAddNewRule(evt) {

}


generateDaysOfWeek() {
  const daysOfWeek = [
    (<option key={++this.listKey} value="0">Sunday</option>),
    (<option key={++this.listKey} value="1">Monday</option>),
    (<option key={++this.listKey} value="2">Tuesday</option>),
    (<option key={++this.listKey} value="3">Wednesday</option>),
    (<option key={++this.listKey} value="4">Thursday</option>),
    (<option key={++this.listKey} value="5">Friday</option>),
    (<option key={++this.listKey} value="6">Saturday</option>),
  ];
  return daysOfWeek;

}


generateHoursList() {

  var hoursList = [

  ];


  for (var i = 0; i < 24; ++i) {

    hoursList.push(<option key={++this.listKey} value={i}>{helpers.twoDigit(i)}</option>);
  }


  return hoursList;

}

generateMinutesList() {
  var minutesList = [

  ];
  for (var i = 15; i < 60; i += 15) {

    minutesList.push(<option key={++this.listKey} value={i}>{helpers.twoDigit(i)}</option>);
  }

  return minutesList;

}


render() {


  var recommendedArming = [];
  var quietPeriods = this.state['quietPeriods'];
  var k = 0;

  quietPeriods.forEach(x => {

    if (x.durationHours > 2) {

      recommendedArming.push((
        <li key={k++}>
          <div key={k++}>
            Automatically arm between &nbsp;
              <font style={{ "fontWeight": "bold" }}>
              {helpers.dowNames[x.dayOfWeekStart]} {helpers.twoDigit(x.hourOfDayStart)}:00 hours
              </font>
            &nbsp;and&nbsp;
              <font style={{ "fontWeight": "bold" }}>
              {helpers.dowNames[x.dayOfWeekEnd]} {helpers.twoDigit(x.hourOfDayEnd)}:00 hours
              </font>

            , if no activity on any sensors for 30 minutes.

            </div>
        </li>

      ));
    }



  });


  const t = this;
  const loc = this.locationHolder.getLocation();




  return <div>
    <ol className="breadcrumb">
      <li className="breadcrumb-item">
        <Link to="locations">Locations</Link>
      </li>
      <li className="breadcrumb-item">
        <Link to="status">{loc}</Link>
      </li>
      <li className="breadcrumb-item active">Automatic Arm / Disarm</li>
    </ol>

    <div className="card mb-3">
      <div className="card-header">
        <i className="fa fa-calendar"></i>&nbsp;Automatically recommended smart arming schedule</div>
      <div className="card-body ">

        <div className="lead">Recommended automatic alarm arming rules from analysis of data at {loc}</div>
        <ul>
          {recommendedArming}
        </ul>

        <div className="btn-group" role="group">
          <button type="button" className="btn btn-primary" onClick={t.onApplyClicked} > Apply </button>

        </div>


      </div>
    </div>


    <div className="card mb-3">
      <div className="card-header">
        <i className="fa fa-pencil"></i>&nbsp;Current arming rules</div>
      <div className="card-body ">

        <p>No rules currently defined</p>
        <hr />

        <p className="lead">New rule</p>
        <form onSubmit={this.onAddNewRule}>
          <div>
            <label>Automatically arm between&nbsp;</label>

            <select
              key="dateSelect1">
              {t.generateDaysOfWeek()}
            </select>
            <label>&nbsp;at&nbsp;</label>
            <select
              key="hourSelect1">
              {t.generateHoursList()}
            </select>

            <label>:00 and&nbsp;</label>
            <select
              key="dateSelect2">
              {t.generateDaysOfWeek()}
            </select>
            <label>&nbsp;at&nbsp;</label>
            <select
              key="hourSelect2">
              {t.generateHoursList()}
            </select>
            <label>:00 if no activity seen for&nbsp;</label>
            <select
              key="minuteSelect">
              {t.generateMinutesList()}
            </select>

            <div style={{ width: 5, height: "auto", display: "inline-block" }} />
            <div className="btn-group" role="group">
              <button className="btn btn-primary" type="submit" >
                Add rule
                </button>
            </div>

          </div>
        </form>



      </div>
    </div>


    <div className="card  mb-3">
      <div className="card-header">
        <i className="fa fa-mobile"></i>&nbsp;Mobile phone proximity automatic arm / disarm</div>
      <div className="card-body ">


      </div>
    </div>



  </div>;

}

}
export default ArmDisarmPage;
