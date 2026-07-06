import { HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  // Anexa o access token atual (lido na hora, para pegar o token renovado no retry).
  const withAuth = (r: HttpRequest<unknown>) => {
    const token = sessionStorage.getItem('token');
    return token ? r.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : r;
  };

  return next(withAuth(req)).pipe(
    catchError((err: HttpErrorResponse) => {
      const isAuthCall = req.url.includes('/api/auth/');
      // Só tenta renovar em 401 (token expirado), fora das próprias rotas de auth,
      // e apenas se houver um refresh token guardado.
      if (err.status !== 401 || isAuthCall || !sessionStorage.getItem('refresh')) {
        return throwError(() => err);
      }
      return auth.refresh().pipe(
        switchMap(() => next(withAuth(req))),   // repete a requisição com o token novo
        catchError(refreshErr => {              // refresh falhou -> sessão acabou
          auth.logout();
          router.navigate(['/login']);
          return throwError(() => refreshErr);
        })
      );
    })
  );
};
