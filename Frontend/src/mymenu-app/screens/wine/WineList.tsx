import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import {Button, useTheme} from 'react-native-paper';

const WineList = () => {
  const theme = useTheme();
  return (
    <View style={styles.container}>
      <Text>Wine List Screen</Text>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});

export default WineList;
