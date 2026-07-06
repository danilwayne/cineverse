import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService, IMG, Title } from '../services/api.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="search-bar">
      <input [(ngModel)]="query" placeholder="Buscar filmes e séries…" (keyup.enter)="search()" />
      <button (click)="search()">Buscar</button>
    </div>

    <ng-container *ngIf="searchResults; else discover">
      <h2>Resultados para "{{ lastQuery }}"</h2>
      <div class="poster-grid">
        <div class="poster" *ngFor="let item of searchResults" (click)="open(item)">
          <img [src]="poster(item.poster_path)" [alt]="item.title || item.name" loading="lazy" />
          <div class="p-name">{{ item.title || item.name }}</div>
        </div>
      </div>
      <button class="secondary" style="margin-top:16px" (click)="searchResults = null">← Voltar</button>
    </ng-container>

    <ng-template #discover>
      <section *ngIf="recs.length">
        <h2>Recomendado para você <span class="pill">IA</span></h2>
        <div class="poster-grid">
          <div class="poster" *ngFor="let t of recs" (click)="openLocal(t)">
            <img [src]="poster(t.posterPath)" [alt]="t.name" loading="lazy" />
            <div class="p-name">{{ t.name }}</div>
          </div>
        </div>
      </section>

      <section style="margin-top:28px">
        <h2>Em alta esta semana</h2>
        <div class="poster-grid">
          <div class="poster" *ngFor="let item of trending" (click)="open(item)">
            <img [src]="poster(item.poster_path)" [alt]="item.title || item.name" loading="lazy" />
            <div class="p-name">{{ item.title || item.name }}</div>
          </div>
        </div>
      </section>
    </ng-template>
  `,
  styles: [`
    .search-bar { display: flex; gap: 10px; margin-bottom: 24px; }
    h2 { font-size: 18px; margin-bottom: 14px; }
  `]
})
export class HomeComponent implements OnInit {
  query = ''; lastQuery = '';
  trending: any[] = [];
  recs: Title[] = [];
  searchResults: any[] | null = null;

  constructor(private api: ApiService, private router: Router) {}

  ngOnInit() {
    this.api.trending().subscribe(res => this.trending = res.results ?? []);
    this.api.recommendations().subscribe(recs => this.recs = recs.filter(t => t.posterPath));
  }

  search() {
    if (!this.query.trim()) return;
    this.lastQuery = this.query;
    this.api.search(this.query).subscribe(res =>
      this.searchResults = (res.results ?? []).filter((r: any) =>
        r.media_type !== 'person' && r.poster_path));
  }

  poster(path: string) {
    return path ? IMG + path : 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg"/>';
  }

  open(item: any) {
    this.router.navigate(['/title', item.media_type ?? 'movie', item.id]);
  }

  openLocal(t: Title) {
    this.router.navigate(['/title', t.mediaType, t.tmdbId]);
  }
}
