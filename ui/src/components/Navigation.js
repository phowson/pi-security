import React from 'react';
import {Route, Link,  withRouter } from 'react-router-dom';

import * as routes from '../constants/routes';
import AuthUserContext from './AuthUserContext';
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
  <li className="nav-item" data-toggle="tooltip" data-placement="right" title="Dashboard">
    <Link className="nav-link" to= "index.html">
      <i className="fa fa-fw fa-dashboard"></i>
      <span className="nav-link-text">Activity</span>
    </Link>
  </li>
  <li className="nav-item" data-toggle="tooltip" data-placement="right" title="Charts">
    <Link className="nav-link" to= "charts.html">
      <i className="fa fa-fw fa-area-chart"></i>
      <span className="nav-link-text">Control</span>
    </Link>
  </li>
  <li className="nav-item" data-toggle="tooltip" data-placement="right" title="Charts">
    <Link className="nav-link" to= "charts.html">
      <i className="fa fa-fw fa-area-chart"></i>
      <span className="nav-link-text">Statistics</span>
    </Link>
  </li>

  <li className="nav-item" data-toggle="tooltip" data-placement="right" title="Charts">
    <Link className="nav-link" to= "charts.html">
      <i className="fa fa-fw fa-area-chart"></i>
      <span className="nav-link-text">Settings</span>
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
    console.log("Fold")

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

    return <nav className="navbar navbar-expand-lg navbar-dark bg-dark fixed-top" id="mainNav">
      <Link className="navbar-brand" to= "/">Pi Security</Link>

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

        <Route exact path={routes.LOCATIONS} component={() => <LocationsSideNav />} />

      </Collapse>

    </nav>
  }

}




const Navigation = ( history ) =>
  <BootstrapNavigation history={history}/>



export default withRouter(Navigation);
