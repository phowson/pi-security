import React from 'react';
import { Route, Link, withRouter } from 'react-router-dom';

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
  DropdownItem
} from 'reactstrap';


const NoLocationSideNav = () =>
  <ul className="navbar-nav navbar-sidenav" id="exampleAccordion">

    <li className="nav-item" data-toggle="tooltip" data-placement="right" title="Status">
      <Link className="nav-link" to="/">
        <i className="fa fa-fw fa-home"></i>
        <span className="nav-link-text">Home</span>
      </Link>
    </li>

  </ul>

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
      <Link className="nav-link" to={routes.SENSORHEALTH}>
        <i className="fa fa-fw fa-heartbeat"></i>
        <span className="nav-link-text">Sensor Health</span>
      </Link>
    </li>

    <li className="nav-item" data-toggle="tooltip" data-placement="right" title="Control">
      <Link className="nav-link" to={routes.DEVICEHEALTH}>
        <i className="fa fa-fw fa-heartbeat"></i>
        <span className="nav-link-text">Device Health</span>
      </Link>
    </li>

    <li className="nav-item" data-toggle="tooltip" data-placement="right" title="Control">
      <Link className="nav-link" to={routes.ARMDISARM}>
        <i className="fa fa-fw fa-power-off"></i>
        <span className="nav-link-text">Automatic Arm / Disarm</span>
      </Link>
    </li>


    <li className="nav-item" data-toggle="tooltip" data-placement="right" title="Control">
      <Link className="nav-link" to={routes.PERMISSIONS}>
        <i className="fa fa-fw fa-lock"></i>
        <span className="nav-link-text">Permissions</span>
      </Link>
    </li>

    <li className="nav-item" data-toggle="tooltip" data-placement="right" title="Settings">
      <Link className="nav-link" to={routes.MONITORINGSETTINGS}>
        <i className="fa fa-fw fa-sliders"></i>
        <span className="nav-link-text">Monitoring setup</span>
      </Link>
    </li>


    <li className="nav-item" data-toggle="tooltip" data-placement="right" title="Control">
      <Link className="nav-link" to={routes.CALLLOG}>
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
      <Link className="nav-link" to={routes.INSTALLERDEVICESETTINGS}>
        <i className="fa fa-fw fa-cogs"></i>
        <span className="nav-link-text">Installer's settings</span>
      </Link>
    </li>

    

  </ul>

const HomeSideNav = () =>
  <ul className="navbar-nav navbar-sidenav" id="exampleAccordion">

    <li className="nav-item" data-toggle="tooltip" data-placement="right" title="Status">
      <Link className="nav-link" to="/">
        <i className="fa fa-fw fa-home"></i>
        <span className="nav-link-text">Home</span>
      </Link>
    </li>

    <li className="nav-item" data-toggle="tooltip" data-placement="right" title="Status">
      <Link className="nav-link" to={routes.SIGN_IN}>
        <i className="fa fa-fw fa-sign-in"></i>
        <span className="nav-link-text">Sign In</span>
      </Link>
    </li>

    <li className="nav-item" data-toggle="tooltip" data-placement="right" title="Status">
      <Link className="nav-link" to={routes.SIGN_UP}>
        <i className="fa fa-fw fa-user"></i>
        <span className="nav-link-text">Sign Up</span>
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


    return (<nav className="navbar navbar-expand-lg navbar-dark bg-dark fixed-top" id="mainNav">
      <Link className="navbar-brand" to="/">Installer and Administrator Interface</Link>

      <NavbarToggler onClick={this.toggle} />

      <Collapse isOpen={this.state.isOpen} navbar>



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
              <Route exact path={routes.SENSORHEALTH} component={() => <LocationsSideNav />} />
              <Route exact path={routes.DEVICESETTINGS} component={() => <LocationsSideNav />} />
              <Route exact path={routes.INSTALLERDEVICESETTINGS} component={() => <LocationsSideNav />} />
              <Route exact path={routes.MONITORINGSETTINGS} component={() => <LocationsSideNav />} />

              <Route exact path={routes.LOCATIONS} component={() => <NoLocationSideNav />} />
              <Route exact path={routes.ACCOUNT} component={() => <NoLocationSideNav />} />

              <Route exact path="/" component={() => <HomeSideNav />} />

              <Route exact path={routes.SIGN_IN} component={() => <HomeSideNav />} />
              <Route exact path={routes.SIGN_UP} component={() => <HomeSideNav />} />
              <Route exact path={routes.PASSWORD_FORGET} component={() => <HomeSideNav />} />

            </div>

            : <div>
              <Route exact path={routes.LOCATIONS} component={() => <NoLocationSideNav />} />
              <Route exact path={routes.ACCOUNT} component={() => <NoLocationSideNav />} />

              <Route exact path="/" component={() => <HomeSideNav />} />

              <Route exact path={routes.SIGN_IN} component={() => <HomeSideNav />} />
              <Route exact path={routes.SIGN_UP} component={() => <HomeSideNav />} />
              <Route exact path={routes.PASSWORD_FORGET} component={() => <HomeSideNav />} />


            </div>}
        </LocationContext.Consumer>
        <AuthUserContext.Consumer>
          {authUser => authUser
            ? <Nav className="ml-auto" navbar>


              <NavItem><Link onClick={this.fold} className='nav-link' to={routes.LOCATIONS}><i className="fa fa-fw fa-location-arrow"></i>Locations</Link></NavItem>
              <NavItem><Link onClick={this.fold} className='nav-link' to={routes.ACCOUNT}><i className="fa fa-fw fa-user"></i>Account</Link></NavItem>
              <NavItem><SignOutButton /></NavItem>
            </Nav>
            : <Nav className="ml-auto" navbar>
            
            
            </Nav>}
        </AuthUserContext.Consumer>
      </Collapse>

    </nav>);
  }

}




const Navigation = (history) =>
  <BootstrapNavigation history={history} />



export default withRouter(Navigation);
