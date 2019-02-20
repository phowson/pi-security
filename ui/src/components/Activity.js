import React from 'react';
import BootstrapTable from 'react-bootstrap-table-next';
import paginationFactory from 'react-bootstrap-table2-paginator';
import LocationContext from './LocationContext';
import {Route, Link,  withRouter } from 'react-router-dom';
import firebase from '../firebase/firebase.js';


const columns = [
  { dataField: "timestamp", text : "Date / Time" },
  { dataField: "type", text : "Event Type" },
  { dataField: "label",text : "Label" },
  { dataField: "comment",text : "Comment" },
  { dataField: "deviceId",text : "Device ID" },
  { dataField: "gpioPin",text : "GPIO Pin" },
  { dataField: "notify",text : "Caused Alert" },
  { dataField: "alertType",text : "Alert Type" },
];


const RemotePagination = ({ data, page, sizePerPage, onTableChange, totalSize }) => (
  <div>
    <BootstrapTable
      remote
      striped
      hover
      condensed
      keyField="id"
      noDataIndication="No activity recorded"
      data={ data }
      columns={ columns }
      pagination={

        paginationFactory({
          "page": page,
          "sizePerPage": sizePerPage,
          "totalSize" : totalSize,
          "sizePerPageList": [{
            text: '5', value: 5
          }, {
            text: '10', value: 10
          }, {
            text: '50', value:50
          }]


        }) }
        onTableChange={ onTableChange }
        />
    </div>
  );

class ActivityPage extends React.Component {

  constructor(props) {
    super(props);
    const  initialSize = 5;
    this.locationHolder = props['locationHolder'];
    this.state = {
      page: 1,
      data: [],
      sizePerPage:  initialSize,
      totalSize: 0
    };


    const func = (events, totalSize)=> {
      const newState = {
        page:1,
        data:events ,
        sizePerPage:initialSize,
        totalSize : totalSize
      };

      this.state = newState;
      this.setState(() => { return newState; });
    }
    this.getevents(0, initialSize, func);

  }

  getevents(startIdx, endIdx , callback){

    var results = [];

    console.log("Start index : "+ startIdx);
    console.log("End index : "+ endIdx);


    const itemsRef = firebase.database().ref('locations/' + this.locationHolder.getLocation() +"/events");
    itemsRef.orderByChild("rtimestamp").
    limitToFirst(endIdx ).
    on('value', (snapshot) => {
      snapshot.forEach( (item) => {

        var v1 = item.val();
        v1['id'] = item.key;

        if (startIdx==0) {
          results.push(v1);
        } else {
          startIdx--;
        }

      });
    });


    callback( results, 50 );
  }


  handleTableChange = (type, { page, sizePerPage }) => {
    const currentIndex = (page - 1) * sizePerPage;

    const func = (events, totalSize)=> {
      this.setState(() => ({
        page,
        data:events ,
        sizePerPage,
        totalSize : totalSize
      }))};
      this.getevents(currentIndex, currentIndex + sizePerPage, func);

    }

  render() {
    const { data, sizePerPage, page, totalSize } = this.state;
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
          <li className="breadcrumb-item active">Acitvity</li>
        </ol>


        <div className="card mb-3  ">
          <div className="card-header">
            <i className="fa fa-exclamation-circle"></i>&nbsp;Events</div>
            <div className="card-body ">
              <div className="table-responsive nohscroll">
                <RemotePagination
                  data={ data }
                  page={ page }
                  sizePerPage={ sizePerPage }
                  totalSize={ totalSize }
                  onTableChange={ this.handleTableChange }
                  />
                <br/>
                <br/>
                <br/>
                <br/>
              </div>
            </div>
          </div>
        </div>
      );
  }
}
export default ActivityPage;
