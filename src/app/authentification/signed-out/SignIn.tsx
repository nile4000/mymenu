import {Fragment} from 'react';
import {ScrollView, StyleSheet, View} from 'react-native';
import {Button, useTheme} from 'react-native-paper';
import {useLinkTo} from '@react-navigation/native';
import Hero from '../../components/Hero';
import EmailPassword from '../auth-providers/EmailPassword';
import {useAppSettings} from '../../components/AppSettings';
import logo from '../../../static/assets/img/MyMenu.png';

function SignIn() {
  const theme = useTheme();
  const appSettings = useAppSettings();
  const linkTo = useLinkTo();

  return (
    <Fragment>
      <ScrollView
        style={[styles.container]}>
        <Hero height={300} image={logo}></Hero>
        <EmailPassword />
        <View
          style={[
            styles.divider,
            styles.center,
            ,
          ]}>
          <Button
            color={theme.colors.primary}
            onPress={() => {
              linkTo('/account/password/forgot');
            }}
            style={styles.button}>
            {appSettings.t('forgotPassword')}
          </Button>
          <Button
            mode="contained"
            icon="plus"
            onPress={() => {
              linkTo('/account/create');
            }}
            style={styles.button}>
            {appSettings.t('createAnAccount')}
          </Button>
        </View>

        {/* {Platform.OS !== 'web' && <Facebook />} */}
        {/* {Platform.OS !== 'web' && <Google />} */}
        {/* {Platform.OS !== 'web' && <Apple />} */}
      </ScrollView>
    </Fragment>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  center: {
    alignItems: 'center',
  },
  fab: {
    width: 50,
    height: 50,
    borderRadius: 25,
    alignItems: 'center',
    justifyContent: 'center',
    alignSelf: 'center',
    elevation: 6,
    marginTop: -25,
  },
  button: {
    marginVertical: 5,
    width: 300,
  },
  divider: {
    height: StyleSheet.hairlineWidth,
  },
});

export default SignIn;
