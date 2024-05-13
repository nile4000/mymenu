<template>
  <q-layout view="lHh Lpr lFf">
    <q-header elevated>
      <q-toolbar>
        <q-toolbar-title> My Menü </q-toolbar-title>
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

    <q-page-container>
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script>
import router from "../router";
import { useQuasar } from "quasar";
import { getAuth, onAuthStateChanged } from "firebase/auth";

import EssentialLink from "components/EssentialLink.vue";

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
    const auth = getAuth();
    const stores = ref([
      {
        name: "Coop",
        steps: [
          {
            icon: "login",
            description:
              "Besuchen Sie die Supercard-Webseite und loggen Sie sich ein.",
            url: "https://www.supercard.ch/de/app-digitale-services/meine-einkaeufe.html",
          },
          {
            icon: "map",
            description:
              "Navigieren Sie zu 'App & Digitale Services' > 'Meine Einkäufe'.",
          },
          {
            icon: "file_download",
            description:
              "Laden Sie die gewünschten Coop Einkaufsbelege (PDF) auf Ihr Gerät.",
          },
          {
            icon: "scanner",
            description:
              "Navigieren Sie in der MyMenu App zum laden Sie die PDFs hoch.",
          },
        ],
      },
      {
        name: "Migros",
        steps: [
          {
            icon: "login",
            description:
              "Besuchen Sie die Migros-Webseite und loggen Sie sich ein.",
            url: "https://cumulus.migros.ch/de/konto/kassenbons.html",
          },
          {
            icon: "map",
            description: "Navigieren Sie zu 'Cumulus Konto' > 'Kassenbons'.",
          },
          {
            icon: "file_download",
            description:
              "Laden Sie die gewünschten Migros Einkaufsbelege (PDF) auf Ihr Gerät.",
          },
          {
            icon: "scanner",
            description:
              "Navigieren Sie in der MyMenu App laden Sie die PDFs hoch.",
          },
        ],
      },
    ]);
    // if we want to get the user details, this is how its done
    onAuthStateChanged(auth, (user) => {
      if (user) {
        email.value = user.email;
        name.value = user.displayName;
      }
    });
    const logout = () => {
      getAuth().signOut();
      router
        .push("/auth/login")
        .then(() => {
          // sessionStorage.clear();
          $q.notify({ message: "Sign Out Success." });
        })
        .catch((error) => console.log("error", error));
    };
    const openHistory = () => {
      router.push("/receipt");
    };
    return {
      stores,
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
