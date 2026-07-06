import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, finalize, shareReplay, tap } from 'rxjs';

interface Tokens { accessToken: string; refreshToken: string; name: string; plan: string; }
interface Profile { id: number; name: string; isKids: boolean; }

@Injectable({ providedIn: 'root' })
export class AuthService {
  private refreshInFlight?: Observable<Tokens>;

  constructor(private http: HttpClient) {}

  /** Renova o access token usando o refresh token. Compartilhado entre chamadas
   *  simultâneas (o backend rotaciona o refresh, então só pode haver 1 por vez). */
  refresh(): Observable<Tokens> {
    if (!this.refreshInFlight) {
      const refreshToken = sessionStorage.getItem('refresh');
      this.refreshInFlight = this.http.post<Tokens>('/api/auth/refresh', { refreshToken }).pipe(
        tap(t => this.store(t)),
        finalize(() => (this.refreshInFlight = undefined)),
        shareReplay(1)
      );
    }
    return this.refreshInFlight;
  }

  register(name: string, email: string, password: string) {
    return this.http.post<Tokens>('/api/auth/register', { name, email, password, locale: 'pt-BR' })
      .pipe(tap(t => this.store(t)));
  }

  login(email: string, password: string) {
    return this.http.post<Tokens>('/api/auth/login', { email, password })
      .pipe(tap(t => this.store(t)));
  }

  loadProfiles() {
    return this.http.get<Profile[]>('/api/profiles')
      .pipe(tap(list => {
        if (list.length && !sessionStorage.getItem('profileId')) {
          sessionStorage.setItem('profileId', String(list[0].id));
        }
      }));
  }

  private store(t: Tokens) {
    sessionStorage.setItem('token', t.accessToken);
    sessionStorage.setItem('refresh', t.refreshToken);
    sessionStorage.setItem('name', t.name);
    sessionStorage.setItem('plan', t.plan);
  }

  profileId() { return Number(sessionStorage.getItem('profileId') ?? 0); }
  token() { return sessionStorage.getItem('token'); }
  isLoggedIn() { return !!this.token(); }
  logout() { sessionStorage.clear(); }
}
