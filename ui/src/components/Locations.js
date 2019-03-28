import React from 'react';
import firebase from '../firebase/firebase.js';
import {Route, Link,  withRouter } from 'react-router-dom';
import * as routes from '../constants/routes';

class OpenButton extends React.Component {
  constructor(props) {
    super(props);
    this.doLink = this.doLink.bind(this);
    this.history = props['history'];
    this.locationHolder = props['locationHolder'];
    this.itemId = props['itemId'];
  } 

  render() {
    const {
      history,
    } = this.props;

    return <button
      className="btn btn-primary"
      history ={history}
      onClick={this.doLink}
      >
      Open
    </button>
  }

  doLink(event) {
    this.locationHolder.setLocation(this.itemId);
    this.history.push(routes.STATUS);
  }

}


class LocationList extends React.Component {

  constructor(params) {
    super(params);
    this.history = params['history'];
    this.getLocations = params['getLocations'];
    this.locationHolder = params['locationHolder']
    this.onLinkClick = this.onLinkClick.bind(this);
  }



  onLinkClick(event, itemId) {
    this.locationHolder.setLocation(itemId);
  }



  render() {
    let l = this.getLocations();

    return (
      <table
      className="table-striped table-hover table-bordered table-condensed"
        cellPadding="5px"
      >
        <thead>
          <tr>
            <th>
              Location Name
            </th>
            <th>
              Description
            </th>
            <th></th>
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

                  <td>
                    {item.description}


                  </td>

                  <td>
                  <div className="btn-group" role="group">
                    <OpenButton history = {this.history} locationHolder={this.locationHolder} itemId={item.id} />
                  </div>

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
    const itemsRef = firebase.database().ref('allLocations');
    itemsRef.once('value', (snapshot) => {

      if (c._mounted) {

        var locations = [];
        
        snapshot.forEach( (item) => {
          locations.push({
            key: item.key,
            id: item.child("name").val(),
            description: item.child("description").val(),

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
                <LocationList 
                history={t.props.history}
                getLocations={ () => t.state["locations"] } locationHolder={t.locationHolder}/>
              </div>
            </div>
          </div>
        </div>

        );

    }

  }


  export default withRouter(LocationsPage);
