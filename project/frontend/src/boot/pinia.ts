import { createPinia } from "pinia";
import { boot } from "quasar/wrappers";

const pinia = createPinia();

export default boot(({ app }) => {
  app.use(pinia);
});

export { pinia };
