import React from 'react';
import ReactDOM from 'react-dom';
import App from './components/App';
import registerServiceWorker from './registerServiceWorker';

import './assets/vendor/bootstrap/css/bootstrap.min.css';
import './assets/vendor/font-awesome/css/font-awesome.css';
import './assets/css/sb-admin.css';



require('./assets/js/sb-admin.js');


ReactDOM.render(<App />, document.getElementById('root'));
registerServiceWorker();
