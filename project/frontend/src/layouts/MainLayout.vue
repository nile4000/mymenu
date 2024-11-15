<template>
  <q-layout view="lHh Lpr lFf" class="shadow-2 rounded-borders">
    <q-header reveal>
      <q-toolbar>
        <q-item clickable @click="returnHome">
          <q-toolbar-title class="title-header">
            <q-avatar flat round class="q-mr-sm">
              <img src="../assets/logo.png" alt="Logo" /> </q-avatar
            ><span class="menu-title">MY MENÜ</span>
          </q-toolbar-title>
        </q-item>
        <q-space />
        <q-tabs>
          <q-tab icon="account_circle" />
        </q-tabs>
      </q-toolbar>
    </q-header>
    <q-footer reveal>
      <q-toolbar>
        <q-toolbar-title class="title-footer"
          >AI Essensplaner by
          <a href="https://lueem.dev" target="_blank"
            >lueem.dev</a
          ></q-toolbar-title
        >
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

    const returnHome = () => {
      void router.push("/");
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
      returnHome,
    };
  },
});
</script>

<style lang="scss" scoped>
.title-header .menu-title,
a {
  text-decoration: underline;
  transition: text-decoration-color 0.5s ease;
  text-underline-offset: 4px;
  text-decoration-thickness: 1.5px;
}
.title-header .menu-title:hover,
a:hover {
  text-decoration: underline;
  text-decoration-color: $secondary;
}

.title-footer,
.title-header {
  color: $dark;
  font-weight: 500;
}

.q-tab,
.q-item {
  padding-left: 4px;
  padding-right: 4px;
}

.q-tab:hover {
  background-color: $tab-accent;
  border-radius: 15px;
}

.title-header {
  font-size: 26px;
}
.title-footer {
  font-style: italic;
  font-size: 16px;
}

.q-toolbar__title {
  padding: 0;
}

.q-avatar {
  background-color: $bar-primary;
}
.q-header {
  margin: 10px;
  border-radius: 15px !important;
}

.q-footer {
  border-top-left-radius: 15px;
  border-top-right-radius: 15px;
  max-width: 280px;
}
.q-footer,
.q-header {
  background-color: $bar-background;
  border: 1px solid $dark;
}
</style>
