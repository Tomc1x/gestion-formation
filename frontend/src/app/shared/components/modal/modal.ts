import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-modal',
  standalone: true,
  imports: [],
  templateUrl: './modal.html',
  styleUrl: './modal.scss'
})
export class ModalComponent {
  @Input() open: boolean = false;
  @Input() width: number = 560;
  @Input() title?: string;
  @Input() subtitle?: string;
  @Input() icon?: string;

  @Output() close = new EventEmitter<void>();

  onClose() {
    this.close.emit();
  }
}
