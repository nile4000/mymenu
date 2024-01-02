/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * Generated with the TypeScript template
 * https://github.com/react-native-community/react-native-template-typescript
 *
 * @format
 */

import appJson from '../app.json';
import {ScrollView, StyleSheet, Text, useColorScheme, View} from 'react-native';
import {Button, Headline, useTheme} from 'react-native-paper';
import {
  SafeAreaProvider,
  useSafeAreaInsets,
} from 'react-native-safe-area-context';
import {createMaterialTopTabNavigator} from '@react-navigation/material-top-tabs';
import {useAppSettings} from '../app/components/AppSettings';
import {NavigationContainer, useLinkTo} from '@react-navigation/native';

export const Home = () => {
  const linkTo = useLinkTo();
  const appSettings = useAppSettings();

  const backgroundStyle = {};

  return (
    <ScrollView
      contentInsetAdjustmentBehavior="automatic"
      style={backgroundStyle}>
      <View>
        <Headline
          style={[
            styles.padded,
            {color: appSettings.currentTheme.colors.text},
          ]}>
          {appSettings.t('welcome')}
        </Headline>
        <View style={[backgroundStyle, styles.detailsContainer]}>
          <Button
            onPress={() => linkTo('/shopping-list')}
            style={styles.button}
            icon="pencil">
            {appSettings.t('goToShopping')}
          </Button>
        </View>
        <View style={[backgroundStyle, styles.detailsContainer]}>
          <Button
            onPress={() => selectPDF()}
            style={styles.button}
            icon="file-upload">
            {appSettings.t('scanList')}
          </Button>
        </View>
        <View style={[backgroundStyle, styles.detailsContainer]}>
          <Button onPress={() => selectPDF()} style={styles.button}>
            {appSettings.t('menuProposals')}
          </Button>
        </View>
      </View>
    </ScrollView>
  );
};

// *****************************************************************************************************
// The rest of the file is to set up a react-navigation and react-native-vector-icons demonstraiton:
const Tab = createMaterialTopTabNavigator();

// import FilePickerManager from 'react-native-file-picker';

const selectPDF = async () => {
  try {
    // FilePickerManager.showFilePicker(response => {
    //   console.log('Response = ', response);
    //   if (response.didCancel) {
    //     console.log('User cancelled file picker');
    //   } else if (response.error) {
    //     console.log('FilePickerManager Error: ', response.error);
    //   } else {
    //     // this.setState({
    //     //   file: response,
    //     // });
    //   }
    // });
  } catch (err) {}
};

const TopTabNavigator = () => {
  const insets = useSafeAreaInsets();

  // Dark mode theming items
  const isDarkMode = useColorScheme() === 'dark';

  const screenOptions = {
    tabBarStyle: {
      paddingTop: insets.top,
    },
  };

  return (
    <Tab.Navigator initialRouteName="Home" screenOptions={screenOptions}>
      <Tab.Screen component={Home} key={'Home'} name={'Home'} />
    </Tab.Navigator>
  );
};

const TabbedApp = () => {
  return (
    <SafeAreaProvider>
      <NavigationContainer
        linking={{
          prefixes: ['localhost'],
          config: {
            screens: {
              Details: 'details',
              Linking: 'linking',
              Home: '*', // Fall back to if no routes match
            },
          },
        }}
        documentTitle={{
          formatter: (options, route) =>
            `${appJson.displayName} - ${options?.title ?? route?.name}`,
        }}>
        <TopTabNavigator />
      </NavigationContainer>
    </SafeAreaProvider>
  );
};

const styles = StyleSheet.create({
  detailsContainer: {
    flex: 1,
    alignContent: 'center',
    justifyContent: 'center',
    alignItems: 'center',
    padding: 5,
  },
  button: {
    marginVertical: 5,
    width: 300,
  },
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: '600',
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '400',
  },
  highlight: {
    fontWeight: '700',
  },
  padded: {
    paddingLeft: 10,
    paddingBottom: 40,
    paddingTop: 20,
  },
});

export default TabbedApp;
