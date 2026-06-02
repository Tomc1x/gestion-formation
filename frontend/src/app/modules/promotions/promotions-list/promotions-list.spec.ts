import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PromotionsList } from './promotions-list';

describe('PromotionsList', () => {
  let component: PromotionsList;
  let fixture: ComponentFixture<PromotionsList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PromotionsList],
    }).compileComponents();

    fixture = TestBed.createComponent(PromotionsList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
