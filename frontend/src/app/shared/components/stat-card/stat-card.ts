import { Component, Input } from '@angular/core';
import { NgClass } from '@angular/common';

@Component({
  selector: 'app-stat-card',
  standalone: true,
  imports: [NgClass],
  templateUrl: './stat-card.html',
  styleUrl: './stat-card.scss'
})
export class StatCardComponent {
  @Input() icon: string = 'box';
  @Input() label: string = '';
  @Input() value: string | number = 0;
  @Input() delta?: string;
  @Input() tone: 'blue' | 'green' | 'purple' | 'amber' = 'blue';
  @Input() clickable: boolean = false;
}
