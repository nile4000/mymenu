<template>
  <div>
    <div class="full-width col">
      <div class="full-width q-pa-lg">
        <div class="full-width">
          <div class="text-h5 q-mb-lg title-auth">Willkommen</div>
        </div>
        <div class="full-width">
          <q-form @submit="submitForm">
            <q-input
              outlined
              class="q-mb-md"
              type="email"
              label="E-Mail"
              v-model="formData.email"
            />
            <q-input
              outlined
              class="q-mb-md"
              type="password"
              label="Passwort"
              v-model="formData.password"
            />
            <div class="row">
              <q-btn type="submit" color="primary" :label="title" unelevated />
              <q-space></q-space>
              <q-btn
                flat
                label="Passwort vergessen?"
                color="grey-7"
                class="q-px-none"
                no-caps
                v-if="tab !== 'register'"
                @click="forgotPassword"
              />
            </div>
          </q-form>
        </div>
      </div>
      <div class="bg-grey-2 full-width" style="min-height: 110px">
        <div class="text-center text-subtitle1 text-grey-5 q-my-sm">oder</div>
        <div class="flex flex-center">
          <q-btn
            color="primary"
            @click="google"
            unelevated
            class="q-pa-none"
            square
          >
            <q-avatar square size="34px">
              <q-img class="img-margin" :src="googleLogo"></q-img>
            </q-avatar>
            <div class="q-mx-md">{{ title }} mit Google</div>
          </q-btn>
        </div>
      </div>
    </div>

    <q-dialog v-model="resetPwdDialog">
      <ForgotPassword />
    </q-dialog>
  </div>
</template>

<script>
import router from "../router";
import ForgotPassword from "./ForgotPassword.vue";
import { defineComponent, reactive, ref, computed } from "vue";
import {
  GoogleAuthProvider,
  getAuth,
  signInWithPopup,
  signInWithEmailAndPassword,
  createUserWithEmailAndPassword,
} from "firebase/auth";
import googleLogo from "../assets/google-logo.png";

export default defineComponent({
  name: "AuthComponent",
  props: ["tab"],
  components: { ForgotPassword },
  data() {
    return {
      googleLogo,
    };
  },
  setup(props, context) {
    const formData = reactive({
      email: "",
      password: "",
    });
    const title = computed(() => {
      return props.tab === "login" ? "Einloggen" : "Registrieren";
    });
    const resetPwdDialog = ref(false);
    const submitForm = () => {
      if (props.tab === "login") {
        signInExistingUser(formData.email, formData.password);
      } else {
        createUser(formData.email, formData.password);
      }
    };
    const google = () => {
      const provider = new GoogleAuthProvider();
      const auth = getAuth();
      signInWithPopup(auth, provider)
        .then((result) => {
          router.push("/");
        })
        .catch((error) => console.log("error", error));
    };
    const signInExistingUser = (email, password) => {
      console.log(email, password);
      const auth = getAuth();
      signInWithEmailAndPassword(auth, email, password)
        .then((userCredential) => {
          router.push("/");
        })
        .catch((error) => {
          console.log(error);
        });
    };
    const createUser = (email, password) => {
      console.log(email, password);
      const auth = getAuth();
      createUserWithEmailAndPassword(auth, email, password)
        .then((auth) => {
          router.push("/");
        })
        .catch((error) => {
          console.log(error);
        });
    };
    const forgotPassword = () => {
      resetPwdDialog.value = true;
    };
    return {
      formData,
      resetPwdDialog,
      submitForm,
      google,
      signInExistingUser,
      createUser,
      forgotPassword,
      title,
    };
  },
});
</script>
<style scoped>
.text-h5 {
  font-family: "Playfair Display", serif;
}

.img-margin {
  margin: 1px 1px 1px 6px;
}
</style>
