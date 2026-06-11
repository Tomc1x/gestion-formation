import { Component, ChangeDetectionStrategy, inject, signal, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { LucideArrowLeft } from '@lucide/angular';
import { BaseInscriptionAdapter } from '../../../core/adapters/inscription.adapter';
import { InscritCours } from '../../../core/models/inscription.model';

@Component({
  selector: 'app-inscrits',
  imports: [RouterLink, LucideArrowLeft],
  templateUrl: './inscrits.html',
  styleUrl: './inscrits.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InscritsComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly inscriptionAdapter = inject(BaseInscriptionAdapter);

  protected readonly inscrits = signal<InscritCours[]>([]);
  protected readonly loading = signal(true);
  protected readonly loadError = signal<string | null>(null);

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    const id = idParam ? Number(idParam) : null;
    if (id === null) {
      this.loadError.set('Session de cours introuvable.');
      this.loading.set(false);
      return;
    }
    this.loadInscrits(id);
  }

  private loadInscrits(coursPlanifieId: number): void {
    this.loading.set(true);
    this.loadError.set(null);

    this.inscriptionAdapter.getInscrits(coursPlanifieId).subscribe({
      next: inscrits => {
        this.inscrits.set(inscrits);
        this.loading.set(false);
      },
      error: () => {
        this.loadError.set('Impossible de charger la liste des inscrits.');
        this.loading.set(false);
      },
    });
  }

  protected fullName(p: { firstName: string; lastName: string }): string {
    return `${p.firstName} ${p.lastName}`;
  }
}
