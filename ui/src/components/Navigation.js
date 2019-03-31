import React from 'react';
import {Route, Link,  withRouter } from 'react-router-dom';

import * as routes from '../constants/routes';
import AuthUserContext from './AuthUserContext';
import LocationContext from './LocationContext';
import SignOutButton from './SignOut';

import {
  Collapse,
  Navbar,
  NavbarToggler,
  NavbarBrand,
  Nav,
  NavItem,
  NavLink,
  UncontrolledDropdown,
  DropdownToggle,
  DropdownMenu,
  DropdownItem } from 'reactstrap';


const LocationsSideNav = () =>
<ul className="navbar-nav navbar-sidenav" id="exampleAccordion">

  <li className="nav-item" data-toggle="tooltip" data-placement="right" title="Status">
    <Link className="nav-link" to={routes.STATUS}>
      <i className="fa fa-fw fa-eye"></i>
      <span className="nav-link-text">Status</span>
    </Link>
  </li>

  <li className="nav-item" data-toggle="tooltip" data-placement="right" title="Activity">
    <Link className="nav-link" to={routes.ACTIVITY}>
      <i className="fa fa-fw fa-exclamation-circle"></i>
      <span className="nav-link-text">Activity</span>
    </Link>
  </li>

  <li className="nav-item" data-toggle="tooltip" data-placement="right" title="Statistics">
    <Link className="nav-link" to={routes.STATS}>
      <i className="fa fa-fw fa-area-chart"></i>
      <span className="nav-link-text">Statistics</span>
    </Link>
  </li>
  <li className="nav-item" data-toggle="tooltip" data-placement="right" title="Control">
    <Link className="nav-link" to= {routes.DEVICEHEALTH}>
      <i className="fa fa-fw fa-heartbeat"></i>
      <span className="nav-link-text">Device Health</span>
    </Link> 
  </li>

  <li className="nav-item" data-toggle="tooltip" data-placement="right" title="Control">
    <Link className="nav-link" to= {routes.ARMDISARM}>
      <i className="fa fa-fw fa-power-off"></i>
      <span className="nav-link-text">Automatic Arm / Disarm</span>
    </Link>
  </li>


  <li className="nav-item" data-toggle="tooltip" data-placement="right" title="Control">
    <Link className="nav-link" to= {routes.PERMISSIONS}>
      <i className="fa fa-fw fa-lock"></i>
      <span className="nav-link-text">Permissions</span>
    </Link>
  </li>


  <li className="nav-item" data-toggle="tooltip" data-placement="right" title="Control">
    <Link className="nav-link" to= {routes.CALLLOG}>
      <i className="fa fa-fw fa-phone"></i>
      <span className="nav-link-text">Call log</span>
    </Link>
  </li>

  <li className="nav-item" data-toggle="tooltip" data-placement="right" title="Settings">
    <Link className="nav-link" to={routes.DEVICESETTINGS}>
      <i className="fa fa-fw fa-cogs"></i>
      <span className="nav-link-text">Device setup</span>
    </Link>
  </li>

  <li className="nav-item" data-toggle="tooltip" data-placement="right" title="Settings">
    <Link className="nav-link" to={routes.MONITORINGSETTINGS}>
      <i className="fa fa-fw fa-sliders"></i>
      <span className="nav-link-text">Monitoring setup</span>
    </Link>
  </li>

</ul>


class BootstrapNavigation extends React.Component {
  constructor(props) {
    super(props);

    this.toggle = this.toggle.bind(this);
    this.fold = this.fold.bind(this);
    this.state = {
      isOpen: false
    };
  }


  fold() {

    this.setState({
      isOpen: false
    });

  }

  toggle() {

    this.setState({
      isOpen: !this.state.isOpen
    });
  }
  render() {


    return ( <nav className="navbar navbar-expand-lg navbar-dark bg-dark fixed-top" id="mainNav">
      <Link className="navbar-brand" to= "/">Security System Replacement</Link>

      <NavbarToggler onClick={this.toggle} />

      <Collapse isOpen={this.state.isOpen} navbar>
        <AuthUserContext.Consumer>
          {authUser => authUser
            ? <Nav className="ml-auto" navbar>


            <NavItem><Link onClick={this.fold} className='nav-link' to={routes.LOCATIONS}>Locations</Link></NavItem>
            <NavItem><Link onClick={this.fold} className='nav-link' to={routes.ACCOUNT}>Account</Link></NavItem>
            <NavItem><SignOutButton /></NavItem>
          </Nav>
          :<Nav className="ml-auto" navbar>
          <NavItem><Link onClick={this.fold} className='nav-link' to={routes.LANDING}>Home</Link></NavItem>
          <NavItem><Link onClick={this.fold} className='nav-link' to={routes.SIGN_IN}>Sign In</Link></NavItem>
        </Nav>  }
      </AuthUserContext.Consumer>


      <LocationContext.Consumer>
        {location => location
          ?
          <div>
            <Route exact path={routes.STATUS} component={() => <LocationsSideNav />} />
            <Route exact path={routes.ACTIVITY} component={() => <LocationsSideNav />} />
            <Route exact path={routes.CALLLOG} component={() => <LocationsSideNav />} />
            <Route exact path={routes.STATS} component={() => <LocationsSideNav />} />
            <Route exact path={routes.DEVICEHEALTH} component={() => <LocationsSideNav />} />
            <Route exact path={routes.ARMDISARM} component={() => <LocationsSideNav />} />
            <Route exact path={routes.PERMISSIONS} component={() => <LocationsSideNav />} />
            <Route exact path={routes.DEVICESETTINGS} component={() => <LocationsSideNav />} />
            <Route exact path={routes.MONITORINGSETTINGS} component={() => <LocationsSideNav />} />
            
            
            
          </div>

          : <div/> }
        </LocationContext.Consumer>

      </Collapse>

    </nav>);
  }

}




const Navigation = ( history ) =>
<BootstrapNavigation history={history}/>



export default withRouter(Navigation);
