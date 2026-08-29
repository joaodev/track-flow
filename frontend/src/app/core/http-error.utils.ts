import { HttpErrorResponse } from '@angular/common/http';

export function extractErrorCode(error: unknown): string {
  if (error instanceof HttpErrorResponse && typeof error.error?.errorCode === 'string') {
    return error.error.errorCode;
  }
  return 'UNKNOWN_ERROR';
}
