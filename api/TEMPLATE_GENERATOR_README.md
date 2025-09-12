# 🎮 Guide du Générateur de Templates de Jeux

**Guide de référence pour scénaristes et game designers**

Ce document vous explique comment créer des templates de jeux d'escape game urbain et de jeux de piste en utilisant notre système de templates. Aucune connaissance en programmation n'est requise !

## 📋 Table des matières

1. [Vue d'ensemble](#vue-densemble)
2. [Syntaxe de base](#syntaxe-de-base)
3. [Structure d'un template](#structure-dun-template)
4. [Le plateau de jeu (Board)](#le-plateau-de-jeu-board)
5. [Le scénario](#le-scénario)
6. [Les cartes](#les-cartes)
7. [Système de références](#système-de-références)
8. [Exemple complet](#exemple-complet)
9. [Référence rapide](#référence-rapide)

---

## 🎯 Vue d'ensemble

Un **template de jeu** est un fichier texte qui décrit tous les éléments de votre jeu :
- **Le plateau** : les zones géographiques où se déroule votre jeu
- **Le scénario** : les étapes, objectifs et règles du jeu
- **Les cartes** : les images et éléments visuels affichés dans l'application

Le système génère automatiquement tous les fichiers techniques nécessaires à partir de votre description.

## 📝 Syntaxe de base

### Format général
```
CODE:VERSION:TITRE:DUREE
--- Section
------ Élément
--------- Détail
------------ Sous-détail
```

### Règles importantes
- **Les tirets (`-`) définissent la hiérarchie** : plus il y en a, plus l'élément est imbriqué
- **Les paramètres sont séparés par `:` (deux points)**
- **Les langues sont spécifiées avec `FR:` et `EN:`**
- **Les références permettent de lier les éléments** avec `(ref NOM_REFERENCE)`

---

## 🏗️ Structure d'un template

### En-tête obligatoire
```
CODE_JEU:VERSION:TITRE_DU_JEU:DUREE_EN_MINUTES
```

**Exemples :**
```
ESCAPE_URBAIN:1.0:Mystère à Montmartre:120
TRESOR_PARIS:2.1:La Chasse au Trésor:90
ENQUETE:1.5::60
```

**Détails :**
- **CODE_JEU** : identifiant unique (lettres, chiffres, tirets bas)
- **VERSION** : numéro de version (ex: 1.0, 2.1.3)
- **TITRE_DU_JEU** : nom affiché aux joueurs (peut être vide)
- **DUREE_EN_MINUTES** : temps maximum de jeu (optionnel, défaut: 60 minutes)

---

## 🗺️ Le plateau de jeu (Board)

Le plateau définit les **zones géographiques** où se déroule votre jeu.

### Syntaxe
```
--- Board
------ Space:NOM_ZONE:PRIORITE
--------- bottomLeft:X1:Y1:topRight:X2:Y2
--------- X3:Y3:X4:Y4
```

### Priorités disponibles
- `HIGHEST` : priorité maximale
- `HIGH` : priorité élevée  
- `MEDIUM` : priorité moyenne
- `LOW` : priorité faible
- `LOWEST` : priorité minimale

### Exemple pratique
```
--- Board
------ Space:Place de la République:HIGH
--------- bottomLeft:48.8677:2.3631:topRight:48.8687:2.3641
--------- 48.8680:2.3635:48.8684:2.3638
------ Space:Jardin du Luxembourg:MEDIUM
--------- 48.8462:2.3372:48.8482:2.3392
```

**💡 Conseil :** Les coordonnées sont en latitude/longitude. Utilisez Google Maps pour obtenir les coordonnées précises.

---

## 🎭 Le scénario

Le scénario est le cœur de votre jeu. Il se compose d'**étapes** (steps) contenant des **objectifs** (targets) et des **règles** (possibilities).

### Structure d'une étape
```
--- Step:FR:Nom en français:EN:Nom en anglais
------ Target:FR:Objectif en français:EN:Objectif en anglais
------ Possibility
--------- Trigger:TYPE:PARAMETRE
--------- Consequence:TYPE:PARAMETRES
```

### Les objectifs (Targets)

#### Objectif simple
```
------ Target:FR:Trouver l'indice:EN:Find the clue
```

#### Objectif optionnel
```
------ Target (Opt):FR:Bonus secret:EN:Secret bonus
```

#### Objectif avec description
```
------ Target Description (ref MON_OBJECTIF):FR:Résoudre l'énigme:EN:Solve the puzzle
--------- FR:Cette énigme nécessite de la réflexion.
--------- Prenez votre temps.
--------- EN:This puzzle requires thinking.
--------- Take your time.
```

### Les règles (Possibilities)

Les **possibilities** définissent quand et comment les événements se déclenchent.

#### Structure de base
```
------ Possibility
--------- Recurrence:TYPE:NOMBRE
--------- Trigger:TYPE:PARAMETRES
--------- Condition:TYPE:PARAMETRES
--------- Consequence:TYPE:PARAMETRES
```

#### Types de déclencheurs (Triggers)

**1. Entrée dans une zone**
```
--------- Trigger:GOINSPACE:ID_ZONE
```

**2. Sortie d'une zone**
```
--------- Trigger:GOOUTSPACE:ID_ZONE
```

**3. Temps absolu**
```
--------- Trigger:ABSOLUTETIME:45
```
*Se déclenche après 45 minutes de jeu*

**4. Temps relatif**
```
--------- Trigger:RELATIVETIME:ID_AUTRE_POSSIBILITY:15
```
*Se déclenche 15 minutes après qu'une autre règle se soit activée*

**5. Sélection d'option de dialogue**
```
--------- Trigger:SELECTTALKOPTION:ID_OPTION
```

**6. Clic sur élément de carte**
```
--------- Trigger:CLMAPOBJECTICKMAPOBJECT:REFERENCE_ELEMENT
```
*Se déclenche quand le joueur clique sur un élément référencé sur la carte*

#### Types de récurrence (Recurrence)

**1. Toujours actif**
```
--------- Recurrence:ALWAYS
```

**2. Nombre limité d'activations**
```
--------- Recurrence:TIMES:3
```
*Se déclenche maximum 3 fois*

#### Types de conditions (Condition)

**1. Être dans une étape**
```
--------- Condition:INSTEP:ID_STEP
```

**2. Être dans une zone**
```
--------- Condition:INSPACE:ID_ZONE
```

**3. Être en dehors d'une zone**
```
--------- Condition:OUTSIDESPACE:ID_ZONE
```

**4. Objectif dans un état spécifique**
```
--------- Condition:STEPTARGET:ID_TARGET
```
*Se déclenche seulement si l'objectif spécifié est déjà accompli (state: success)*

**5. Logique ET/OU**
```
--------- ConditionType:AND
--------- ConditionType:OR
```

#### Types de conséquences (Consequence)

**1. Activer/désactiver un objectif**
```
--------- Consequence:GoalTarget:targetId:ID_TARGET:state:active
--------- Consequence:GoalTarget:targetId:ID_TARGET:state:success
--------- Consequence:GoalTarget:targetId:ID_TARGET:state:failure
```

**2. Activer/désactiver une étape**
```
--------- Consequence:GoalStep:stepId:ID_STEP:state:active
--------- Consequence:GoalStep:stepId:ID_STEP:state:success
```

**💡 Transition automatique entre chapitres :**
```
--------- Consequence:GoalStep:stepId:CHAPITRE_ACTUEL:state:success
--------- Consequence:GoalStep:stepId:CHAPITRE_SUIVANT:state:active
```
*Permet de terminer un chapitre et d'activer automatiquement le suivant*

**3. Terminer la session**
```
--------- Consequence:SessionEnd:WIN
--------- Consequence:SessionEnd:LOOSE
```

**4. Afficher un message**
```
--------- Consequence:TalkAlert
------------ FR:Félicitations !
------------ Vous avez trouvé l'indice.
------------ EN:Congratulations!
------------ You found the clue.
```

**5. Afficher des choix multiples**
```
--------- Consequence:TalkOptions
------------ Label
--------------- FR:Que voulez-vous faire ?
--------------- EN:What do you want to do ?
------------ Option (ref CHOIX_A)
--------------- FR:Examiner l'objet
--------------- EN:Examine the object
------------ Option (ref CHOIX_B)
--------------- FR:Continuer
--------------- EN:Continue
```

**6. Gestion des objets**
```
--------- Consequence:OBJETADD:ID_OBJET
--------- Consequence:OBJETREMOVE:ID_OBJET
```

**7. Mise à jour des métadonnées**
```
--------- Consequence:UPDATEDMETADATA:SCORE:100
```

---

## 🗺️ Les cartes

Les cartes définissent les éléments visuels affichés dans l'application.

### Structure de base
```
--- Map:Asset:chemin/vers/image.png
------ Priority:HIGH
------ Position:X:Y
--------- Priority:MEDIUM
--------- Space:ID_ZONE1
--------- Space:ID_ZONE2
------ Position (ref REFERENCE):X:Y
--------- Priority:HIGH
```

### Système de coordonnées
**Coordonnées relatives (0-1)** : Les positions utilisent des coordonnées relatives de 0 à 1
- `x: 0` = côté gauche de l'image, `x: 1` = côté droit
- `y: 0` = bas de l'image, `y: 1` = haut de l'image

### Exemple pratique
```
--- Map:Asset:images/plan_bureau.png
------ Priority:HIGH
------ Position:0.4711:0.5176
--------- Priority:HIGHEST
--------- Space:BUREAU_SPACE
------ Position (ref BUREAU_INTERACTIF):0.9158:0.2900
--------- Priority:HIGH
------ Position (ref CASIER):0.4831:0.8541
--------- Priority:MEDIUM
```

**💡 Points cliquables :** Les positions avec références peuvent être utilisées comme déclencheurs avec `Trigger:MAPCLICK:REFERENCE`

---

## 🔗 Système de références

Les références permettent de lier les éléments entre eux et de simplifier l'écriture.

### Créer une référence
```
------ Target (ref MON_TARGET):FR:Objectif important:EN:Important goal
------ Step (ref ETAPE_FINALE):FR:Dernière étape:EN:Final step
```

### Utiliser une référence
```
--------- Consequence:GoalTarget:targetId:MON_TARGET:state:active
--------- Consequence:GoalStep:stepId:ETAPE_FINALE:state:success
```

### 🆕 Déduction automatique de stepId

**Nouveauté !** Vous pouvez maintenant omettre le `stepId` dans les conséquences `GoalTarget`. Le système trouvera automatiquement l'étape qui contient le target.

**Avant :**
```
--------- Consequence:GoalTarget:stepId:ID_STEP:targetId:MON_TARGET:state:active
```

**Maintenant :**
```
--------- Consequence:GoalTarget:targetId:MON_TARGET:state:active
```

### 💡 Exemples pratiques

#### Exemple : Système séquentiel avec conditions
```
--- Step:FR:Étape tutorial:EN:Tutorial step
------ Target (ref ENTER_OFFICE):FR:Entrer dans le bureau:EN:Enter the office  
------ Target (ref SEARCH_DESK):FR:Fouiller le bureau:EN:Search the desk

------ Possibility
--------- Trigger:GOINSPACE:Office
--------- Consequence:GoalTarget:targetId:ENTER_OFFICE:state:success

------ Possibility  
--------- Trigger:CLICKMAPOBJECT:DESK_POSITION
--------- Condition:StepTarget:ENTER_OFFICE
--------- Consequence:GoalTarget:targetId:SEARCH_DESK:state:success
--------- Consequence:Alert
------------ FR:Vous avez fouillé le bureau avec succès !
------------ EN:You searched the desk successfully!
```

**Principe :** Le joueur doit d'abord entrer dans le bureau (premier objectif), puis cliquer sur le bureau sur la carte (second objectif). La condition `StepTarget:ENTER_OFFICE` s'assure que le clic n'est actif que si le premier objectif est déjà accompli.

### 💡 Exemples pratiques

#### Exemple : Système séquentiel avec conditions
```
--- Step:FR:Étape tutorial:EN:Tutorial step
------ Target (ref ENTER_OFFICE):FR:Entrer dans le bureau:EN:Enter the office  
------ Target (ref SEARCH_DESK):FR:Fouiller le bureau:EN:Search the desk

------ Possibility
--------- Trigger:GOINSPACE:Office
--------- Consequence:GoalTarget:targetId:ENTER_OFFICE:state:success

------ Possibility  
--------- Trigger:CLICKMAPOBJECT:DESK_POSITION
--------- Condition:StepTarget:ENTER_OFFICE
--------- Consequence:GoalTarget:targetId:SEARCH_DESK:state:success
--------- Consequence:Alert
------------ FR:Vous avez fouillé le bureau avec succès !
------------ EN:You searched the desk successfully!
```

**Principe :** Le joueur doit d'abord entrer dans le bureau (premier objectif), puis cliquer sur le bureau sur la carte (second objectif). La condition `StepTarget:ENTER_OFFICE` s'assure que le clic n'est actif que si le premier objectif est déjà accompli.

### 💡 Exemples pratiques

#### Exemple : Système séquentiel avec conditions
```
--- Step:FR:Étape tutorial:EN:Tutorial step
------ Target (ref ENTER_OFFICE):FR:Entrer dans le bureau:EN:Enter the office  
------ Target (ref SEARCH_DESK):FR:Fouiller le bureau:EN:Search the desk

------ Possibility
--------- Trigger:GOINSPACE:Office
--------- Consequence:GoalTarget:targetId:ENTER_OFFICE:state:success

------ Possibility  
--------- Trigger:CLICKMAPOBJECT:DESK_POSITION
--------- Condition:StepTarget:ENTER_OFFICE
--------- Consequence:GoalTarget:targetId:SEARCH_DESK:state:success
--------- Consequence:Alert
------------ FR:Vous avez fouillé le bureau avec succès !
------------ EN:You searched the desk successfully!
```

**Principe :** Le joueur doit d'abord entrer dans le bureau (premier objectif), puis cliquer sur le bureau sur la carte (second objectif). La condition `StepTarget:ENTER_OFFICE` s'assure que le clic n'est actif que si le premier objectif est déjà accompli.

### 💡 Exemples pratiques

#### Exemple : Système séquentiel avec conditions
```
--- Step:FR:Étape tutorial:EN:Tutorial step
------ Target (ref ENTER_OFFICE):FR:Entrer dans le bureau:EN:Enter the office  
------ Target (ref SEARCH_DESK):FR:Fouiller le bureau:EN:Search the desk

------ Possibility
--------- Trigger:GOINSPACE:Office
--------- Consequence:GoalTarget:targetId:ENTER_OFFICE:state:success

------ Possibility  
--------- Trigger:CLICKMAPOBJECT:DESK_POSITION
--------- Condition:StepTarget:ENTER_OFFICE
--------- Consequence:GoalTarget:targetId:SEARCH_DESK:state:success
--------- Consequence:Alert
------------ FR:Vous avez fouillé le bureau avec succès !
------------ EN:You searched the desk successfully!
```

**Principe :** Le joueur doit d'abord entrer dans le bureau (premier objectif), puis cliquer sur le bureau sur la carte (second objectif). La condition `StepTarget:ENTER_OFFICE` s'assure que le clic n'est actif que si le premier objectif est déjà accompli.

### 💡 Exemples pratiques

#### Exemple : Système séquentiel avec conditions
```
--- Step:FR:Étape tutorial:EN:Tutorial step
------ Target (ref ENTER_OFFICE):FR:Entrer dans le bureau:EN:Enter the office  
------ Target (ref SEARCH_DESK):FR:Fouiller le bureau:EN:Search the desk

------ Possibility
--------- Trigger:GOINSPACE:Office
--------- Consequence:GoalTarget:targetId:ENTER_OFFICE:state:success

------ Possibility  
--------- Trigger:CLICKMAPOBJECT:DESK_POSITION
--------- Condition:StepTarget:ENTER_OFFICE
--------- Consequence:GoalTarget:targetId:SEARCH_DESK:state:success
--------- Consequence:Alert
------------ FR:Vous avez fouillé le bureau avec succès !
------------ EN:You searched the desk successfully!
```

**Principe :** Le joueur doit d'abord entrer dans le bureau (premier objectif), puis cliquer sur le bureau sur la carte (second objectif). La condition `StepTarget:ENTER_OFFICE` s'assure que le clic n'est actif que si le premier objectif est déjà accompli.

### 💡 Exemples pratiques

#### Exemple : Système séquentiel avec conditions
```
--- Step:FR:Étape tutorial:EN:Tutorial step
------ Target (ref ENTER_OFFICE):FR:Entrer dans le bureau:EN:Enter the office  
------ Target (ref SEARCH_DESK):FR:Fouiller le bureau:EN:Search the desk

------ Possibility
--------- Trigger:GOINSPACE:Office
--------- Consequence:GoalTarget:targetId:ENTER_OFFICE:state:success

------ Possibility  
--------- Trigger:CLICKMAPOBJECT:DESK_POSITION
--------- Condition:StepTarget:ENTER_OFFICE
--------- Consequence:GoalTarget:targetId:SEARCH_DESK:state:success
--------- Consequence:Alert
------------ FR:Vous avez fouillé le bureau avec succès !
------------ EN:You searched the desk successfully!
```

**Principe :** Le joueur doit d'abord entrer dans le bureau (premier objectif), puis cliquer sur le bureau sur la carte (second objectif). La condition `StepTarget:ENTER_OFFICE` s'assure que le clic n'est actif que si le premier objectif est déjà accompli.

### 💡 Exemples pratiques

#### Exemple : Système séquentiel avec conditions
```
--- Step:FR:Étape tutorial:EN:Tutorial step
------ Target (ref ENTER_OFFICE):FR:Entrer dans le bureau:EN:Enter the office  
------ Target (ref SEARCH_DESK):FR:Fouiller le bureau:EN:Search the desk

------ Possibility
--------- Trigger:GOINSPACE:Office
--------- Consequence:GoalTarget:targetId:ENTER_OFFICE:state:success

------ Possibility  
--------- Trigger:CLICKMAPOBJECT:DESK_POSITION
--------- Condition:StepTarget:ENTER_OFFICE
--------- Consequence:GoalTarget:targetId:SEARCH_DESK:state:success
--------- Consequence:Alert
------------ FR:Vous avez fouillé le bureau avec succès !
------------ EN:You searched the desk successfully!
```

**Principe :** Le joueur doit d'abord entrer dans le bureau (premier objectif), puis cliquer sur le bureau sur la carte (second objectif). La condition `StepTarget:ENTER_OFFICE` s'assure que le clic n'est actif que si le premier objectif est déjà accompli.

---

## 📖 Exemple complet

Voici un exemple complet d'un mini escape game urbain :

```
MYSTERE_LOUVRE:1.0:Mystère au Louvre:90

--- Board
------ Space:Cour Napoléon:HIGH
--------- bottomLeft:48.8606:2.3376:topRight:48.8616:2.3386
------ Space:Jardin des Tuileries:MEDIUM
--------- bottomLeft:48.8634:2.3274:topRight:48.8644:2.3284

--- Step (ref ETAPE_DEBUT):FR:Accueil:EN:Welcome
------ Target (ref TROUVER_PYRAMIDE):FR:Trouver la pyramide:EN:Find the pyramid
------ Possibility
--------- Trigger:GOINSPACE:Cour Napoléon
--------- Consequence:GoalTarget:targetId:TROUVER_PYRAMIDE:state:success
--------- Consequence:TalkAlert
------------ FR:Vous avez trouvé la pyramide ! Cherchez maintenant l'indice caché.
------------ EN:You found the pyramid! Now look for the hidden clue.

--- Step (ref ETAPE_ENIGME):FR:L'énigme de la pyramide:EN:The pyramid puzzle
------ Target (ref RESOUDRE_ENIGME):FR:Résoudre l'énigme:EN:Solve the puzzle
------ Possibility
--------- Trigger:ABSOLUTETIME:30
--------- Condition:INSPACE:Cour Napoléon
--------- Consequence:TalkOptions
------------ Label
--------------- FR:Un message mystérieux apparaît...
--------------- EN:A mysterious message appears...
------------ Option (ref REPONSE_A)
--------------- FR:Le nombre d'or
--------------- EN:The golden ratio
------------ Option (ref REPONSE_B)
--------------- FR:666
--------------- EN:666

------ Possibility
--------- Trigger:SELECTTALKOPTION:REPONSE_A
--------- Consequence:GoalTarget:targetId:RESOUDRE_ENIGME:state:success
--------- Consequence:SessionEnd:WIN

------ Possibility
--------- Trigger:SELECTTALKOPTION:REPONSE_B
--------- Consequence:GoalTarget:targetId:RESOUDRE_ENIGME:state:failure
--------- Consequence:SessionEnd:LOOSE

------ Possibility
--------- Trigger:MAPCLICK:PYRAMIDE_CLICK
--------- Condition:INSPACE:Cour Napoléon
--------- Consequence:TalkAlert
------------ FR:Vous examinez la pyramide de plus près...
------------ EN:You examine the pyramid more closely...

--- Map:Asset:images/plan_louvre.png
------ Priority:HIGH
------ Position:0.5:0.3
--------- Priority:HIGHEST
--------- Space:Cour Napoléon
------ Position (ref PYRAMIDE_CLICK):0.7:0.4
--------- Priority:HIGH
------ Position:0.2:0.8
--------- Priority:MEDIUM
--------- Space:Jardin des Tuileries
```

---

## 📚 Référence rapide

### Types de déclencheurs
| Type | Syntaxe | Description |
|------|---------|-------------|
| `GOINSPACE` | `Trigger:GOINSPACE:ID_ZONE` | Entrer dans une zone |
| `GOOUTSPACE` | `Trigger:GOOUTSPACE:ID_ZONE` | Sortir d'une zone |
| `ABSOLUTETIME` | `Trigger:ABSOLUTETIME:MINUTES` | Temps absolu depuis le début |
| `RELATIVETIME` | `Trigger:RELATIVETIME:ID_POSSIBILITY:MINUTES` | Temps relatif après une autre règle |
| `SELECTTALKOPTION` | `Trigger:SELECTTALKOPTION:ID_OPTION` | Sélection d'option de dialogue |
| `MAPCLICK` | `Trigger:MAPCLICK:REFERENCE` | Clic sur élément référencé de carte |

### Types de conséquences
| Type | Syntaxe | Description |
|------|---------|-------------|
| `GoalTarget` | `Consequence:GoalTarget:targetId:ID:state:STATE` | Modifier état d'un objectif |
| `GoalStep` | `Consequence:GoalStep:stepId:ID:state:STATE` | Modifier état d'une étape |
| `SessionEnd` | `Consequence:SessionEnd:WIN/LOOSE` | Terminer la session |
| `TalkAlert` | `Consequence:TalkAlert` + contenu | Afficher un message |
| `TalkOptions` | `Consequence:TalkOptions` + contenu | Afficher des choix |
| `OBJETADD` | `Consequence:OBJETADD:ID_OBJET` | Ajouter un objet |
| `OBJETREMOVE` | `Consequence:OBJETREMOVE:ID_OBJET` | Retirer un objet |
| `UPDATEDMETADATA` | `Consequence:UPDATEDMETADATA:KEY:VALUE` | Mettre à jour une métadonnée |

### États disponibles
- `active` : actif/visible
- `success` : réussi/terminé avec succès
- `failure` : échoué
- `inactive` : inactif/masqué

### Priorités
- `HIGHEST`, `HIGH`, `MEDIUM`, `LOW`, `LOWEST`

---

## 🆘 Conseils et bonnes pratiques

### ✅ À faire
- **Utilisez des noms de référence explicites** : `TROUVER_PYRAMIDE` plutôt que `T1`
- **Utilisez les coordonnées relatives** : 0-1 pour positions sur carte (0,0 = coin bas-gauche)
- **Organisez vos étapes dans l'ordre logique** du jeu
- **Commentez vos templates** avec des descriptions claires
- **Utilisez la déduction automatique de stepId** pour simplifier votre code
- **Créez des éléments cliquables** avec références et `MAPCLICK` pour l'interactivité

### ❌ À éviter
- Ne pas oublier les `:` entre les paramètres
- Ne pas mélanger les niveaux de tirets (`---`, `------`, `---------`)
- Ne pas utiliser de caractères spéciaux dans les IDs
- Ne pas créer de références circulaires

### 🐛 Résolution de problèmes
- **Erreur "Invalid parameter format"** : vérifiez les `:` et l'ordre des paramètres
- **Référence non trouvée** : assurez-vous que la référence est définie avant d'être utilisée
- **Coordonnées incorrectes** : utilisez le format `latitude:longitude` (pas l'inverse)

---

## 📞 Support

Pour toute question ou problème avec vos templates, n'hésitez pas à :
1. Consulter les exemples dans ce guide
2. Vérifier la syntaxe avec les références rapides
3. Contacter l'équipe de développement pour assistance

**Bon game design ! 🎮**