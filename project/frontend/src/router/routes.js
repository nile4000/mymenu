const routes = [
  {
    path: "/",
    component: () => import("layouts/MainLayout.vue"),
    meta: { requiresAuth: false },
    children: [
      {
        path: "",
        component: () => import("pages/Index.vue"),
        meta: { requiresAuth: false },
      },
      {
        path: "scanner",
        component: () => import("pages/Scanner.vue"),
        meta: { requiresAuth: false },
      },
      {
        path: "receipt",
        component: () => import("pages/receipt/ReceiptTable.vue"),
        meta: { requiresAuth: false },
      },
      {
        path: "recipe",
        component: () => import("pages/recipe/RecipeTable.vue"),
        meta: { requiresAuth: false },
        children: [
          {
            path: ":id",
            component: () => import("pages/recipe/RecipeDetail.vue"),
            meta: { requiresAuth: false },
          },
        ],
      },
      {
        path: "food",
        component: () => import("pages/article/FoodTable.vue"),
        meta: { requiresAuth: false },
      },
    ],
  },
  {
    path: "/auth",
    component: () => import("layouts/AuthLayout.vue"),
    children: [
      { path: "/auth/login", component: () => import("pages/Auth.vue") },
    ],
  },
  // Always leave this as last one,
  // but you can also remove it
  {
    path: "/:catchAll(.*)*",
    component: () => import("pages/ErrorDefault.vue"),
  },
];

export default routes;
