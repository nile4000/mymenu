<template>
  <div class="flex flex-center">
    <q-card style="width: 500px; max-width: 40vw">
      <q-card-section class="row items-center q-pb-none">
        <div class="text-h6">Passwort zurücksetzen</div>
        <q-space />
        <q-btn icon="close" flat round dense v-close-popup />
      </q-card-section>
      <q-card-section class="q-pt-md">
        <q-form ref="resetPasswordForm">
          <q-input
            type="email"
            v-model="form.email"
            label="E-Mail angeben"
            lazy-rules
            :rules="[
              (val) => (val && val.length > 0) || 'Bitte E-Mail angeben',
            ]"
          />
        </q-form>
        <q-card-actions align="right">
          <div class="row q-mt-xs">
            <q-btn
              class="q-px-md"
              label="Reset my password"
              color="primary"
              @click="resetPassword"
              unelevated
            />
          </div>
        </q-card-actions>
      </q-card-section>
    </q-card>
  </div>
</template>

<script>
import { defineComponent, reactive } from "vue";
// import { getAuth, sendPasswordResetEmail } from "firebase/auth";
import { useQuasar } from "quasar";

export default defineComponent({
  name: "ForgotPassword",
  setup() {
    const $q = useQuasar();
    const form = reactive({ email: "" });
    const resetPassword = () => {
      const auth = getAuth();
      sendPasswordResetEmail(auth, form.email)
        .then(() => {
          form.email = "";
          $q.notify({
            message: "Überprüfe deine E-Mails und setz dort dein Passwort zurück. ",
          });
        })
        .catch((error) => console.log(error));
    };
    return {
      form,
      resetPassword,
    };
  },
});
</script>
