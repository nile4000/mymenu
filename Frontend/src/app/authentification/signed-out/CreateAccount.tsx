import {useEffect, useState} from 'react';
import {ScrollView, StyleSheet} from 'react-native';
import {
  Button,
  HelperText,
  Paragraph,
  TextInput,
  useTheme,
} from 'react-native-paper';
import auth, {FirebaseAuthTypes} from '@react-native-firebase/auth';
import {useAlerts} from 'react-native-paper-alerts';

import {useAppSettings} from '../../components/AppSettings';
import {useSupabase} from 'app/App';

function CreateAccount(): JSX.Element {
  const [loading, setLoading] = useState<boolean>(false);
  const [email, setEmail] = useState<string>('');
  const [password, setPassword] = useState<string>('');
  const [confirm, setConfirm] = useState<string>('');
  const Alert = useAlerts();
  const supabase = useSupabase();

  const [help, setHelp] = useState<string>('');
  const theme = useTheme();
  const appSettings = useAppSettings();

  useEffect(() => {
    if (password === confirm) {
      setHelp('');
    } else if (password && confirm && password !== confirm) {
      setHelp(appSettings.t('passwordsDoNotMatch'));
    }
  }, [password, confirm, appSettings]);

  async function handleCreate() {
    try {
      setLoading(true);
      await auth()
        .createUserWithEmailAndPassword(email, password)
        .then(async ({user}) => {
          user.sendEmailVerification();
          if (user) {
            // Speichern des Benutzers in Supabase
            const {error} = await supabase.from('User').insert([
              {
                created_at: user.metadata.creationTime,
                firebase_uid: user.uid,
                firebase_email: user.email,
              },
            ]);
            if (error) {
              Alert.alert(
                appSettings.t('createAccountError'),
                appSettings.t(error.code ?? 'unknownError'),
                [{text: appSettings.t('OK')}],
              );
            } else {
              console.log('Benutzer in Supabase gespeichert!');
            }
          }
        });
    } catch (e) {
      setLoading(false);
      const error = e as FirebaseAuthTypes.PhoneAuthError;
      Alert.alert(
        appSettings.t('createAccountError'),
        appSettings.t(error.code ?? 'unknownError'),
        [{text: appSettings.t('OK')}],
      );
    }
  }

  return (
    <ScrollView
      style={[styles.container, {backgroundColor: theme.colors.background}]}>
      <Paragraph>{appSettings.t('createAccountInstructions')}</Paragraph>
      <TextInput
        style={styles.input}
        mode="outlined"
        label={appSettings.t('emailLabel')}
        value={email}
        onChangeText={setEmail}
        keyboardType="email-address"
        autoCapitalize="none"
        autoCorrect={false}
        autoComplete="email"
        autoFocus={true}
      />
      <TextInput
        secureTextEntry
        style={styles.input}
        mode="outlined"
        label={appSettings.t('passwordLabel')}
        value={password}
        onChangeText={setPassword}
        autoComplete="password"
      />
      <TextInput
        secureTextEntry
        style={styles.input}
        mode="outlined"
        label={appSettings.t('createAccountPasswordConfirmLabel')}
        value={confirm}
        error={!!confirm && password !== confirm}
        onChangeText={setConfirm}
        autoComplete="password"
      />
      <HelperText type="error" visible={!!help}>
        {help}
      </HelperText>
      <Button
        loading={loading}
        mode="contained"
        disabled={!email || !password || !confirm || !!help}
        onPress={() => (loading ? null : handleCreate())}>
        {loading
          ? appSettings.t('createAccountCreating')
          : appSettings.t('createAccountCreate')}
      </Button>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
  },
  input: {
    marginVertical: 10,
  },
});

export default CreateAccount;
