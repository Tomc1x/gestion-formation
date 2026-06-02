import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UiElements } from './ui-elements';

describe('UiElements', () => {
  let component: UiElements;
  let fixture: ComponentFixture<UiElements>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UiElements],
    }).compileComponents();

    fixture = TestBed.createComponent(UiElements);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
