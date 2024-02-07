# 🛸 mymenu

MyMenü ist eine innovative Lösung für die alltägliche Essensplanung. Durch die Kombination moderner Technologien wie Quasar, Cloud Services und künstlicher Intelligenz, ermöglichst es dem Benutzer ihren Coop-Einkaufsbeleg einfach und effizient in einen wöchentlichen Speiseplan umzuwandeln.

## Funktionen

1. Einkaufsbeleg-Scannen: Registrierte Benutzer können ihren Coop Einkaufsbeleg über ihr Mobilgerät einscannen. Die Applikation unterstützt die Einlesung von Belegen als Bild- oder PDF-Datei.

   <img src="https://github.com/nile4000/mymenu-reactnative/assets/61655582/fb7e3738-6fcf-4c77-803e-d20a74bfeffc" width="300">

2. Textextraktion: Nach dem Einlesen wird der Beleg an eine Middleware gesendet, die für die Textextraktion und -aufbereitung zuständig ist. Diese Konversionsschicht stellt sicher, dass die Daten sparsam und in einem für die weitere Verarbeitung geeigneten Format vorliegen.

3. Datenverarbeitung mit chatGPT: Die aufbereiteten Daten werden an ChatGPT übermittelt, wo sie strukturiert und in ein JSON-Format umgewandelt werden. Dies ermöglicht eine präzise und nutzerfreundliche Darstellung der Einkaufsdaten.

4. Frontend-Interaktion: Im Frontend der Applikation können Nutzer die verarbeiteten Daten einsehen und bei Bedarf bearbeiten. Dieser Schritt ist entscheidend, um die Genauigkeit und Relevanz der Menüvorschläge zu gewährleisten.

  <img src="https://github.com/nile4000/mymenu-reactnative/assets/61655582/8fb044fe-ae8e-4ba2-8617-f7b888cc7b77" width="300">

5. Menüzusammenstellung: Nach der Bereinigung der Daten können Benutzer per chatGPT, Menüvorschläge für den Beleg zusammenstellen lassen. Diese Funktion nutzt die bearbeiteten Daten, um kreative und abwechslungsreiche Speisepläne zu erstellen.

  <img src="https://github.com/nile4000/mymenu-reactnative/assets/61655582/cd392cdb-b41c-4851-bf50-f9f65a060ec6" width="300">
