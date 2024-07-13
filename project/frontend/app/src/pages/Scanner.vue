<template>
  <div class="q-pa-md">
    <DialogComponent></DialogComponent>
    <q-uploader
      :url="apiUrl"
      label="PDFs hochladen"
      accept=".pdf"
      icon="scanner"
      style="width: 100%"
      :headers="authHeaders"
      @rejected="onRejected"
      @uploaded="onUploaded"
    ></q-uploader>
  </div>
</template>
<script>
import { defineComponent, computed } from "vue";
import { getAuth } from "firebase/auth";
import DialogComponent from "../components/DialogComponent.vue";
import { useQuasar } from "quasar";

export default defineComponent({
  name: "ScannerPage",
  components: {
    DialogComponent,
  },

  setup() {
    const $q = useQuasar();
    const auth = getAuth();

    const authHeaders = computed(() => {
      const userId = auth.currentUser ? auth.currentUser.uid : 0;
      if (userId) {
        return [{ name: "FirebaseAuthId", value: userId }];
      } else {
        console.error("User not logged in");
        return [];
      }
    });

    const apiUrl = computed(() => {
      const apiUrl = process.env.API_URL;
      return `${apiUrl}/api/extract`;
    });

    function onRejected(rejectedEntries) {
      $q.notify({
        type: "negative",
        message: `${rejectedEntries.length} file did not pass validation constraints`,
      });
    }

    function onUploaded(event) {
      if (event.xhr && event.xhr.responseText) {
        try {
          const response = JSON.parse(event.xhr.responseText);
          const uid = response.UID || 0;
          sessionStorage.setItem("receipt_" + uid, JSON.stringify(response));

          $q.dialog({
            component: DialogComponent,
            componentProps: {
              response: [response],
            },
          });
        } catch (error) {
          $q.notify({
            type: "negative",
            message: "Failed to parse server response.",
          });
          console.error("Error parsing response:", error);
        }
      }
    }

    return {
      authHeaders,
      apiUrl,
      onRejected,
      onUploaded,
    };
  },
});
</script>
<style scoped>
::v-deep .q-icon {
  color: white !important;
}
</style>
