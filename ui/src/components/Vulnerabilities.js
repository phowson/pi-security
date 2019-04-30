
import React from 'react';
import { Route, Link, withRouter } from 'react-router-dom';
import firebase from '../firebase/firebase.js';
import * as helpers from '../helpers/datehelpers.js';
import * as qpq from '../helpers/QuietPeriodQuery.js'

class VulnerabilitiesPage extends React.Component {
  constructor(props) {
    super(props);

    this._mounted = false;
    this.locationHolder = props['locationHolder'];
    this.limitTo = 100;
    this.offset = 0;
    this.knownMax = 0;
    this.listKey = 0;

    this.state = {
      perDayOfWeekTable: new Map(),
      quietPeriods: [],
      allDaysPeriods : []
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

      (periods, allDaysPeriods) => {

        if (c._mounted) {
          c.setState({
            allDaysPeriods: allDaysPeriods,
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


render() {


  var recommendedArming = [];
  var quietPeriods = this.state['quietPeriods'];
  var allDaysPeriods = this.state['allDaysPeriods'];
  var k = 0;

  quietPeriods.forEach( (x) => {

    if (x.durationHours > 1) {

      recommendedArming.push((
        <li key={k++}>
          <div key={k++}>
            Between &nbsp;
              <font style={{ "fontWeight": "bold" }}>
              {helpers.dowNames[x.dayOfWeekStart]} {helpers.twoDigit(x.hourOfDayStart)}:00 hours
              </font>
            &nbsp;and&nbsp;
              <font style={{ "fontWeight": "bold" }}>
              {helpers.dowNames[x.dayOfWeekEnd]} {helpers.twoDigit(x.hourOfDayEnd)}:00 hours
              </font>


            </div>
        </li>

      ));

    }

    



  });

  recommendedArming.push(<div><hr></hr> <div>Or every day</div></div>);

  allDaysPeriods.forEach( (x) => {

    if (x.durationHours > 1) {

      recommendedArming.push((
        <li key={k++}>
          <div key={k++}>
            Between &nbsp;
              <font style={{ "fontWeight": "bold" }}>
              {helpers.twoDigit(x.hourOfDayStart)}:00 hours
              </font>
            &nbsp;and&nbsp;
              <font style={{ "fontWeight": "bold" }}>
              {helpers.twoDigit(x.hourOfDayEnd)}:00 hours
              </font>


            </div>
      </li> ));
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
      <li className="breadcrumb-item active">Vulnerabilities</li>
    </ol>

    <div className="card mb-3">
      <div className="card-header">
        <i className="fa fa-calendar"></i>&nbsp;Vulnerable times for {loc}</div>
      <div className="card-body ">

        <div className="lead">The site {loc} is most vulnerable during the following periods</div>
        <ul>
          {recommendedArming}
        </ul>

        

      </div>
    </div>




  </div>;

}

}
export default VulnerabilitiesPage;
