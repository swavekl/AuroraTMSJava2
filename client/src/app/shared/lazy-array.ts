import {animationFrameScheduler, from, Observable, of, scheduled} from 'rxjs';
import {bufferCount, concatMap, delay, mergeMap, scan, tap} from 'rxjs/operators';

/**
 * Array which when piped to from another array will incrementally release more chunks of size (batchSize)
 *
 * @param delayMs how long to wait before releasing another batch
 * @param batchSize size of each batch
 */
export function lazyArray<T>(
  delayMs = 0,
  batchSize = 50
) {
  let isFirstEmission = true;

  return (source$: Observable<T[]>) => {
    return source$.pipe(
      mergeMap((items) => {
        if (!isFirstEmission) {
          return of(items);
        }

        const items$ = from(items);

        return items$.pipe(
          bufferCount(batchSize),
          concatMap((value, index) => {
            const delayed = delay(index * delayMs);
            return scheduled(of(value), animationFrameScheduler).pipe(delayed);
          }),
          scan((acc: T[], steps: T[]) => {
            return [...acc, ...steps];
          }, []),
          tap((scannedItems: T[]) => {
            const scanDidComplete = scannedItems.length === items.length;

            if (scanDidComplete) {
              isFirstEmission = false;
            }
          }),
        );
      }),
    );
  };
}
