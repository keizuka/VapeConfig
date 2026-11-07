# VapeConfig

Application Android en français pour gérer son matériel de vape et ses recettes. Les données personnelles restent sur l’appareil; le catalogue partagé public est fourni par le dépôt.

## Fonctions

- Catalogue partagé consultable et fiches matérielles locales modifiables/supprimables.
- Comparaison de texte des connecteurs pour signaler seulement s’ils concordent; cela ne constitue pas une vérification de compatibilité complète.
- Profils de batteries intégrées et amovibles, avec références fabricant saisies par l’utilisateur.
- Estimateur théorique de résistance pour fil rond (Kanthal A-1, Nichrome 80 et acier 316L) et fiches reconstructibles enregistrées.
- Calculateur volumétrique de recette selon volume cible, nicotine, concentration de base, arômes, ratio PG/VG et supports des ingrédients.
- Vérification des GitHub Releases publiques et lien de téléchargement manuel de l’APK.
- Interface Material 3 sombre, français par défaut et ressources anglaises.

## Modules

- `app`: démarrage, thème, navigation et tableau de bord.
- `core:database`: stockage Room/SQLite et migration de schéma.
- `core:calculator`: estimateurs de résistance et de mélanges.
- `core:compatibility`: comparaison limitée des connecteurs déclarés.
- `core:updater`: consultation des GitHub Releases publiques.
- `feature:catalog`, `feature:batteries`, `feature:configurator`, `feature:liquids`, `feature:settings`.

## Compilation

Avec JDK 17 et Android SDK 36 :

```sh
gradle assembleDebug
```

GitHub Actions compile l’APK pour chaque push sur `main`. Une release n’est publiée qu’après déclenchement manuel du workflow avec un numéro de version.

## Versioning

Suivre Semantic Versioning avec des tags `vMAJEUR.MINEUR.CORRECTIF`. Augmenter `versionCode` Android pour chaque version et aligner `versionName` sur le tag sans `v`.

## Limites et sécurité

L’estimation reconstructible est théorique, pour des coils simples en fil rond; elle ne tient pas compte de toutes les variations réelles et ne certifie jamais la sécurité. Mesurez le montage et vérifiez indépendamment les accus, le matériel et leurs limites.

La comparaison du catalogue porte uniquement sur le texte des connecteurs renseignés. Elle ne vérifie ni les dimensions, ni le dégagement, ni l’électronique, ni la sécurité.

Les recettes sont des calculs de volumes fondés sur les concentrations et supports indiqués. Vérifiez les étiquettes et les consignes officielles des produits, notamment pour la nicotine. Les profils de batteries sont des notes personnelles, pas une certification ni une recommandation de modèle.

La documentation des formules et des références de résistivité se trouve dans [CALCULATIONS.md](docs/CALCULATIONS.md).

La release est une APK debug non signée avec une clé de production. Une signature dédiée et les vérifications requises sont nécessaires avant une diffusion de production.
