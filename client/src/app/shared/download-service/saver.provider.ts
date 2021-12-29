import {InjectionToken} from '@angular/core';
import { saveAs } from 'file-saver';

/**
 * Wrapper for file-saver library
 */
export type Saver = (blob: Blob, filename?: string) => void;

export const SAVER = new InjectionToken<Saver>('saver');

export function getSaver(): Saver {
  return saveAs;
}
