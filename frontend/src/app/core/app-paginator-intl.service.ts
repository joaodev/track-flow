import { effect, inject, Injectable } from '@angular/core';
import { MatPaginatorIntl } from '@angular/material/paginator';
import { TranslationService } from './translation.service';

@Injectable()
export class AppPaginatorIntl extends MatPaginatorIntl {
  private translation = inject(TranslationService);

  constructor() {
    super();
    effect(() => {
      this.translation.currentLang();
      this.itemsPerPageLabel = this.translation.t('common.pagination.itemsPerPage');
      this.nextPageLabel = this.translation.t('common.pagination.nextPageLabel');
      this.previousPageLabel = this.translation.t('common.pagination.previousPage');
      this.firstPageLabel = this.translation.t('common.pagination.firstPageLabel');
      this.lastPageLabel = this.translation.t('common.pagination.lastPageLabel');
      this.changes.next();
    });
  }

  override getRangeLabel = (page: number, pageSize: number, length: number): string => {
    const of = this.translation.t('common.pagination.of');
    if (length === 0 || pageSize === 0) {
      return `0 ${of} ${length}`;
    }
    const startIndex = page * pageSize;
    const endIndex = startIndex < length ? Math.min(startIndex + pageSize, length) : startIndex + pageSize;
    return `${startIndex + 1} - ${endIndex} ${of} ${length}`;
  };
}
