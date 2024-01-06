const {
  addBeforeLoader,
  loaderByName,
  when,
  whenDev,
  whenProd,
  whenTest,
  ESLINT_MODES,
  POSTCSS_MODES,
} = require('@craco/craco');
const path = require('path');

module.exports = {
  babel: {
    presets: [
      // The 'metro-react-native-babel-preset' preset is recommended to match React Native's packager
      [
        'module:metro-react-native-babel-preset',
        {useTransformReactJSXExperimental: true},
      ],
    ],

    plugins: [
      // https://necolas.github.io/react-native-web/docs/setup/#package-optimization
      'react-native-web',
      [
        // Enable new JSX Transform from React
        '@babel/plugin-transform-react-jsx',
        {
          runtime: 'automatic',
        },
      ],
      ['@babel/plugin-proposal-decorators', {legacy: true}],
      ['@babel/plugin-proposal-class-properties', {loose: true}],
      ['@babel/plugin-proposal-private-methods', {loose: true}],
      ['@babel/plugin-proposal-private-property-in-object', {loose: true}],
    ],
    loaderOptions: {
      /* Any babel-loader configuration options: https://github.com/babel/babel-loader. */
    },
    // loaderOptions: (babelLoaderOptions, {env, paths}) => {
    //   return babelLoaderOptions;
    // }
  },
  // typescript: {
  //   enableTypeChecking: true /* (default value)  */
  // },
  webpack: {
    alias: {
      ['@invertase/react-native-apple-authentication$']: path.resolve(
        __dirname,
        'src/shims/react-native-apple-authentication-web.ts',
      ),
      ['@react-native-firebase/app$']: path.resolve(
        __dirname,
        'src/shims/firebase-app-web.ts',
      ),
      ['@react-native-firebase/analytics$']: path.resolve(
        __dirname,
        'src/shims/firebase-analytics-web.ts',
      ),
      ['@react-native-firebase/auth$']: path.resolve(
        __dirname,
        'src/shims/firebase-auth-web.ts',
      ),
      ['@react-native-firebase/firestore$']: path.resolve(
        __dirname,
        'src/shims/firebase-firestore-web.ts',
      ),
      ['@react-native-google-signin/google-signin$']: path.resolve(
        __dirname,
        'src/shims/google-signin-web.ts',
      ),
      ['react-native-fbsdk-next$']: path.resolve(
        __dirname,
        'src/shims/react-native-fbsdk-next-web.ts',
      ),
    },
    plugins: {
      add: [],
      remove:
        [] /* An array of plugin constructor's names (i.e. "StyleLintPlugin", "ESLintWebpackPlugin" ) */,
    },
    configure: {
      /* Any webpack configuration options: https://webpack.js.org/configuration */
    },
    configure: (webpackConfig, {env, paths}) => {
      const imageLoader = {
        test: /\.(gif|jpe?g|png|svg)$/,
        use: {
          loader: 'url-loader',
          options: {
            name: '[name].[ext]',
            esModule: false,
          },
        },
      };

      addBeforeLoader(webpackConfig, loaderByName('url-loader'), imageLoader);

      return webpackConfig;
    },
  },
  plugins: [
    {
      plugin: {
        // overrideCracoConfig: ({cracoConfig, pluginOptions, context: {env, paths}}) => {
        //   return cracoConfig;
        // },
        overrideWebpackConfig: ({
          webpackConfig,
          cracoConfig,
          pluginOptions,
          context: {env, paths},
        }) => {
          return webpackConfig;
        },
      },
      // options: {}
    },
  ],
};
