import { boot } from "quasar/wrappers";

const firebaseConfig = {
  apiKey: process.env.FIREBASE_API_KEY,
  authDomain: "mymenu-bdf37.firebaseapp.com",
  projectId: "mymenu-bdf37",
  storageBucket: "mymenu-bdf37.appspot.com",
  messagingSenderId: "343475080236",
  appId: "1:343475080236:web:c37379f45b9ce915509589",
};

export default boot(() => {
  // ToDo: used for auth
  // initializeApp(firebaseConfig);
  // const auth = getAuth();

  // This code has been commented out to disable the auth guard on startup
  // router.beforeEach((to, from, next) => {
  //   return new Promise((resolve, reject) => {
  //     const unsubscribe = auth.onAuthStateChanged(function (user) {
  //       unsubscribe();
  //       if (!user && to.path != "/auth/login") {
  //         next("/auth/login");
  //       } else if (user) {
  //         if (
  //           to.path == "/auth/login" ||
  //           to.path == "/auth/verifyEmail" ||
  //           to.path == "/auth/completeAccount"
  //         ) {
  //           next("/");
  //         } else {
  //           next();
  //         }
  //       } else {
  //         next();
  //       }
  //       resolve(user);
  //     }, reject);
  //   });
  // });

  // Simply navigate without auth checks
  // router.beforeEach((to, from, next) => {
  //   next();
  // });
});
