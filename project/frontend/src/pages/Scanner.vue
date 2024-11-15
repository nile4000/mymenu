<template>
  <div class="custom">
    <DialogComponent></DialogComponent>
    <q-uploader
      :url="apiUrl"
      label="PDFs hochladen"
      accept=".pdf"
      :headers="authHeaders"
      multiple
      color="amber"
      text-color="black"
      class="uploader"
      @rejected="onRejected"
      @uploaded="onUploaded"
    ></q-uploader>
  </div>
</template>
<script lang="ts">
import { defineComponent, computed } from "vue";
import DialogComponent from "../components/DialogComponent.vue";
import { useQuasar } from "quasar";

export default defineComponent({
  name: "ScannerPage",
  components: {
    // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment
    DialogComponent,
  },

  setup() {
    const $q = useQuasar();
    // const auth = getAuth();

    const authHeaders = computed(() => {
      // const userId = auth.currentUser ? auth.currentUser.uid : 0;
      // if (userId) {
      //   return [{ name: "FirebaseAuthId", value: userId }];
      // } else {
      //   console.error("User not logged in");
      //   return [];
      // }
      return [];
    });

    const apiUrl = computed(() => {
      const apiUrl = process.env.API_URL;
      return `${apiUrl}/api/extract`;
    });

    function onRejected(rejectedEntries: any) {
      $q.notify({
        type: "negative",
        message: `${rejectedEntries.length} PDFs konnten nicht geladen werden.`,
      });
    }

    function onUploaded(event: any) {
      if (event.xhr && event.xhr.responseText) {
        try {
          const response = JSON.parse(event.xhr.responseText);
          $q.dialog({
            component: DialogComponent,
            componentProps: {
              response: [response],
            },
          });
        } catch (error) {
          $q.notify({
            type: "negative",
            message: "Fehler beim verarbeiten der Antwort.",
          });
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
<style lang="scss" scoped>
::v-deep .q-icon {
  color: white !important;
}
.uploader {
  margin: 0;
  width: 420px;
  height: 200px;
  border-radius: 15px;
}
.custom {
  padding-left: 0;
}
.q-uploader__header {
  background-color: $card-background !important;
}
</style>
