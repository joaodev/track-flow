import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

export type Lang = 'pt' | 'en' | 'es';

const LANG_KEY = 'track_flow_lang';
const DEFAULT_LANG: Lang = 'pt';
const SUPPORTED_LANGS: Lang[] = ['pt', 'en', 'es'];

type Dictionary = Record<string, unknown>;

@Injectable({ providedIn: 'root' })
export class TranslationService {
  private http = inject(HttpClient);
  private dictionaries: Partial<Record<Lang, Dictionary>> = {};

  currentLang = signal<Lang>(this.readInitialLang());
  dict = computed(() => this.dictionaries[this.currentLang()] ?? {});

  /** Called once by the app initializer, before the app renders */
  async loadAll(): Promise<void> {
    const results = await Promise.all(
      SUPPORTED_LANGS.map((lang) =>
        firstValueFrom(this.http.get<Dictionary>(`/i18n/${lang}.json`)),
      ),
    );
    SUPPORTED_LANGS.forEach((lang, i) => {
      this.dictionaries[lang] = results[i];
    });
  }

  setLang(lang: Lang): void {
    this.currentLang.set(lang);
    localStorage.setItem(LANG_KEY, lang);
  }

  t(key: string, params?: Record<string, string | number>): string {
    const value = this.resolve(this.dict(), key);
    if (typeof value !== 'string') {
      console.warn(`Missing translation for key "${key}" in language "${this.currentLang()}"`);
      return key;
    }
    return params ? this.interpolate(value, params) : value;
  }

  private resolve(dict: Dictionary, key: string): unknown {
    return key.split('.').reduce<unknown>((acc, segment) => {
      if (acc && typeof acc === 'object') {
        return (acc as Dictionary)[segment];
      }
      return undefined;
    }, dict);
  }

  private interpolate(value: string, params: Record<string, string | number>): string {
    return value.replace(/{{\s*(\w+)\s*}}/g, (_, token) =>
      token in params ? String(params[token]) : `{{${token}}}`,
    );
  }

  private readInitialLang(): Lang {
    const stored = localStorage.getItem(LANG_KEY) as Lang | null;
    return stored && SUPPORTED_LANGS.includes(stored) ? stored : DEFAULT_LANG;
  }
}
