import React from 'react';
import BootstrapTable from 'react-bootstrap-table-next';
import paginationFactory from 'react-bootstrap-table2-paginator';


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

const events = [
  { id : "-L9KRD-iPUsslWrBjDFF",
    "alertType" : "NONE",
    "comment" : "Monitoring reconfigured",
    "deviceId" : "alarm-test",
    "gpioPin" : -1,
    "label" : "Monitoring reconfigured",
    "notify" : false,
    "timestamp" : 1522925630218,
    "type" : "CONFIG_CHANGED"
  },
  { id : "-L9KRD-qVeagNyBlABXp",
    "alertType" : "NONE",
    "comment" : "Alarm bell reconfigured",
    "deviceId" : "alarm-test",
    "gpioPin" : -1,
    "label" : "Alarm bell reconfigured",
    "notify" : false,
    "timestamp" : 1522925630226,
    "type" : "CONFIG_CHANGED"
  },
  { id : "-L9KRD-rClqjselQlsAf",
    "alertType" : "NONE",
    "comment" : "Auto arm reconfigured",
    "deviceId" : "alarm-test",
    "gpioPin" : -1,
    "label" : "Auto arm reconfigured",
    "notify" : false,
    "timestamp" : 1522925630227,
    "type" : "CONFIG_CHANGED"
  },
  { id : "-L9KRDmBjaf68E5n2kqL" ,
    "alertType" : "NONE",
    "comment" : "System startup at timestamp : 1522925628556",
    "deviceId" : "alarm-test",
    "gpioPin" : -1,
    "label" : "System start",
    "notify" : false,
    "timestamp" : 1522925628555,
    "type" : "SYSTEM_START"
  },
  { id : "-L9LGYyb8czMqI0Vy8Ux" ,
    "alertType" : "NONE",
    "comment" : "Armed manually",
    "deviceId" : "alarm-test",
    "gpioPin" : -1,
    "label" : "System manually armed",
    "notify" : false,
    "timestamp" : 1522939613827,
    "type" : "SYSTEM_MANUAL_ARMED"
  },
  { id : "-L9LGySLtv84eISk0jKQ" ,
    "alertType" : "DELAYED_ALERT",
    "comment" : "Activity detected on pin 2",
    "deviceId" : "alarm-test",
    "gpioPin" : 2,
    "label" : "Device on GPIO #2",
    "notify" : true,
    "timestamp" : 1522939722290,
    "type" : "ACTIVITY"
  },
  { id : "-L9LGyhRobunow_PcD47" ,
    "alertType" : "DELAYED_ALERT",
    "comment" : "Activity detected on pin 3",
    "deviceId" : "alarm-test",
    "gpioPin" : 3,
    "label" : "Device on GPIO #3",
    "notify" : true,
    "timestamp" : 1522939723320,
    "type" : "ACTIVITY"
  },
  { id : "-L9LI6iZTiXvDGBZx7F6" ,
    "alertType" : "NONE",
    "comment" : "Alarm reset manually",
    "deviceId" : "alarm-test",
    "gpioPin" : -1,
    "label" : "Alarm reset manually",
    "notify" : false,
    "timestamp" : 1522940022400,
    "type" : "ALARMRESET"
  },
  { id : "-L9LI8oov74d_3zyKm0p" ,
    "alertType" : "DELAYED_ALERT",
    "comment" : "Activity detected on pin 2",
    "deviceId" : "alarm-test",
    "gpioPin" : 2,
    "label" : "Device on GPIO #2",
    "notify" : true,
    "timestamp" : 1522940030992,
    "type" : "ACTIVITY"
  },
  { id : "-L9LI8zBGvCqDaBdRdF5" ,
    "alertType" : "DELAYED_ALERT",
    "comment" : "Activity detected on pin 3",
    "deviceId" : "alarm-test",
    "gpioPin" : 3,
    "label" : "Device on GPIO #3",
    "notify" : true,
    "timestamp" : 1522940031656,
    "type" : "ACTIVITY"
  },
  { id : "-L9LJGvvR_pGF5ksHayn" ,
    "alertType" : "NONE",
    "comment" : "Monitoring reconfigured",
    "deviceId" : "alarm-test",
    "gpioPin" : -1,
    "label" : "Monitoring reconfigured",
    "notify" : false,
    "timestamp" : 1522940326595,
    "type" : "CONFIG_CHANGED"
  },
  { id : "-L9LJGw7YsRGhoHz3YNh" ,
    "alertType" : "NONE",
    "comment" : "Alarm bell reconfigured",
    "deviceId" : "alarm-test",
    "gpioPin" : -1,
    "label" : "Alarm bell reconfigured",
    "notify" : false,
    "timestamp" : 1522940326608,
    "type" : "CONFIG_CHANGED"
  },
  { id : "-L9LJGwBVYE1_-NWaOa1",
    "alertType" : "NONE",
    "comment" : "Auto arm reconfigured",
    "deviceId" : "alarm-test",
    "gpioPin" : -1,
    "label" : "Auto arm reconfigured",
    "notify" : false,
    "timestamp" : 1522940326612,
    "type" : "CONFIG_CHANGED"
  },
  { id : "-L9LJHlg8JQjjDfUj6Hc" ,
    "alertType" : "NONE",
    "comment" : "System startup at timestamp : 1522940324972",
    "deviceId" : "alarm-test",
    "gpioPin" : -1,
    "label" : "System start",
    "notify" : false,
    "timestamp" : 1522940324971,
    "type" : "SYSTEM_START"
  },
  { id : "-L9LK3gX4zdSOjQUywKe" ,
    "alertType" : "NONE",
    "comment" : "Monitoring reconfigured",
    "deviceId" : "alarm-test",
    "gpioPin" : -1,
    "label" : "Monitoring reconfigured",
    "notify" : false,
    "timestamp" : 1522940534507,
    "type" : "CONFIG_CHANGED"
  },
  { id : "-L9LK3gi4P7StFB_0PhB" ,
    "alertType" : "NONE",
    "comment" : "Alarm bell reconfigured",
    "deviceId" : "alarm-test",
    "gpioPin" : -1,
    "label" : "Alarm bell reconfigured",
    "notify" : false,
    "timestamp" : 1522940534519,
    "type" : "CONFIG_CHANGED"
  },
  { id : "-L9LK3glCTksRQLS5Guw" ,
    "alertType" : "NONE",
    "comment" : "Auto arm reconfigured",
    "deviceId" : "alarm-test",
    "gpioPin" : -1,
    "label" : "Auto arm reconfigured",
    "notify" : false,
    "timestamp" : 1522940534522,
    "type" : "CONFIG_CHANGED"
  },
  { id : "-L9LK4X052yD2lRFxiXw" ,
    "alertType" : "NONE",
    "comment" : "System startup at timestamp : 1522940532865",
    "deviceId" : "alarm-test",
    "gpioPin" : -1,
    "label" : "System start",
    "notify" : false,
    "timestamp" : 1522940532864,
    "type" : "SYSTEM_START"
  },
  { id : "-L9LQR9BUWGQeuP9KANy" ,
    "alertType" : "NONE",
    "comment" : "Monitoring reconfigured",
    "deviceId" : "alarm-test",
    "gpioPin" : -1,
    "label" : "Monitoring reconfigured",
    "notify" : false,
    "timestamp" : 1522942203503,
    "type" : "CONFIG_CHANGED"
  },
  { id : "-L9LQR9KGpbB23inpmsV" ,
    "alertType" : "NONE",
    "comment" : "Alarm bell reconfigured",
    "deviceId" : "alarm-test",
    "gpioPin" : -1,
    "label" : "Alarm bell reconfigured",
    "notify" : false,
    "timestamp" : 1522942203512,
    "type" : "CONFIG_CHANGED"
  },
  { id : "-L9LQR9MmTRw6CoJoovp" ,
    "alertType" : "NONE",
    "comment" : "Auto arm reconfigured",
    "deviceId" : "alarm-test",
    "gpioPin" : -1,
    "label" : "Auto arm reconfigured",
    "notify" : false,
    "timestamp" : 1522942203514,
    "type" : "CONFIG_CHANGED"
  },
  { id : "-L9LQS-PT5w4xW983zjx",
    "alertType" : "NONE",
    "comment" : "System startup at timestamp : 1522942201882",
    "deviceId" : "alarm-test",
    "gpioPin" : -1,
    "label" : "System start",
    "notify" : false,
    "timestamp" : 1522942201882,
    "type" : "SYSTEM_START"
  },
  { id : "-L9LQf2AnGtaEsgaRjPN" ,
    "alertType" : "NONE",
    "comment" : "Armed manually",
    "deviceId" : "alarm-test",
    "gpioPin" : -1,
    "label" : "System manually armed",
    "notify" : false,
    "timestamp" : 1522942264494,
    "type" : "SYSTEM_MANUAL_ARMED"
  },
  { id : "-L9LQhAifiD8J3sC1_Bf" ,
    "alertType" : "DELAYED_ALERT",
    "comment" : "Activity detected on pin 2",
    "deviceId" : "alarm-test",
    "gpioPin" : 2,
    "label" : "Device on GPIO #2",
    "notify" : true,
    "timestamp" : 1522942273233,
    "type" : "ACTIVITY"
  },
  { id : "-L9LQhKhy7AoawOUxVMH" ,
    "alertType" : "DELAYED_ALERT",
    "comment" : "Activity detected on pin 2",
    "deviceId" : "alarm-test",
    "gpioPin" : 2,
    "label" : "Device on GPIO #2",
    "notify" : true,
    "timestamp" : 1522942273872,
    "type" : "ACTIVITY"
  },
  { id : "-L9LQhPJTLi2_4mtuAKJ" ,
    "alertType" : "DELAYED_ALERT",
    "comment" : "Activity detected on pin 2",
    "deviceId" : "alarm-test",
    "gpioPin" : 2,
    "label" : "Device on GPIO #2",
    "notify" : true,
    "timestamp" : 1522942274167,
    "type" : "ACTIVITY"
  },
  { id : "-L9LRGvgvDiYKni28U1n" ,
    "alertType" : "DELAYED_ALERT",
    "comment" : "Activity detected on pin 2",
    "deviceId" : "alarm-test",
    "gpioPin" : 2,
    "label" : "Device on GPIO #2",
    "notify" : true,
    "timestamp" : 1522942423759,
    "type" : "ACTIVITY"
  },
  { id : "-L9LRGzZSod9IRXdn6xz" ,
    "alertType" : "DELAYED_ALERT",
    "comment" : "Activity detected on pin 2",
    "deviceId" : "alarm-test",
    "gpioPin" : 2,
    "label" : "Device on GPIO #2",
    "notify" : true,
    "timestamp" : 1522942424007,
    "type" : "ACTIVITY"
  },
  { id : "-L9LRH1Q41HWFTAlo1MU" ,
    "alertType" : "DELAYED_ALERT",
    "comment" : "Activity detected on pin 2",
    "deviceId" : "alarm-test",
    "gpioPin" : 2,
    "label" : "Device on GPIO #2",
    "notify" : true,
    "timestamp" : 1522942424190,
    "type" : "ACTIVITY"
  },
  { id : "-L9LRH4K4IS3rkJbzMqT" ,
    "alertType" : "DELAYED_ALERT",
    "comment" : "Activity detected on pin 2",
    "deviceId" : "alarm-test",
    "gpioPin" : 2,
    "label" : "Device on GPIO #2",
    "notify" : true,
    "timestamp" : 1522942424376,
    "type" : "ACTIVITY"
  },
  { id : "-L9LRH7JBNY5j1T8773Q" ,
    "alertType" : "DELAYED_ALERT",
    "comment" : "Activity detected on pin 2",
    "deviceId" : "alarm-test",
    "gpioPin" : 2,
    "label" : "Device on GPIO #2",
    "notify" : true,
    "timestamp" : 1522942424567,
    "type" : "ACTIVITY"
  },
  { id : "-L9LRHB2_9yW-_1uiLsy",
    "alertType" : "DELAYED_ALERT",
    "comment" : "Activity detected on pin 2",
    "deviceId" : "alarm-test",
    "gpioPin" : 2,
    "label" : "Device on GPIO #2",
    "notify" : true,
    "timestamp" : 1522942424806,
    "type" : "ACTIVITY"
  },
  { id : "-L9LRHEo9L_Iy4VmeShW" ,
    "alertType" : "DELAYED_ALERT",
    "comment" : "Activity detected on pin 2",
    "deviceId" : "alarm-test",
    "gpioPin" : 2,
    "label" : "Device on GPIO #2",
    "notify" : true,
    "timestamp" : 1522942425047,
    "type" : "ACTIVITY"
  },
  { id : "-L9LRHIZQbq_osaMLvaU" ,
    "alertType" : "DELAYED_ALERT",
    "comment" : "Activity detected on pin 2",
    "deviceId" : "alarm-test",
    "gpioPin" : 2,
    "label" : "Device on GPIO #2",
    "notify" : true,
    "timestamp" : 1522942425287,
    "type" : "ACTIVITY"
  },
  { id : "-L9LSuKIVpxu-EMpXWwu",
    "alertType" : "DELAYED_ALERT",
    "comment" : "Activity detected on pin 2",
    "deviceId" : "alarm-test",
    "gpioPin" : 2,
    "label" : "Device on GPIO #2",
    "notify" : true,
    "timestamp" : 1522942851382,
    "type" : "ACTIVITY"
  },
  { id : "-L9LSuQI04xOY2Vn8U3W" ,
    "alertType" : "DELAYED_ALERT",
    "comment" : "Activity detected on pin 2",
    "deviceId" : "alarm-test",
    "gpioPin" : 2,
    "label" : "Device on GPIO #2",
    "notify" : true,
    "timestamp" : 1522942851766,
    "type" : "ACTIVITY"
  },
  { id : "-L9LSuTR54r0IGFfI0EE" ,
    "alertType" : "DELAYED_ALERT",
    "comment" : "Activity detected on pin 2",
    "deviceId" : "alarm-test",
    "gpioPin" : 2,
    "label" : "Device on GPIO #2",
    "notify" : true,
    "timestamp" : 1522942851967,
    "type" : "ACTIVITY"
  },
  { id : "-L9LSuf3ntYe3dQsPL21" ,
    "alertType" : "DELAYED_ALERT",
    "comment" : "Activity detected on pin 2",
    "deviceId" : "alarm-test",
    "gpioPin" : 2,
    "label" : "Device on GPIO #2",
    "notify" : true,
    "timestamp" : 1522942852775,
    "type" : "ACTIVITY"
  },
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
    callback( events.slice(startIdx, endIdx), events.length );
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
    return (
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
      );
  }
}
export default ActivityPage;
