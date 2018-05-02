import React from 'react';
import ReactDOM from 'react-dom';
import App from './components/App';
import registerServiceWorker from './registerServiceWorker';

import './assets/vendor/bootstrap/css/bootstrap.css';
import 'react-bootstrap-table-next/dist/react-bootstrap-table2.min.css';
import './assets/vendor/font-awesome/css/font-awesome.css';
import './assets/css/sb-admin.css';
import './assets/css/dropdown-adaptions.css'



// require('./assets/js/sb-admin.js');


ReactDOM.render(<App />, document.getElementById('root'));
registerServiceWorker();
