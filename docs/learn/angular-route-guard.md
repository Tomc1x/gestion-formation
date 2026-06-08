# Angular Route Guard — CanActivateFn

> Niveau : Débutant
> Prérequis : bases TypeScript (types), bases Angular (routes)
> Durée estimée : ~10 minutes

## Concept en une phrase

Un guard Angular est une fonction typée `CanActivateFn` qu'Angular appelle automatiquement avant de charger une route, pour décider si l'accès est autorisé ou non.

## Analogie

C'est comme un videur à l'entrée d'une salle : il vérifie ton invitation avant de te laisser entrer. Si tu n'en as pas, il te redirige vers l'accueil plutôt que de te laisser passer.

## Comment ça fonctionne

### 1. La déclaration TypeScript

```typescript
export const authGuard: CanActivateFn = () => { ... }
```

Le `: CanActivateFn` après les deux-points est une **annotation de type** TypeScript.  
Cela signifie : *"la variable `authGuard` contient une fonction qui respecte le contrat `CanActivateFn` d'Angular"*.

### 2. Les valeurs de retour

La fonction doit retourner :

| Valeur retournée | Effet |
|-----------------|-------|
| `true` | Accès autorisé — la route se charge |
| `false` | Accès refusé — navigation bloquée |
| `router.createUrlTree(['/chemin'])` | Accès refusé — redirection vers `/chemin` |

### 3. Exemple complet

```typescript
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.isAuthenticated() ? true : router.createUrlTree(['/login']);
};
```

- `inject()` remplace l'injection par constructeur dans les fonctions standalone
- `isAuthenticated()` vérifie si l'utilisateur a une session active
- Si non connecté → redirection vers `/login`

### 4. Appliquer le guard à une route

```typescript
const routes: Routes = [
  {
    path: 'tableau-de-bord',
    component: TableauDeBordComponent,
    canActivate: [authGuard]
  }
];
```

Angular appellera `authGuard` **avant** de charger `TableauDeBordComponent`.

## À retenir

- Le `:` en TypeScript déclare le **type** d'une variable, pas une valeur
- `CanActivateFn` est un type de fonction défini par Angular pour protéger les routes
- Retourner `true` laisse passer, retourner une `UrlTree` redirige
- Le guard est déclaré une fois, réutilisable sur autant de routes que nécessaire

## Questions de révision

1. Quelle est la différence entre retourner `false` et retourner `router.createUrlTree(['/login'])` ?
2. Pourquoi utilise-t-on `inject()` à l'intérieur du guard plutôt que dans un constructeur ?
3. Si tu veux protéger toutes les routes d'un module lazy-loaded, quel autre guard Angular pourrais-tu explorer ?

## Pièges courants

- **Oublier d'ajouter `canActivate`** dans la définition de route — le guard est déclaré mais jamais appelé
- **Retourner `false` au lieu d'une `UrlTree`** — l'utilisateur est bloqué sans être redirigé, ce qui crée une mauvaise expérience

## Ressources pour aller plus loin

- Documentation officielle Angular : *Router — Preventing unauthorized access*
- `CanActivateChildFn` — pour protéger toutes les routes enfants d'un groupe
