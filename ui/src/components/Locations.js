import React from 'react';
import firebase from '../firebase/firebase.js';


class LocationList extends React.Component {

  constructor(params) {
    super();
    this.getLocations = params['getLocations'];

  }




  render() {
    let l = this.getLocations();
    console.log("In location list");
    console.log(l);
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
                  <td key={item.key}>{item.id}</td>
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

    this.state = {
      locations: [ ],
    };
  }

  componentWillUnmount() {
    this._mounted = false;
    console.log("Unmounting for some reason")
  }


  componentDidMount() {
    this._mounted = true;
    const c = this;
    const itemsRef = firebase.database().ref('locations');
    itemsRef.on('value', (snapshot) => {

      if (c._mounted) {

        let locations = [];
        snapshot.forEach( (item) => {
          locations.push({
            key: item.key,
            id: item.key
          });
          return true;
        });


        console.log("Setting state to the following");
        console.log(locations);
        this.setState({
          locations: locations
        });
        console.log("Expecting that to cause a repaint?")

      }
    });
  }

  render() {

    const t  = this;
    return (

      <div className="card mb-3">
        <div className="card-header">
          <i className="fa fa-table"></i>Monitored Locations</div>
          <div className="card-body">
            <div className="table-responsive">
              <LocationList getLocations={ () => t.state["locations"] }/>
            </div>
          </div>
        </div>

      );

    }

  }


  export default LocationsPage;
