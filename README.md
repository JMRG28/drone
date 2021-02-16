## Travailler avec gitlab

### Initialisation :
Il faut préalablement avoir créé un compte sur ce gitlab [ici](http://git.coulet.xyz/users/sign_in) 

Cloner le repository sur votre machine :
```
git clone https://git.coulet.xyz/Gaspard/Drone-routing.git
```

### Obtention des mises-à-jour
```
git pull
```
### Mise en ligne de vos modifications
La mise en ligne s'effectue en 3 étapes.

D'abord : l'ajout des fichiers:

Ajouter tout les fichiers modifiés :
```
git add -u
```
Ajouter un fichier spécifique :
```
git add "nom_du_fichier"
```
Ajouter tout ( non recommandé )
```
git add -A
```
Ensuite, le "commit" ou archivage des modifications, où le message comporte les informations sur les modifications apportées
```
git commit -m "message du commit"
```
Enfin, après qu'au moins un commit ait été réalisé, l'envoit
```
git push
```
### Travailler avec les branches
Afin de travailler proprement, git dispose d'un système de branches, la principale est la branche "MASTER" sur laquelle on ne doit, en théorie, jamais travailler directement ( c'est une branche dite stable ).
Ainsi, on se localise/créée (dans) une branche dédiée avant de travailler sur un dossier, un fichier, une fois les modifications effectuées et le code testé, on fusionne le contenu de cette branche de travail avec la branche principale.

Sur chaque poste, on peut connaitre le nom de la branche courante en faisant :
```
git branch
```
On peut obtenir une liste de toutes les branches en faisant :
```
git branch -a
```
On change la branche courante en faisant :
```
git checkout nom_branche
```
On peut créer une branche et s'y localiser directement en faisant :
```
git checkout -b nom_branche
```

Enfin, quand on juge que le travail produit dans une branche est fini et/ou fiable, on peut fusionner cette branche avec MASTER en faisant :
```
git checkout master
git merge nom_branche
```
Logiquement, si l'on veut importer le contenu d'une branche vers une autre, on utilise l'enchainement :
```
git checkout branche_cible
git merge branche_source
``` 
Après chaque merge, il faut pusher le résultat sur la branche courante !
Pour supprimer une branche :
```
git branch -d nom_branche
```

Il arrive qu'il y ait des conflits lors des merges, alors on peut savoir quels sont les fichiers affectés :
```
git status
```
Les fichiers à conflits sont précédés de la mention "unmerged", on peut alors aller résoudre facilement ces conflits qui se présentent comme :
```
<<<<<<< HEAD:fichier.ext
code présent selon la branche master
=======
code présent selon la branche de travail
>>>>>>> nom_branche:fichier.ext
```
Pour résoudre le conflit, on choisit le code à garder, puis on supprime l'autre, ainsi que les chevrons, et les égals.
Ensuite, on peut réajouter les modifications avec
```
git add
```
