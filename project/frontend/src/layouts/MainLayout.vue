<template>
  <q-layout view="lHh Lpr lFf">
    <q-header reveal>
      <q-toolbar>
        <q-item clickable @click="returnHome">
          <q-toolbar-title class="title-header">
            <q-avatar flat round class="q-mr-sm">
              <img src="../assets/mymenu-logo.png" alt="Logo" /> </q-avatar
            ><span class="menu-title">MY MENÜ</span>
          </q-toolbar-title>
        </q-item>
        <q-space />
        <q-tabs>
          <q-route-tab
            round
            icon="document_scanner"
            :to="'/receipt'"
            class="custom-icon"
          >
            <q-tooltip anchor="center left" class="text-h5">Scanner</q-tooltip>
          </q-route-tab>
          <q-route-tab round icon="receipt_long" :to="'/'" class="custom-icon2">
            <q-tooltip anchor="center left" class="text-h5">Artikel</q-tooltip>
          </q-route-tab>
          <q-route-tab
            round
            icon="restaurant"
            :to="'/recipe'"
            class="custom-icon3"
          >
            <q-tooltip anchor="center left" class="text-h5">Rezepte</q-tooltip>
          </q-route-tab>

          <!-- <q-tab round icon="account_circle" class="custom-icon4">
            <q-tooltip anchor="center left" class="text-h6">Profil</q-tooltip>
          </q-tab> -->
        </q-tabs>
      </q-toolbar>
    </q-header>
    <!-- <q-footer reveal>
      <q-toolbar>
        <q-toolbar-title class="title-footer"
          >AI Essensplaner
          <span class="title-part">
            by
            <a
              style="font-family: 'Playfair Display'"
              href="https://lueem.dev"
              target="_blank"
              >lueem.dev</a
            ></span
          ></q-toolbar-title
        >
      </q-toolbar>
    </q-footer> -->

    <q-page-container>
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script>
import { useQuasar } from "quasar";
import { computed, defineComponent, ref } from "vue";
import router from "../router";
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
    const isReceipt = computed(
      () => router.currentRoute.value.path === "/receipt"
    );
    // const auth = getAuth();
    // if we want to get the user details, this is how its done
    // onAuthStateChanged(auth, (user) => {
    //   if (user) {
    //     email.value = user.email;
    //     name.value = user.displayName;
    //   }
    // });

    const openHistory = () => {
      void router.push("/receipt");
    };

    const returnHome = () => {
      void router.push("/recipe");
    };
    return {
      email,
      name,
      leftDrawerOpen,
      toggleLeftDrawer() {
        leftDrawerOpen.value = !leftDrawerOpen.value;
      },
      openHistory,
      returnHome,
      isReceipt,
    };
  },
});
</script>

<style lang="scss" scoped>
.color-primary {
  color: $primary;
}
a {
  text-decoration: underline;
  text-decoration-color: $dark;
  transition: text-decoration-color 0.5s ease;
  text-underline-offset: 4px;
  text-decoration-thickness: 1.5px;
}
.title-header .menu-title:hover {
  text-decoration: underline;
  text-decoration-color: $secondary;
}

.title-footer,
.title-header {
  font-size: 11px;
  color: $dark;
}

.custom-icon {
  :deep(.q-tab__content) {
    .q-icon:active {
      color: $amber;
    }
  }
}

.custom-icon2 {
  :deep(.q-tab__content) {
    .q-icon:active {
      color: $negative;
    }
  }
}

.custom-icon3 {
  :deep(.q-tab__content) {
    .q-icon:active {
      color: $secondary;
    }
  }
}

.custom-icon4 {
  :deep(.q-tab__content) {
    .q-icon {
      color: $profile !important;
    }
  }
}

.q-tab,
.q-item {
  padding-left: 4px;
  padding-right: 4px;
}

.q-tab:hover {
  border-radius: 15px;
}

.title-header {
  font-size: 26px;
}
.title-footer {
  font-style: italic;
  font-size: 16px;
}

@media (max-width: 431px) {
  .q-toolbar {
    padding-right: 6px;
  }
  .menu-title {
    display: none;
  }
  .title-part {
    font-size: 12px;
  }
  .q-footer {
    width: 178px;
  }
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
  max-width: 270px;
}

.q-footer,
.q-header {
  background-color: $bar-background;
  border: 1px solid $dark;
}
</style>
