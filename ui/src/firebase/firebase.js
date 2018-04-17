

import * as firebase from 'firebase';
const config = {
  apiKey: "AIzaSyDAEDD5moOkblTYHZvIG-jCMTl-FRbuWaw",
    authDomain: "pisecurity-72796.firebaseapp.com",
    databaseURL: "https://pisecurity-72796.firebaseio.com",
    projectId: "pisecurity-72796",
    storageBucket: "pisecurity-72796.appspot.com",
    messagingSenderId: "229435963846"
};

if (!firebase.apps.length) {
  firebase.initializeApp(config);
}

const auth = firebase.auth();


export default firebase;
export {
  auth
};
