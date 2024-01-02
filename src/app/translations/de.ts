import {translation} from './index';
require('dayjs/locale/de');

export const german: translation = {
  loading: 'Laden',
  Success: 'Erfolg',
  OK: 'OK',
  forgotPassword: 'Passwort vergessen?',
  createAnAccount: 'Registrieren',
  phoneSignIn: 'Mit Telefonnummer anmelden',
  phoneSignInTitle: 'Anmeldung per Telefon',
  signIn: 'Anmelden',

  home: 'Startseite',
  welcome: 'Willkommen bei MyMenü',
  NotFound: 'Seite nicht gefunden',
  PageNotFoundText: 'Oh, nein! Diese Seite existiert nicht.',
  gettingStarted: 'Home',
  userInfo: 'Profil',
  settings: 'Einstellungen',

  // forms

  formClose: 'Schliessen',

  // shopping
  shopping: 'Einkaufszettel',
  shoppingList: 'Meine Einkaufszettel',
  goToShopping: 'Einkaufszettel bearbeiten',
  scanList: 'Einkaufszettel einscannen',
  menuProposals: "Menü Vorschläge",
  goToWine: 'Weine',
  createItems: 'Hinzufügen',
  sendItems: 'Absenden',
  deleteSelectedItems: 'Löschen',

  // article
  article: 'Artikel',
  articleList: 'Meine Artikelliste',
  createMenu: 'Menü zusammenstellen',

  // wine
  wine: 'Wein',

  createAccountError: 'Kontoerstellung - Fehler',
  createAccountPasswordsDifferent: 'Passwörter stimmen nicht überein',
  createAccountInstructions:
    'Erstellen Sie ein Konto mit Ihrer E-Mail und Ihrem Passwort. Nach der Erstellung werden Sie automatisch in Ihrem Profil angemeldet.',
  emailLabel: 'E-Mail-Adresse',
  passwordLabel: 'Passwort',
  createAccountPasswordConfirmLabel: 'Passwort bestätigen',
  passwordsDoNotMatch: 'Passwörter stimmen nicht überein',
  createAccountCreating: 'Konto wird erstellt',
  createAccountCreate: 'Konto erstellen',

  forgotPasswordInstructions:
    'Geben Sie unten Ihre E-Mail-Adresse ein, um eine Passwort-Zurücksetzen-E-Mail zu senden:',
  forgotPasswordLabel: 'E-Mail-Adresse',
  forgotPasswordError: 'Passwort vergessen - Fehler',
  forgotPasswordSending: 'Passwort-Zurücksetzen-E-Mail wird gesendet',
  forgotPasswordSend: 'Passwort-Zurücksetzen-E-Mail senden',

  phoneVerificationCode: 'Bestätigungscode',
  phoneVerificationConfirm: 'Bestätigen',
  phoneVerificationCountryInstructions:
    'Tippen Sie, um das Land der Telefonnummer auszuwählen',
  phoneVerificationNumberInstructions: 'Geben Sie Ihre Telefonnummer ein:',
  phoneVerificationNumberLabel: 'Telefonnummer',
  phoneVerificationNumberSubmit: 'Einreichen',

  signInSigningIn: 'Anmeldung läuft',
  signInSignIn: 'Anmelden',

  profileLastSignIn: 'Letzte Anmeldung',

  userUpdateError: 'Benutzeraktualisierung - Fehler',
  userEmailVerify: 'Erneut senden',
  userEmailVerification: 'Verifizierung',
  userEmailVerificationInstructions1:
    'Eine Verifizierungsmail wurde gesendet an',
  userEmailVerificationInstructions2:
    'Bitte befolgen Sie die Anweisungen, um Ihre E-Mail-Adresse zu verifizieren.',
  userEmailVerificationBanner:
    'Bitte verifizieren Sie Ihre E-Mail-Adresse, um die vollen Funktionen dieser App zu nutzen. Klicken Sie auf die Schaltfläche "Erneut senden" unten, um die Verifizierungsmail erneut zu senden. Wenn Sie bereits verifiziert haben, drücken Sie erneut verifizieren, um Ihren Status hier zu aktualisieren.',
  userEmailVerifyTitle: 'E-Mail-Verifizierung',
  userEmailVerificationSuccess:
    'Sie haben Ihre E-Mail-Adresse erfolgreich verifiziert.',
  userEmailVerificationFailure:
    'Es scheint, dass Ihre E-Mail immer noch nicht verifiziert ist. Versuchen Sie, die Verifizierungsmail erneut zu senden und befolgen Sie die Anweisungen in der E-Mail.',
  userEmailVerificationVerifyButton: 'Erneut verifizieren',
  userDisplayLabel: 'Anzeigeeinstellungen:',
  userNameDisplayLabel: 'Anzeigename',
  userNameDisplayInstructions:
    'Legen Sie einen benutzerdefinierten Anzeigenamen fest, um eine persönliche Begrüßung zu erhalten.',
  userNameDisplaySave: 'Speichern',
  userNameDisplayUpdatedTitle: 'Anzeigename geändert',
  userNameDisplayUpdateMessage: 'Ihr Anzeigename wurde erfolgreich geändert.',
  userPasswordUpdateLabel: 'Passwortaktualisierung:',
  userPasswordInstructions:
    'Aktualisieren Sie Ihr Kontopasswort. Aus Sicherheitsgründen geben Sie bitte Ihr aktuelles Kontopasswort ein.',
  userPasswordCurrent: 'Aktuelles Passwort',
  userPasswordNew: 'Neues Passwort',
  userPasswordConfirm: 'Neues Passwort bestätigen',
  userPasswordUpdate: 'Aktualisieren',
  userSignOut: 'Abmelden',

  // Google-Auth-Nachrichten
  googleAuthErrorTitle: 'Google-Authentifizierungsfehler',
  googleAuthCancelled: 'Google-Authentifizierung abgebrochen.',
  googleAuthInProgress: 'Google-Authentifizierung läuft bereits.',
  googleAuthPlayServices:
    'Für die Google-Authentifizierung sind Google Play-Dienste erforderlich.',
  googleAuthConfigError:
    'Die Google-Authentifizierung ist für diese Anwendung nicht korrekt konfiguriert.',
  // TODO Übersetzung des Katalogs von Google-Fehlermeldungen

  // Facebook-Auth-Nachrichten
  facebookAuthErrorTitle: 'Facebook-Authentifizierungsfehler',
  facebookAuthCancelled: 'Facebook-Authentifizierung abgebrochen.',
  facebookAuthErrorMessage:
    'Wir haben keinen Zugriffstoken von Facebook erhalten.',
  // TODO Übersetzung des Katalogs von Facebook-Fehlermeldungen

  // Apple-Auth-Nachrichten
  appleAuthErrorTitle: 'Apple-Authentifizierungsfehler',
  appleAuthErrorMessage:
    'Es war nicht möglich, ein Identitäts-Token von Apple zu erhalten.',
  // TODO Übersetzung des Katalogs von Apple-Auth-Fehlercodes

  // Firebase-Auth-Fehlermeldungen - aus dem Firebase-Fehlerkatalog
  unknownError:
    'Ein unerwarteter Fehler ist aufgetreten. Bitte versuchen Sie es erneut.',
  //... and so on for the remaining Firebase auth error messages
};
