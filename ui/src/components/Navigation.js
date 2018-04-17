import React from 'react';
import { Link } from 'react-router-dom';

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


  const Navigation = () =>

  <nav class="navbar navbar-expand-lg navbar-dark bg-dark fixed-top" id="mainNav">
    <NavbarBrand href="/">Pi Security</NavbarBrand>



    <AuthUserContext.Consumer>
      {authUser => authUser
        ? <NavigationAuth />
      : <NavigationNonAuth />
  }
</AuthUserContext.Consumer>



<div class="collapse navbar-collapse" id="navbarResponsive">







</div>

</nav>

//{this.state.isOpen}

const NavigationAuth = () =>
<Nav className="ml-auto" navbar>
  <NavItem><NavLink href={routes.LOCATIONS}>Locations</NavLink></NavItem>
  <NavItem><NavLink href={routes.ACCOUNT}>Account</NavLink></NavItem>
  <NavItem><SignOutButton /></NavItem>
</Nav>



const NavigationNonAuth = () =>
<Nav className="ml-auto" navbar>
  <NavItem><NavLink  href={routes.LANDING}>Home</NavLink></NavItem>
  <NavItem><NavLink  href={routes.SIGN_IN}>Sign In</NavLink></NavItem>
</Nav>

export default Navigation;
