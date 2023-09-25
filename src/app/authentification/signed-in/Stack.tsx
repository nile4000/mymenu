import {createMaterialTopTabNavigator} from '@react-navigation/material-top-tabs';
import {createStackNavigator} from '@react-navigation/stack';
import {useAppSettings} from '../../components/AppSettings';
import {NotFound} from '../../components/NotFound';
import {useSafeAreaInsets} from 'react-native-safe-area-context';
import {Home as GettingStarted} from '../../../mymenu-app/Home';
import Profile from './Profile';
import Settings from './Settings';

import ShoppingList from '../../../mymenu-app/screens/shopping/ShoppingList';
import WineList from 'mymenu-app/screens/wine/WineList';

const Stack = createStackNavigator();
const TopTabs = createMaterialTopTabNavigator();

const ProfileStack = () => {
  const appSettings = useAppSettings();
  return (
    <Stack.Navigator initialRouteName="UserProfile">
      <Stack.Screen
        name="UserProfile"
        component={Profile}
        options={{headerShown: false}}
      />
      <Stack.Screen
        name="UserSettings"
        options={{title: appSettings.t('settings')}}
        component={Settings}
      />
      <Stack.Screen
        name="ShoppingList"
        component={ShoppingList}
        options={{headerShown: false}}
      />
      <Stack.Screen
        name="WineList"
        component={WineList}
        options={{headerShown: false}}
      />
      <Stack.Screen
        name="NotFound"
        component={NotFound}
        options={{title: appSettings.t('NotFound')}}
      />
    </Stack.Navigator>
  );
};

const SignedIn = () => {
  // Used for status bar layout in react-navigation
  const insets = useSafeAreaInsets();
  const appSettings = useAppSettings();

  const screenOptions = {
    tabBarStyle: {
      paddingTop: insets.top,
    },
  };

  return (
    <TopTabs.Navigator initialRouteName="Home" screenOptions={screenOptions}>
      <TopTabs.Screen
        name="Home"
        options={{title: appSettings.t('gettingStarted')}}
        component={GettingStarted}
      />
      <TopTabs.Screen
        name="WineList"
        options={{title: appSettings.t('wine')}}
        component={WineList}
      />
      <TopTabs.Screen
        name="ShoppingList"
        options={{title: appSettings.t('shopping')}}
        component={ShoppingList}
      />
      <TopTabs.Screen
        name="User"
        options={{title: appSettings.t('userInfo')}}
        component={ProfileStack}
      />
    </TopTabs.Navigator>
  );
};

export default SignedIn;
