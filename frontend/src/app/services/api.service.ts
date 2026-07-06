import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthService } from './auth.service';

export const IMG = 'https://image.tmdb.org/t/p/w342';

export interface Title {
  id: number; tmdbId: number; mediaType: string; name: string;
  overview: string; posterPath: string; voteAverage: number; genres: string[];
}

export interface GamificationStatus {
  xp: number; level: number; streakDays: number; longestStreak: number;
  achievements: { code: string; name: string; description: string; icon: string }[];
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  constructor(private http: HttpClient, private auth: AuthService) {}

  private pid() { return this.auth.profileId(); }

  trending() { return this.http.get<any>('/api/catalog/trending'); }

  search(q: string) {
    return this.http.get<any>('/api/catalog/search', { params: { q, profileId: this.pid() } });
  }

  details(type: string, tmdbId: number) {
    return this.http.get<any>(`/api/catalog/${type}/${tmdbId}`, { params: { profileId: this.pid() } });
  }

  recommendations() {
    return this.http.get<Title[]>('/api/catalog/recommendations', { params: { profileId: this.pid() } });
  }

  watchlists() {
    return this.http.get<any[]>('/api/watchlists', { params: { profileId: this.pid() } });
  }

  createWatchlist(name: string) {
    return this.http.post<any>('/api/watchlists', { name }, { params: { profileId: this.pid() } });
  }

  addToWatchlist(listId: number, titleId: number) {
    return this.http.post<void>(`/api/watchlists/${listId}/items/${titleId}`, null,
      { params: { profileId: this.pid() } });
  }

  watchlistItems(listId: number) {
    return this.http.get<Title[]>(`/api/watchlists/${listId}/items`, { params: { profileId: this.pid() } });
  }

  shareWatchlist(listId: number) {
    return this.http.post<{ slug: string }>(`/api/watchlists/${listId}/share`, null,
      { params: { profileId: this.pid() } });
  }

  setActivity(titleId: number, status: string, season?: number, episode?: number) {
    return this.http.put<any>('/api/activity', { titleId, status, season, episode },
      { params: { profileId: this.pid() } });
  }

  reviews(titleId: number) { return this.http.get<any[]>(`/api/titles/${titleId}/reviews`); }

  postReview(titleId: number, rating: number, body: string) {
    return this.http.post<any>(`/api/titles/${titleId}/reviews`,
      { rating, body, spoiler: false }, { params: { profileId: this.pid() } });
  }

  gamification() {
    return this.http.get<GamificationStatus>('/api/gamification/me', { params: { profileId: this.pid() } });
  }
}
