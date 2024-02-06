<template>
  <q-page class="flex flex-center">
    <div class="q-pa-md">
      <q-uploader
        :url="apiUrl"
        label="PDFs hochladen"
        style="max-width: 600px"
        accept=".pdf"
        :headers="authHeaders"
        @rejected="onRejected"
        @uploaded="onUploaded"
      ></q-uploader>
    </div>
  </q-page>
</template>

<script>
import { defineComponent } from "vue";
import { getAuth } from "firebase/auth";

export default defineComponent({
  name: "ScannerPage",

  computed: {
    authHeaders() {
      const auth = getAuth();
      const userId = auth.currentUser ? auth.currentUser.uid : null;
      if (userId) {
        return [{ name: "FirebaseAuthId", value: userId }];
      } else {
        console.error("User not logged in");
        return [];
      }
    },
  },

  methods: {
    onRejected(rejectedEntries) {
      this.$q.notify({
        type: "negative",
        message: `${rejectedEntries.length} file did not pass validation constraints`,
      });
    },
    apiUrl() {
      const apiUrl = process.env.API_URL;
      return `${apiUrl}/api/extract`;
    },

    onUploaded(event) {
      if (event.xhr && event.xhr.responseText) {
        try {
          const response = JSON.parse(event.xhr.responseText);

          // Save the extracted data to the session storage
          const now = new Date();
          const datetimeKey = `${now.getFullYear()}-${
            now.getMonth() + 1
          }-${now.getDate()}_${now.getHours()}-${now.getMinutes()}-${now.getSeconds()}`;

          // save with a unique key to avoid overwriting previous data
          sessionStorage.setItem(datetimeKey, JSON.stringify(response));

          this.$q.notify({
            type: "positive",
            message: "Upload successful!",
          });
          console.log("Server response:", response);
        } catch (error) {
          this.$q.notify({
            type: "negative",
            message: "Failed to parse server response.",
          });
          console.error("Error parsing response:", error);
        }
      }
    },
  },
});
</script>
