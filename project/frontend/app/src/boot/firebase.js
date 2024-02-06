import { boot } from "quasar/wrappers";
// import firebase from 'firebase'
import { initializeApp, getCurrentUser } from "firebase/app";
import { getAuth } from "firebase/auth";

const firebaseConfig = {
  apiKey: "AIzaSyCdFPyGbaC3lQv_uid-bx5vcVDLRyiIYUk",
  authDomain: "mymenu-bdf37.firebaseapp.com",
  projectId: "mymenu-bdf37",
  storageBucket: "mymenu-bdf37.appspot.com",
  messagingSenderId: "343475080236",
  appId: "1:343475080236:web:c37379f45b9ce915509589",
  measurementId: "G-Z838G5N852"
};

// "async" is optional;
// more info on params: https://v2.quasar.dev/quasar-cli/boot-files
export default boot(async ({ router, app }) => {
  initializeApp(firebaseConfig);
  const auth = getAuth();
  router.beforeEach((to, from, next) => {
    return new Promise((resolve, reject) => {
      const unsubscribe = auth.onAuthStateChanged(function (user) {
        unsubscribe();
        if (!user && to.path != "/auth/login") {
          next("/auth/login");
        } else if (user) {
          // if (!user.emailVerified && to.path != "/auth/verifyEmail" && to.path != "/auth/completeAccount") {
          //   next("/auth/verifyEmail");
          // } else
          if (
            to.path == "/auth/login" ||
            to.path == "/auth/verifyEmail" ||
            to.path == "/auth/completeAccount"
          ) {
            next("/");
          } else {
            next();
          }
        } else {
          next();
        }
        resolve(user);
      }, reject);
    });
  });
});
