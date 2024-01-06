import {createStackNavigator} from '@react-navigation/stack';
import {useAppSettings} from '../../../app/components/AppSettings';

import ShoppingList from './ShoppingList';

// const Stack = createStackNavigator();

const ShoppingStack = () => {
  const appSettings = useAppSettings();
  // return (
    // <Stack.Navigator initialRouteName="ShoppingList">
    //   <Stack.Screen
    //     name="ShoppingList"
    //     component={ShoppingList} // Assuming you've defined this screen somewhere
    //     options={{headerShown: false}} // Adjust options as per your requirement
    //   />
    //   {/* You can add more screens related to ShoppingList here */}
    //   {/* <Stack.Screen
    //       name="ShoppingListDetail"
    //       component={ShoppingListDetail} 
    //       options={{title: 'Item Detail'}}
    //     /> */}
    // </Stack.Navigator>
  // );
};

export default ShoppingStack;
