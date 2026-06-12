# WI-20260611-FULLST-029

## Work Item
WI-20260611-FULLST-029 — Corriger le "desordre pedagogique" du cursus DWWM via la
fonctionnalite "Corriger" livree en WI-027/028 (page `/app/admin/cursus/:id`).

## Role
developer

## Status
DONE (rien a corriger — voir Decisions)

## Scope
Verification fonctionnelle via chrome-devtools sur `/app/admin/cursus/5`
("Développeur web et web mobile", filiere "Développement" — c'est le cursus DWWM).
Aucun fichier de code modifie.

## Evidence

### Etat "avant" (et "apres" — identique, aucune action possible)
URL: http://localhost:4200/app/admin/cursus/5 (login ref@ref.com / toto785971)

Cursus "Développeur web et web mobile" — 18 cours, ordre pedagogique (positions 0-17) :
```
0  Algorithmique / Pseudo-Code
1  Initiation à la Programmation / Java
2  Web Client / HTML & CSS
3  JavaScript initiation
4  Projet Web / HTML & CSS + JS
5  Programmation Orientée Objet / Java
6  Langage SQL / SQL Server
7  Notions Complémentaires / Java SE
8  Développement Web côté Serveur (Back-End) / Java Spring Boot
9  Projet Web / Java Spring Boot
10 Analyse et Conception / Oracle Data Modeler
11 JavaScript avancé + initiation Framework JS / Angular
12 Développement Web côté Serveur avec JavaScript / Node.js et NoSQL
13 Développement Web côté Serveur (Back-End) / PHP
14 Développement Web côté Serveur (Back-End) / Symfony
15 Projet Web / Symfony
16 CMS / WordPress
17 CMS / WordPress + Projet Final
```

Bloc "⚠ Prérequis mal ordonnés" : **14 alertes**, TOUTES de la forme :
```
« <cours> » (position N) requiert « Algorithmique + Initiation à la Programmation / Java »
— absent du cursus
```
(positions concernees : 3, 4, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 — courte liste
des cours dont le prereq catalogue "Algorithmique + Initiation à la Programmation / Java"
est cite).

**Aucun bouton "Corriger" n'est rendu pour aucune de ces 14 alertes.**

### Verification du template
`frontend/src/app/features/administration/cursus-detail/cursus-detail.html` lignes 54-63 :
le bouton "Corriger" (`fixOrder`) n'est rendu QUE si `alert.prereqPosition > 0`.
Pour ce cursus, les 14 alertes ont toutes `prereqPosition === 0` (prereq absent de la
liste -> `cours.findIndex(...) === -1` -> `prereqIndex + 1 === 0`, cf.
`cursus-alerts.util.ts` lignes 45-53).

=> Le mecanisme "Corriger" fonctionne exactement comme specifie en WI-027/028 : il ne
s'affiche QUE pour les alertes ou le prereq est present dans le cursus mais mal place.

## Decisions
- **Aucune action n'a ete realisee** : sur le cursus DWWM, les 14 alertes "prérequis mal
  ordonné" correspondent TOUTES au cas "prereq absent du cursus" (le cours catalogue
  "Algorithmique + Initiation à la Programmation / Java" — vraisemblablement un prereq
  "fusionne" issu du referentiel pedagogique — n'est lui-meme membre du cursus DWWM).
  Le cursus contient a la place deux cours separes en positions 0 et 1
  ("Algorithmique / Pseudo-Code" et "Initiation à la Programmation / Java") qui
  semblent etre l'equivalent "splitte" de ce prereq, mais ne matchent pas par id avec
  le prereq catalogue.
- Conformement aux instructions du WI : ces alertes "prereq absent du cursus" ne sont
  PAS corrigibles via le bouton "Corriger" (par design, cf. FULLST-027), et ne doivent
  pas etre "forcees". Il n'y a donc rien a reordonner ici — pas de bug a remonter sur
  FULLST-027/028, le comportement observe correspond exactement a la specification.
- Le "desordre pedagogique" signale par l'utilisateur n'est donc pas un probleme
  d'ordre des cours dans le cursus (l'ordre actuel semble deja coherent : pseudo-code ->
  prog Java -> ... -> back-end -> projets -> WordPress), mais releve d'un probleme de
  **modelisation catalogue** : soit le cours catalogue "Algorithmique + Initiation à la
  Programmation / Java" devrait etre supprime/fusionne avec les deux cours equivalents
  deja presents (0 et 1) au niveau des `prerequis` des autres cours catalogue, soit ces
  deux cours (0 et 1) devraient eux-memes etre declares comme prerequis directs des
  cours qui referencent actuellement le cours fusionne absent.
- Pas de rechargement/persistance a verifier puisqu'aucune mutation n'a ete effectuee.

## Files Touched
Aucun (verification uniquement).

## Open Blockers
Aucun blocker technique. Point ouvert fonctionnel : la correction du "desordre
pedagogique" reel necessiterait un changement de donnees catalogue (relation
prerequis du cours "Algorithmique + Initiation à la Programmation / Java" vs les
cours "Algorithmique / Pseudo-Code" + "Initiation à la Programmation / Java"), ce qui
sort du scope de ce WI (UI de reordonnancement) et devrait faire l'objet d'un nouveau
WI si l'utilisateur le souhaite.

## Next Actions
- Si l'utilisateur souhaite vraiment faire disparaitre ces 14 alertes "absent du
  cursus" pour le DWWM, ouvrir un nouveau WI cible "catalogue cours / prerequis" pour
  decider : (a) ajouter le cours "Algorithmique + Initiation à la Programmation / Java"
  au cursus DWWM, ou (b) repointer les `prerequis` des cours catalogue concernes vers
  "Algorithmique / Pseudo-Code" + "Initiation à la Programmation / Java".
- Ne pas modifier `computeCursusPrereqAlerts` / le template pour "forcer" l'affichage
  d'un bouton Corriger sur le cas `prereqPosition === 0` sans decision produit explicite
  (changerait la semantique validee en FULLST-027).

## Recall Hints
- Cursus DWWM = id 5 ("Développeur web et web mobile", filiere "Développement"),
  18 cours, 14 alertes "prereq absent du cursus" (toutes referencant le meme prereq
  catalogue "Algorithmique + Initiation à la Programmation / Java").
- Login utilise pour la verification : ref@ref.com / toto785971 (deja documente en
  FULLST-017/019/020).
- `cursus-detail.html` L54-63 : condition `alert.prereqPosition > 0` pour afficher
  "Corriger".
