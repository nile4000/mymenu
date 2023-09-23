// Native builds get the config from google-services.json GoogleService-Info.plist
import firebase from 'firebase/compat/app';
// import { getAnalytics } from "firebase/analytics";

const firebaseConfig = {
  apiKey: "AIzaSyCdFPyGbaC3lQv_uid-bx5vcVDLRyiIYUk",
  authDomain: "mymenu-bdf37.firebaseapp.com",
  projectId: "mymenu-bdf37",
  storageBucket: "mymenu-bdf37.appspot.com",
  messagingSenderId: "343475080236",
  appId: "1:343475080236:web:c37379f45b9ce915509589",
  measurementId: "G-Z838G5N852"
};

const initializeApp = (): void => {
  firebase.initializeApp(firebaseConfig);
};
// const analytics = getAnalytics(app);

export default initializeApp;
