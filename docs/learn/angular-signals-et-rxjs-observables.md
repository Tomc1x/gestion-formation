# Signals Angular & Observables RxJS

> Niveau : Débutant / Intermédiaire
> Prérequis : bases TypeScript, notions de réactivité
> Durée estimée : ~15 minutes

## 1. La double négation `!!`

### Concept en une phrase

`!!` convertit n'importe quelle valeur JavaScript en son équivalent booléen.

### Comment ça fonctionne

JavaScript classe les valeurs en deux catégories :
- **Falsy** : `null`, `undefined`, `""`, `0`, `false` → `!!valeur` donne `false`
- **Truthy** : tout le reste (une chaîne non-vide, un objet, un nombre non-zéro) → `!!valeur` donne `true`

```typescript
!!localStorage.getItem('auth_token') // null → false, "abc" → true
```

---

## 2. Signals Angular

### Concept en une phrase

Un signal est une **boîte réactive** : quand sa valeur change, tous les endroits qui la lisent sont automatiquement mis à jour.

### Analogie

Une boîte avec un voyant lumineux. Quand tu changes le contenu, le voyant clignote chez tous ceux qui regardent cette boîte — sans qu'on ait besoin de les prévenir manuellement.

### Comment ça fonctionne

```typescript
// Créer un signal avec une valeur initiale
private readonly _isAuthenticated = signal(false);

// Exposer en lecture seule (l'extérieur peut lire, pas modifier)
readonly isAuthenticated = this._isAuthenticated.asReadonly();

// Modifier la valeur (uniquement dans le service)
this._isAuthenticated.set(true);
```

### À retenir

- `signal(valeur)` → crée la boîte
- `.set(nouvelleValeur)` → modifie le contenu
- `.asReadonly()` → expose une version non modifiable de l'extérieur
- Différent de `computed()` qui dérive une valeur d'un autre signal

---

## 3. Observables RxJS

### Concept en une phrase

Un Observable est un **flux de valeurs** qui peuvent arriver maintenant, plus tard, ou indéfiniment.

### Analogie

S'abonner à un journal : tu ne reçois pas tous les articles d'un coup, tu les reçois au fur et à mesure qu'ils sont publiés.

### Deux grandes familles

| Type | Exemple | Se termine ? |
|------|---------|--------------|
| **Fini** | Appel HTTP, `of(true)` | Oui, après N valeurs |
| **Infini** | Clics sur un bouton, WebSocket | Non, tant qu'abonné |

### `of(valeur)`

Crée un Observable qui émet une valeur **immédiatement** puis se complète.

```typescript
return of(true); // émet true → se termine → pas de fuite mémoire
```

### Pourquoi retourner un Observable pour `login()` ?

La vraie implémentation fait un appel HTTP (asynchrone). Pour garder une **interface cohérente** entre la version mock et la version réelle, le mock retourne aussi un Observable — même si sa valeur est disponible immédiatement.

### Lire la valeur : `.subscribe()`

```typescript
this.authService.login(credentials).subscribe(result => {
  // result = true
});
```

---

## Pièges courants

- **Oublier de se désabonner** d'un Observable infini → fuite mémoire (le callback tourne sur un composant détruit)
- **Solution** : utiliser le `async` pipe dans les templates — il se désabonne automatiquement à la destruction du composant
- Les Observables finis (comme `of(...)` ou les appels HTTP) se complètent seuls → pas de risque

## Questions de révision

1. Que retourne `!!null` ? Et `!!"token-abc"` ?
2. Quelle est la différence entre `signal.set()` et `signal.asReadonly()` ?
3. Pourquoi un Observable infini peut-il causer une fuite mémoire ?
4. Pourquoi `of(true)` ne cause-t-il pas de fuite mémoire ?

## Ressources pour aller plus loin

- [Angular Signals — doc officielle](https://angular.dev/guide/signals)
- [RxJS `of` operator](https://rxjs.dev/api/index/function/of)
- [Angular async pipe](https://angular.dev/api/common/AsyncPipe)
