import { inject, Pipe, PipeTransform } from '@angular/core';
import { TranslationService } from '../translation.service';
import { formatCurrency } from './currency.util';

@Pipe({ name: 'localCurrency', standalone: true, pure: false })
export class LocalCurrencyPipe implements PipeTransform {
  private translation = inject(TranslationService);

  transform(value: number | null | undefined): string {
    if (value == null) return '';
    return formatCurrency(value, this.translation.currentLang());
  }
}
