# Modèles de calcul de VapeConfig

## Résistance théorique d’un coil en fil rond

Le calcul utilise la relation R = ρ × L / A, avec une résistivité indicative à 20 °C, une section circulaire et une longueur calculée à partir du diamètre intérieur, du diamètre du fil, du nombre de spires et des deux pattes. Pour plusieurs coils identiques en parallèle, la résistance calculée d’un coil est divisée par leur nombre.

Valeurs initiales de résistivité :
- Kanthal A-1 : 1,45 Ω·mm²/m selon la fiche technique Kanthal.
- Nichrome 80 / Nikrothal 80 : 1,09 Ω·mm²/m selon la documentation Kanthal.
- Acier inoxydable 316L : 0,75 Ω·mm²/m selon la fiche de données thyssenkrupp Materials.

Les valeurs réelles dépendent du lot, des dimensions et de la température. Ce modèle ne couvre pas les fils complexes, les variations de géométrie, les connexions, les tolérances ou les erreurs de mesure. Le résultat est une estimation seulement. Il ne détermine pas si un montage est sûr et ne remplace ni une mesure réelle ni la vérification indépendante des accus et de l’appareil.

Sources fabricant :
- https://www.kanthal.com/en/products/datasheets/material-datasheets/wire/resistance-heating-wire-and-resistance-wire/kanthal-a-1/
- https://www.kanthal.com/en/products/datasheets/material-datasheets/wire/resistance-heating-wire-and-resistance-wire/nikrothal-80/
- https://www.thyssenkrupp-materials.co.uk/stainless-steel-316l-14404.html

## Calculateur de mélange

Le volume de base nicotinée suit le rapport concentration cible / concentration de la base. Le volume d’arôme suit le pourcentage renseigné. Les apports PG et VG des bases sont soustraits du ratio final demandé. Le calcul suppose que les supports des bases et arômes saisis sont exacts; il ne valide pas les étiquettes ni les concentrations.

## Compatibilité

Le contrôle présent compare uniquement deux chaînes de caractères de connecteur normalisées. Un résultat identique signifie seulement que les textes concordent. Il ne vérifie pas les dimensions, dégagements, plages électriques, compatibilité de l’appareil ou sécurité.
