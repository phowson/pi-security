import React from 'react';
import firebase from '../firebase/firebase.js';
import {Route, Link,  withRouter } from 'react-router-dom';
import * as routes from '../constants/routes';
class LocationList extends React.Component {

  constructor(params) {
    super();
    this.getLocations = params['getLocations'];
    this.locationHolder = params['locationHolder']
    this.onLinkClick = this.onLinkClick.bind(this);
  }



  onLinkClick(event, itemId) {
    console.log(itemId);
    this.locationHolder.setLocation(itemId);

  }



  render() {
    let l = this.getLocations();

    console.log("Current location holder : ")
    console.log(this.locationHolder);

    return (
      <table>
        <thead>
          <tr>
            <th>
              Location Name
            </th>
          </tr>
        </thead>
        <tbody>
          {

            l?
            l.map((item) => {
              return (
                <tr  key={item.key}>
                  <td key={item.key}>


                    <Link to={routes.STATUS} onClick={(e) => this.onLinkClick(e, item.id)}>{item.id}</Link>

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


class LocationsPage extends React.Component {

  constructor(props) {
    super(props);
    this.locationHolder = props['locationHolder'];

    this.state = {
      locations: [ ],
    };
  }

  componentWillUnmount() {
    this._mounted = false;
  }


  componentDidMount() {
    this._mounted = true;
    const c = this;
    const itemsRef = firebase.database().ref('locations');
    itemsRef.once('value', (snapshot) => {

      if (c._mounted) {

        var locations = [];
        snapshot.forEach( (item) => {
          locations.push({
            key: item.key,
            id: item.key,

          });
          return false;
        });


        this.setState({
          locations: locations
        });

      }
    });
  }

  render() {

    const t  = this;
    return (
      <div>
        <ol className="breadcrumb">
          <li className="breadcrumb-item">
            <Link to="locations">Locations</Link>
          </li>
        </ol>
        <div className="card mb-3">
          <div className="card-header">
            <i className="fa fa-table"></i>Monitored Locations</div>
            <div className="card-body">
              <div className="table-responsive">
                <LocationList getLocations={ () => t.state["locations"] } locationHolder={t.locationHolder}/>
              </div>
            </div>
          </div>
        </div>

        );

    }

  }


  export default LocationsPage;
