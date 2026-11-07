# VapeConfig

Application Android de configuration et de suivi pour le matériel de vape, avec stockage local.

## État du projet

Le dépôt contient le squelette Kotlin/Jetpack Compose/Material 3 et le tableau de bord initial. Les données de catalogue restent locales; la synchronisation distante et les fonctions de calcul détaillées seront développées ensuite.

## Modules

- `app` : démarrage, thème, navigation et tableau de bord.
- `core:database` : persistance Room/SQLite.
- `core:calculator` : domaine des estimations de résistances.
- `core:compatibility` : règles de compatibilité entre éléments.
- `core:updater` : consultation des GitHub Releases publiques.
- `feature:catalog` : catalogue mixte de matériel.
- `feature:configurator` : résistances préfabriquées et reconstructibles.
- `feature:liquids` : recettes et liquides.
- `feature:settings` : réglages et vérification des mises à jour.

## Prérequis et compilation

Android Studio récent avec JDK 17 et Android SDK 35. Pour compiler l’APK debug après installation de Gradle :

```sh
gradle assembleDebug
```

Le workflow GitHub Actions compile l’APK à chaque push et pull request vers `main`, puis le conserve comme artefact. Il ne crée ni tag ni release.

## Versioning

Suivre [Semantic Versioning](https://semver.org/), avec des tags `vMAJEUR.MINEUR.CORRECTIF` (par exemple `v0.1.0`). Le `versionCode` Android doit augmenter à chaque publication; `versionName` reprend le tag sans le préfixe `v`. Les releases et leurs APK seront publiés manuellement après validation.

## Données et sécurité

Les données personnelles et configurations sont conservées localement dans SQLite via Room. La vérification des mises à jour consulte les métadonnées publiques de `Keizuka/VapeConfig`; aucune release n’est publiée automatiquement.

Le calcul reconstructible fournit uniquement une **estimation**. Il ne détermine pas si un montage est sûr. Vérifiez indépendamment la résistance réelle, l’état et les limites des accus, les spécifications du matériel et les recommandations applicables avant toute utilisation. Ne vous fiez jamais au seul résultat du calculateur.

## Langues

Le français est la langue initiale. Les ressources Android sont organisées par locale afin d’ajouter d’autres traductions (`values-en`, etc.) sans coder les textes directement dans les écrans.
