# Calcul des bénéfices – Charte UP

Application Java Swing pour répartir le bénéfice d’un événement entre plusieurs associations selon une charte : part fixe, part variable (points) et part savoir‑faire.

## Fonctionnalités

- Gestion d’une liste d’associations (ajout, suppression, sauvegarde automatique dans `associations.csv`).  
- Saisie de points par type (bénévolat, logistique, etc.) et par association dans un tableau, sauvegardés dans `points.csv`.  
- Attribution de savoir‑faire par association (cases à cocher), sauvegardés dans `savoirfaire.csv`.  
- Paramétrage des poids :
  - des savoir‑faire (ex. Prog artistique, Visuel, etc.)  
  - des types de points (ex. Heures de bénévolat, Bilan financier, etc.)  
  Ces poids sont sauvegardés dans `params.csv`.  
- Calcul automatique de la répartition du bénéfice total via `BenefitCalculator` (part fixe, variable, savoir‑faire) et affichage détaillé par association.  

## Structure du projet

- `App.java` : fenêtre principale (JFrame), tables, actions utilisateur, sauvegarde auto à chaque modification.  
- `SettingsDialog.java` : fenêtre de paramètres (JDialog) pour modifier les poids des savoir‑faire et des types de points (sauvegarde auto).  
- `BenefitCalculator.java` : logique de répartition du bénéfice.  
- `Association.java` : modèle pour une association (nom, parts, total).  
- `SavoirFaire.java` : modèle pour un savoir‑faire (nom, poids).  
- `PointType.java` : modèle pour un type de point (nom, poids).  
- `Save.java` : lecture/écriture des fichiers CSV (`associations.csv`, `points.csv`, `savoirfaire.csv`, `params.csv`).  

## Format des fichiers

- `associations.csv`  
  - Une association par ligne :  
    - `NomAssociation`

- `points.csv`  
  - Une ligne par type de point :  
    - `TypeDePoint;asso1;asso2;...`  
  - Les valeurs sont les points saisis dans l’interface.

- `savoirfaire.csv`  
  - Une ligne par association :  
    - `NomAssociation;prog;malle;visuel;deco`  
  - Les valeurs sont `true` / `false` selon les cases cochées.

- `params.csv`  
  - Ligne 1 (savoir‑faire) : `SF;nomSF1;poids1;nomSF2;poids2;...`  
  - Ligne 2 (types de points) : `PT;nomType1;poids1;nomType2;poids2;...`  

## Compilation et exécution

1. Placer tous les fichiers `.java` dans le même package `_Benefices_Up`.  
2. Compiler (par exemple avec `javac`) ou via un IDE (IntelliJ, Eclipse, NetBeans).  
3. Lancer la classe `App` (méthode `main`).  

Les fichiers CSV sont créés / mis à jour dans le répertoire de travail courant de l’application.
