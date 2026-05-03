import ROUTE_PATHS from "./paths";

const routes = [
  {
    path: ROUTE_PATHS.home,
    component: () => import("layouts/MainLayout.vue"),
    meta: { requiresAuth: false },
    children: [
      {
        path: "",
        component: () => import("pages/Index.vue"),
        meta: { requiresAuth: false },
      },
      {
        path: ROUTE_PATHS.scanner.slice(1),
        redirect: ROUTE_PATHS.receipt,
        meta: { requiresAuth: false },
      },
      {
        path: ROUTE_PATHS.receipt.slice(1),
        component: () => import("pages/receipt/ReceiptTable.vue"),
        meta: { requiresAuth: false },
      },
      {
        path: ROUTE_PATHS.recipe.slice(1),
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
        path: ROUTE_PATHS.article.slice(1),
        component: () => import("pages/article/ArticleTable.vue"),
        meta: { requiresAuth: false },
      },
      {
        path: ROUTE_PATHS.foodAlias.slice(1),
        redirect: ROUTE_PATHS.article,
        meta: { requiresAuth: false },
      },
    ],
  },
  {
    path: ROUTE_PATHS.auth,
    component: () => import("layouts/AuthLayout.vue"),
    children: [
      { path: "login", component: () => import("pages/Auth.vue") },
    ],
  },
  // Always leave this as last one,
  // but you can also remove it
  {
    path: "/:catchAll(.*)*",
    component: () => import("pages/common/NotFoundPage.vue"),
  },
];

export default routes;
