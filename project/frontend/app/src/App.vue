<template>
  <router-view />
</template>
<script>
import { defineComponent, onMounted } from "vue";

export default defineComponent({
  name: "App",
  setup() {
    // initialize dummy data
    onMounted(() => {
      const item1 = {
        UID: "c32c41a0-eb88-4f66-bb7c-bb782bf9e85d",
        PurchaseDate: "16.03.22",
        Corp: "Coop",
        Total: "4.60",
        Articles: [
          {
            Name: "QP Früchtequark Erdbeer 2x125g",
            Price: 1.2,
            Quantity: 1,
            Discount: 0,
          },
          {
            Name: "Primagusto Erdbeeren 250g Imp",
            Price: 3.5,
            Quantity: 1,
            Discount: 1.75,
          },
          {
            Name: "Heidelbeeren 250g Import",
            Price: 3.3,
            Quantity: 1,
            Discount: 1.65,
          },
        ],
      };
      const item2 = {
        UID: "f0c7e8c3-8330-4d92-899f-04b3ddbf01a9",
        PurchaseDate: "20.01.22",
        Corp: "Coop",
        Total: "10.65",
        Articles: [
          {
            Name: "Oriental Bowl HG BE 290G",
            Price: 9.95,
            Quantity: 1,
            Discount: 0,
          },
          {
            Name: "Balsamico Dressing 60ML",
            Price: 0.7,
            Quantity: 1,
            Discount: 0,
          },
          {
            Name: "Naturaplan Bio Tafel Crémant 100G",
            Price: 1.95,
            Quantity: 1,
            Discount: 1.95,
          },
        ],
      };

      sessionStorage.setItem(
        "rec_c32c41a0-eb88-4f66-bb7c-bb782bf9e85d",
        JSON.stringify(item2)
      );
      sessionStorage.setItem(
        "rec_f0c7e8c3-8330-4d92-899f-04b3ddbf01a9",
        JSON.stringify(item1)
      );
    });
  },
});
</script>
