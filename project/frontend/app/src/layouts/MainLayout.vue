<template>
  <q-layout view="lHh Lpr lFf">
    <q-header elevated>
      <q-toolbar>
        <q-btn
          flat
          dense
          round
          icon="menu"
          aria-label="Menu"
          @click="toggleLeftDrawer"
        />
        <q-toolbar-title> My Menü </q-toolbar-title>
        <div>
          <q-btn-dropdown
            unelevated
            flat
            color="white"
            icon="person"
          >
            <q-list>
              <q-item class="bg-grey-3">
                <q-item-title>
                  Eingeloggt als: <br /><b>{{ email }}</b>
                </q-item-title>
              </q-item>
              <q-item clickable v-close-popup @click="logout">
                <q-item-section>
                  <q-item-label>Ausloggen</q-item-label>
                </q-item-section>
              </q-item>
            </q-list>
          </q-btn-dropdown>
        </div>
      </q-toolbar>
    </q-header>

    <q-drawer v-model="leftDrawerOpen" show-if-above bordered>
      <q-list>
        <q-item-label header></q-item-label>

        <EssentialLink
          v-for="link in essentialLinks"
          :key="link.title"
          v-bind="link"
        />
      </q-list>
    </q-drawer>

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

const linksList = [
  {
    title: "Home",
    caption: "Willkommen",
    icon: "home",
    link: "/",
  },
  {
    title: "Erfassung",
    caption: "Dokumente Erfassung",
    icon: "scanner",
    link: "/scanner",
  },
  {
    title: "Deine Belege",
    caption: "Gescannte Belege",
    icon: "receipt",
    link: "/receipt",
  },
];

import { defineComponent, ref } from "vue";

export default defineComponent({
  name: "MainLayout",

  components: {
    EssentialLink,
  },

  setup() {
    const leftDrawerOpen = ref(false);
    const email = ref("");
    const name = ref("");
    const $q = useQuasar();
    const auth = getAuth();
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
          $q.notify({ message: "Sign Out Success." });
        })
        .catch((error) => console.log("error", error));
    };
    return {
      essentialLinks: linksList,
      email,
      name,
      leftDrawerOpen,
      toggleLeftDrawer() {
        leftDrawerOpen.value = !leftDrawerOpen.value;
      },
      logout,
    };
  },
});
</script>
