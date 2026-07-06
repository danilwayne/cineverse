import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';

interface Tokens { accessToken: string; refreshToken: string; name: string; plan: string; }
interface Profile { id: number; name: string; isKids: boolean; }

@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(private http: HttpClient) {}

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
