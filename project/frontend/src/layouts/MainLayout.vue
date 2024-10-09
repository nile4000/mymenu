<template>
  <q-layout view="lHh Lpr lFf">
    <q-header elevated>
      <q-toolbar>
        <q-toolbar-title class="title-header">
          <q-avatar square size="36px" class="q-mr-sm">
            <img src="../assets/logo.png" alt="Logo" /> </q-avatar
          >MY MENÜ
        </q-toolbar-title>
        <div>
          <q-btn-dropdown unelevated flat color="white">
            <q-list>
              <!-- <q-item class="bg-grey-3">
                <q-item-title>
                  Eingeloggt als: <br /><b>{{ email }}</b>
                </q-item-title>
              </q-item> -->
              <q-item clickable v-close-popup @click="openHistory">
                <q-item-section avatar>
                  <q-item-label
                    ><q-icon name="history"></q-icon> Verlauf</q-item-label
                  >
                </q-item-section>
              </q-item>
              <!-- <q-item clickable v-close-popup @click="logout">
                <q-item-section>
                  <q-item-label>Ausloggen</q-item-label>
                </q-item-section>
              </q-item> -->
            </q-list>
          </q-btn-dropdown>
        </div>
      </q-toolbar>
    </q-header>
    <q-footer>
        <q-toolbar>
          <q-toolbar-title class="title-footer">AI Essensplaner</q-toolbar-title>
        </q-toolbar>
      </q-footer>

    <q-page-container>
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script>
import router from "../router";
import { useQuasar } from "quasar";
// import { getAuth, onAuthStateChanged } from "firebase/auth";


// const linksList = [
//   {
//     title: "Home",
//     caption: "Willkommen",
//     icon: "home",
//     link: "/",
//   },
//   {
//     title: "Erfassung",
//     caption: "Dokumente Erfassung",
//     icon: "scanner",
//     link: "/scanner",
//   },
//   // {
//   //   title: "Deine Belege",
//   //   caption: "Gescannte Belege",
//   //   icon: "receipt",
//   //   link: "/receipt",
//   // },
//   {
//     title: "Esswaren",
//     caption: "Deine Esswaren",
//     icon: "restaurant",
//     link: "/food",
//   },
// ];

import { defineComponent, ref } from "vue";

export default defineComponent({
  name: "MainLayout",

  // components: {
  //   // EssentialLink,
  // },

  setup() {
    const leftDrawerOpen = ref(false);
    const email = ref("");
    const name = ref("");
    const $q = useQuasar();
    // const auth = getAuth();
    // if we want to get the user details, this is how its done
    // onAuthStateChanged(auth, (user) => {
    //   if (user) {
    //     email.value = user.email;
    //     name.value = user.displayName;
    //   }
    // });
    const logout = () => {
      // getAuth().signOut();
      router
        .push("/auth/login")
        .then(() => {
          // sessionStorage.clear();
          $q.notify({ message: "Sign Out Success." });
        })
        .catch((error) => console.log("error", error));
    };
    const openHistory = () => {
      void router.push("/receipt");
    };
    return {
      email,
      name,
      leftDrawerOpen,
      toggleLeftDrawer() {
        leftDrawerOpen.value = !leftDrawerOpen.value;
      },
      logout,
      openHistory,
    };
  },
});
</script>

<style lang="scss" scoped>
.title-header {
  font-family: "Playfair Display", serif;
  font-weight: 500;
  letter-spacing: 0.2em;
}
.title-footer {
  font-family: "Playfair Display", serif;
  letter-spacing: 0.2em;
}
</style>
