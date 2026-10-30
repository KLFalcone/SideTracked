import { HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';


/*
 * If any API call comes back "not signed in" (for example after
 * the session expires), tell the app so it can show the sign-in
 * screen. The sign-in endpoints themselves are left alone.
 */
export const SIGNED_OUT_EVENT = 'sidetracked:signed-out';

export const authInterceptor: HttpInterceptorFn = (request, next) =>
  next(request).pipe(
    catchError(error => {

      if (
        error.status === 401 &&
        !request.url.includes('/api/auth/')
      ) {
        window.dispatchEvent(new Event(SIGNED_OUT_EVENT));
      }

      return throwError(() => error);
    })
  );
