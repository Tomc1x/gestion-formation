import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MonCalendrier } from './mon-calendrier';

describe('MonCalendrier', () => {
  let component: MonCalendrier;
  let fixture: ComponentFixture<MonCalendrier>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MonCalendrier],
    }).compileComponents();

    fixture = TestBed.createComponent(MonCalendrier);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
