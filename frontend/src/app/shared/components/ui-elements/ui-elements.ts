import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { NgClass } from '@angular/common';

@Component({
  selector: 'app-ui-elements',
  standalone: true,
  imports: [NgClass],
  templateUrl: './ui-elements.html',
  styleUrl: './ui-elements.scss'
})
export class UiElementsComponent implements OnChanges {
  // Propriétés pour le type d'élément à afficher
  @Input() type: 'pill' | 'badge' | 'avatar' = 'pill';

  // Propriétés spécifiques
  @Input() statut: 'active' | 'upcoming' | 'ended' = 'active';
  @Input() role: 'ELEVE' | 'FORMATEUR' | 'REF' | 'ADMIN' = 'ELEVE';
  @Input() size: 'sm' | 'md' | 'lg' | number = 'md';
  @Input() name: string = '';

  // Métadonnées pour l'avatar ou le badge
  initials: string = '';
  roleLabel: string = '';

  ngOnChanges(changes: SimpleChanges): void {
    if (this.name) {
      this.initials = this.name.split(' ').map(w => w[0]).slice(0, 2).join('').toUpperCase();
    }

    // Mapping des rôles (Simule ton window.DB.roleMeta)
    const roleMap = {
      ELEVE: 'Élève',
      FORMATEUR: 'Formateur',
      REF: 'Référente',
      ADMIN: 'Admin'
    };
    this.roleLabel = roleMap[this.role] || this.role;
  }
}
