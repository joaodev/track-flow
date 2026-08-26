import { Component, EventEmitter, inject, Output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-track-search-form',
  standalone: true,
  imports: [ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './track-search-form.component.html',
  styleUrl: './track-search-form.component.scss',
})
export class TrackSearchFormComponent {
  @Output() search = new EventEmitter<string>();

  private fb = inject(FormBuilder);

  form = this.fb.nonNullable.group({
    trackingCode: ['', Validators.required],
  });

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.search.emit(this.form.getRawValue().trackingCode.trim().toUpperCase());
  }
}
