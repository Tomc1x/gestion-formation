# Work Item: WI-20260608-FRONTE-002
# Role: developer
# Status: DONE

## Scope
MonCalendrierComponent — vues mois et semaine avec navigation, légende et chargement réactif des événements via MockCalendarAdapter.

## Files Touched
- `frontend/src/app/features/calendrier/mon-calendrier/mon-calendrier.ts` (replaced stub)
- `frontend/src/app/features/calendrier/mon-calendrier/mon-calendrier.html` (replaced stub)
- `frontend/src/app/features/calendrier/mon-calendrier/mon-calendrier.scss` (created, was empty)

## Evidence
```
npx ng build --configuration=development
Application bundle generation complete. [2.355 seconds]
Lazy chunk: chunk-TWVCMX64.js | mon-calendrier | 94.14 kB
BUILD: PASS — zero errors, zero warnings
```

## Decisions

1. **combineLatest + toObservable** pour réagir aux deux signals `referenceDate` et `view` — plus propre qu'un `effect()` qui setait `referenceDate` en guise de déclencheur.

2. **Inject `BaseCalendarAdapter`** (token abstrait) plutôt que `MockCalendarAdapter` directement — cohérent avec l'app.config.ts existant (`{ provide: BaseCalendarAdapter, useClass: MockCalendarAdapter }`).

3. **`DatePipe`** importé depuis `@angular/common` pour les bindings `| date` dans le template (formatage accessibilité ARIA `aria-label`). Les appels date dans le composant utilisent `date-fns/format` directement.

4. **Grille mois** : `startOfWeek(..., { weekStartsOn: 1 })` pour démarrer lundi. La boucle while produit autant de semaines que nécessaire (4 à 6) sans forcer 6 semaines fixes.

5. **Légende** positionnée avec `margin-left: auto` dans la toolbar pour pousser à droite naturellement sans flex-grow hacky.

6. **Accessibilité** : `role="grid"`, `role="row"`, `role="gridcell"`, `role="columnheader"`, `aria-label` sur chaque cellule et événement, `aria-pressed` sur les boutons toggle, `aria-hidden` sur les SVG décoratifs. WCAG AA visé.

## Open Blockers
~

## Next Actions
~

## Recall Hints
- Token DI calendrier : `BaseCalendarAdapter` (abstract class), fourni via `app.config.ts`
- date-fns v4 utilisé — imports nommés depuis `date-fns` et `date-fns/locale`
- `toObservable` / `toSignal` depuis `@angular/core/rxjs-interop` (Angular 16+)

## Proposed Rules

- TYPE: CONVENTION
  Title: Réactivité multi-signals via combineLatest + toObservable
  Scope: Composants Angular avec plusieurs signals déclencheurs d'un Observable
  Rule: Utiliser `combineLatest([toObservable(sig1), toObservable(sig2)]).pipe(switchMap(...))` plutôt qu'un `effect()` qui re-set un signal pour déclencher une recharge.
  Why: L'effect approach est un hack (effets secondaires dans un effect), combineLatest est déclaratif et testable.
  How to apply: Importer `combineLatest` depuis `rxjs` et `toObservable` depuis `@angular/core/rxjs-interop`. Passer tous les signals déclencheurs dans le tableau combineLatest.
  Evidence: WI-20260608-FRONTE-002, mon-calendrier.ts
