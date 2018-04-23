import React from 'react';
import { firebase } from '../firebase';

import LocationContext from './LocationContext';

const withLocation = (Component) => {
  class WithLocation extends React.Component {
    constructor(props) {
      super(props);

      this.state = {
        location: props["location"],
      };
    }







    render() {
      const { location } = this.state;

      return (
        <LocationContext.Provider value={location}>
          <Component />
        </LocationContext.Provider>
      );
    }
  }

  return WithLocation;
}

export default withLocation;
