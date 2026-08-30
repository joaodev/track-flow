import {
  Directive,
  effect,
  ElementRef,
  forwardRef,
  HostListener,
  inject,
  Renderer2,
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { TranslationService } from '../translation.service';
import { CURRENCY_BY_LANG } from './currency.util';

@Directive({
  selector: '[appCurrencyInput]',
  standalone: true,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => CurrencyInputDirective),
      multi: true,
    },
  ],
})
export class CurrencyInputDirective implements ControlValueAccessor {
  private el = inject(ElementRef<HTMLInputElement>);
  private renderer = inject(Renderer2);
  private translation = inject(TranslationService);

  private onChange: (value: number) => void = () => {};
  private onTouched: () => void = () => {};
  private rawValue: number = 0;

  constructor() {
    effect(() => {
      this.translation.currentLang();
      this.render();
    });
  }

  writeValue(value: number | null): void {
    this.rawValue = value ?? 0;
    this.render();
  }

  registerOnChange(fn: (value: number) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.renderer.setProperty(this.el.nativeElement, 'disabled', isDisabled);
  }

  @HostListener('input', ['$event'])
  onInput(event: Event): void {
    const target = event.target as HTMLInputElement;
    const digits = target.value.replace(/\D/g, '');
    const cents = digits ? parseInt(digits, 10) : 0;
    this.rawValue = cents / 100;
    this.onChange(this.rawValue);
    this.render();
  }

  @HostListener('blur')
  onBlue(): void {
    this.onTouched();
  }

  private render(): void {
    const { locale, currencyCode } = CURRENCY_BY_LANG[this.translation.currentLang()];
    const formatted = new Intl.NumberFormat(locale, {
      style: 'currency',
      currency: currencyCode,
    }).format(this.rawValue);
    this.renderer.setProperty(this.el.nativeElement, 'value', formatted);
  }
}
