# 🛸 mymenu

MyMenü ist eine innovative Lösung für die alltägliche Essensplanung. Durch die Kombination moderner Technologien wie React-Native, Cloud Services und künstlicher Intelligenz, ermöglichst es dem Benutzer ihren Coop-Einkaufsbeleg einfach und effizient in einen wöchentlichen Speiseplan umzuwandeln.

## Funktionen

1. Einkaufsbeleg-Scannen: Registrierte Benutzer können ihren Coop Einkaufsbeleg über ihr Mobilgerät einscannen. Die Applikation unterstützt die Einlesung von Belegen als Bild- oder PDF-Datei.
  <img src="https://github.com/nile4000/mymenu-reactnative/assets/61655582/fb7e3738-6fcf-4c77-803e-d20a74bfeffc" width="300">

3. Textextraktion: Nach dem Einlesen wird der Beleg an eine Middleware gesendet, die für die Textextraktion und -aufbereitung zuständig ist. Diese Konversionsschicht stellt sicher, dass die Daten sparsam und in einem für die weitere Verarbeitung geeigneten Format vorliegen.

4. Datenverarbeitung mit chatGPT: Die aufbereiteten Daten werden an ChatGPT übermittelt, wo sie strukturiert und in ein JSON-Format umgewandelt werden. Dies ermöglicht eine präzise und nutzerfreundliche Darstellung der Einkaufsdaten.

5. Frontend-Interaktion: Im Frontend der Applikation können Nutzer die verarbeiteten Daten einsehen und bei Bedarf bearbeiten. Dieser Schritt ist entscheidend, um die Genauigkeit und Relevanz der Menüvorschläge zu gewährleisten.

  <img src="https://github.com/nile4000/mymenu-reactnative/assets/61655582/8fb044fe-ae8e-4ba2-8617-f7b888cc7b77" width="300">

7. Menüzusammenstellung: Nach der Bereinigung der Daten können Benutzer per chatGPT, Menüvorschläge für den Beleg zusammenstellen lassen. Diese Funktion nutzt die bearbeiteten Daten, um kreative und abwechslungsreiche Speisepläne zu erstellen.

  <img src="https://github.com/nile4000/mymenu-reactnative/assets/61655582/cd392cdb-b41c-4851-bf50-f9f65a060ec6" width="300">

## ⭐ Features
- [React Native Web](https://necolas.github.io/react-native-web/)
- [React Navigation](https://reactnavigation.org/)
- [React Native Paper](https://callstack.github.io/react-native-paper/)
- [React Native Firebase Authentication](https://rnfirebase.io)
- [Supabase.js](https://supabase.io/docs/reference/javascript/supabase-client)

## Running the app

### Web

#### Development

##### Prerequisites Web
Node.js: 16.17.0
Yarn: 1.22.10

(initial) Install the app: `yarn install`
Run the app: `yarn web`
Open [http://localhost:3000](http://localhost:3000) to view it in the browser.

The page will reload if you make edits.
You will also see any lint errors in the console.

#### Production

Build the app: `yarn build-web`
Use the app `npx serve -s build/`

### Native

### Prerequisites Android
SDK-Version: 32
Build-Tools: 32.0.0
Android-Emulator: Pixel 3a API 32

1. Start Metro Bundler: `yarn start`
2. Start the Android app: `yarn android`
3. Start the iOS app: `yarn ios` (make sure you have installed pods first! `yarn ios:pods` if needed)

## Development Tools

1. Check your code style with `yarn lint:all` (runs eslint, prettier, and tsc)
1. Check your code correctness with `yarn test:all` (runs jest)

## Support
If you have any questions or issues, please contact nile4000.

PRs welcome to these repositories to add react-native-web support! :pray:

- [React Native Facebook SDK](https://github.com/thebergamo/react-native-fbsdk-next#readme)
- [React Native Google Signin](https://github.com/react-native-google-signin/google-signin#readme)
- [React Native Apple Authentication](https://github.com/invertase/react-native-apple-authentication#readme)
