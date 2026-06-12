import { Component, ChangeDetectionStrategy, input, output, signal, computed } from '@angular/core';
import { LucideSearch, LucidePlus, LucideChevronLeft, LucideChevronRight } from '@lucide/angular';

export interface SelectableEntity {
  id: number;
  label: string;
  sublabel?: string;
}

@Component({
  selector: 'app-entity-selector',
  imports: [LucideSearch, LucidePlus, LucideChevronLeft, LucideChevronRight],
  templateUrl: './entity-selector.html',
  styleUrl: './entity-selector.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EntitySelectorComponent {
  readonly items = input.required<SelectableEntity[]>();
  readonly selectedIds = input<Set<number>>(new Set());
  readonly disabledIds = input<Set<number>>(new Set());
  readonly mode = input<'add' | 'multi-select'>('add');
  readonly pageSize = input<number>(10);
  readonly placeholder = input<string>('Rechercher...');

  readonly add = output<number>();
  readonly selectionChange = output<Set<number>>();

  protected readonly searchTerm = signal('');
  protected readonly currentPage = signal(0);
  private readonly localSelection = signal<Set<number>>(new Set());

  protected readonly filteredItems = computed(() => {
    const term = this.normalize(this.searchTerm());
    const items = this.items();
    if (!term) return items;
    return items.filter(item =>
      this.normalize(item.label).includes(term) ||
      (item.sublabel ? this.normalize(item.sublabel).includes(term) : false)
    );
  });

  protected readonly totalPages = computed(() => Math.max(1, Math.ceil(this.filteredItems().length / this.pageSize())));

  protected readonly pagedItems = computed(() => {
    const page = this.currentPage();
    const size = this.pageSize();
    const start = page * size;
    return this.filteredItems().slice(start, start + size);
  });

  protected readonly currentSelection = computed(() => {
    const initial = this.selectedIds();
    const local = this.localSelection();
    return local.size > 0 || initial.size === 0 ? local : initial;
  });

  protected onSearchChange(value: string): void {
    this.searchTerm.set(value);
    this.currentPage.set(0);
  }

  protected goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages()) return;
    this.currentPage.set(page);
  }

  protected onAdd(id: number): void {
    this.add.emit(id);
  }

  protected isSelected(id: number): boolean {
    return this.selectedIds().has(id) || this.localSelection().has(id);
  }

  protected toggleSelection(id: number): void {
    const next = new Set(this.localSelection().size > 0 ? this.localSelection() : this.selectedIds());
    if (next.has(id)) {
      next.delete(id);
    } else {
      next.add(id);
    }
    this.localSelection.set(next);
    this.selectionChange.emit(next);
  }

  protected isDisabled(id: number): boolean {
    return this.disabledIds().has(id);
  }

  private normalize(value: string): string {
    return value
      .toLowerCase()
      .normalize('NFD')
      .replace(/[̀-ͯ]/g, '');
  }
}
