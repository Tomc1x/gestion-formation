import { Component, ChangeDetectionStrategy, input, computed } from '@angular/core';

const AVATAR_COLORS = ['#E3ECF6', '#EDE9F8', '#E4F5EE', '#FDF3E5', '#FCE8E8', '#E8F5FC'];
const AVATAR_TEXT_COLORS = ['#1E3A5F', '#5B21B6', '#1E9E6A', '#D97706', '#C0392B', '#1A6FA8'];

@Component({
  selector: 'app-avatar',
  imports: [],
  template: `
    <span
      class="avatar"
      [class.avatar--sm]="size() === 'sm'"
      [class.avatar--md]="size() === 'md'"
      [class.avatar--lg]="size() === 'lg'"
      [style.background]="bg()"
      [style.color]="textColor()"
      [attr.aria-label]="firstName() + ' ' + lastName()"
      aria-hidden="true"
    >{{ initials() }}</span>
  `,
  styleUrl: './avatar.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AvatarComponent {
  firstName = input.required<string>();
  lastName = input.required<string>();
  size = input<'sm' | 'md' | 'lg'>('md');

  initials = computed(() => {
    const f = this.firstName().trim()[0] ?? '';
    const l = this.lastName().trim()[0] ?? '';
    return (f + l).toUpperCase();
  });

  private colorIndex = computed(() => {
    const seed = (this.firstName() + this.lastName()).toLowerCase();
    let hash = 0;
    for (let i = 0; i < seed.length; i++) {
      hash = seed.charCodeAt(i) + ((hash << 5) - hash);
    }
    return Math.abs(hash) % AVATAR_COLORS.length;
  });

  bg = computed(() => AVATAR_COLORS[this.colorIndex()]);
  textColor = computed(() => AVATAR_TEXT_COLORS[this.colorIndex()]);
}
