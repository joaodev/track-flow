import { Lang } from '../translation.service';

export const CURRENCY_BY_LANG: Record<Lang, { locale: string; currencyCode: string }> = {
  pt: { locale: 'pt-BR', currencyCode: 'BRL' },
  en: { locale: 'en-US', currencyCode: 'USD' },
  es: { locale: 'es-ES', currencyCode: 'EUR' },
};

export function formatCurrency(value: number, lang: Lang): string {
  const { locale, currencyCode } = CURRENCY_BY_LANG[lang];
  return new Intl.NumberFormat(locale, { style: 'currency', currency: currencyCode }).format(value);
}
