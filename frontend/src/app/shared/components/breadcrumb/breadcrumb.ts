import { Component, ChangeDetectionStrategy, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { Router, NavigationEnd, ActivatedRoute } from '@angular/router';
import { filter, map, startWith } from 'rxjs';

@Component({
  selector: 'app-breadcrumb',
  imports: [],
  template: `
    @if (pageTitle()) {
      <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <span class="breadcrumb-home">Accueil</span>
        <span class="breadcrumb-sep" aria-hidden="true">/</span>
        <span class="breadcrumb-current" aria-current="page">{{ pageTitle() }}</span>
      </nav>
    }
  `,
  styleUrl: './breadcrumb.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BreadcrumbComponent {
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);

  readonly pageTitle = toSignal(
    this.router.events.pipe(
      filter(e => e instanceof NavigationEnd),
      startWith(null),
      map(() => {
        let route = this.activatedRoute.root;
        while (route.firstChild) route = route.firstChild;
        return route.snapshot.title;
      })
    )
  );
}
