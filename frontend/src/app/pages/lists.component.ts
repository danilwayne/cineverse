import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService, IMG, Title } from '../services/api.service';

@Component({
  selector: 'app-lists',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <h2>Minhas listas</h2>
    <div class="new-list">
      <input [(ngModel)]="newName" placeholder="Nome da nova lista (ex.: Terror anos 80)"
             (keyup.enter)="create()" />
      <button (click)="create()">Criar</button>
    </div>

    <div class="card list" *ngFor="let list of lists">
      <div class="list-header">
        <h3>{{ list.name }}</h3>
        <div>
          <button class="secondary small" (click)="toggle(list)">
            {{ opened === list.id ? 'Fechar' : 'Ver títulos' }}
          </button>
          <button class="secondary small" (click)="share(list)">Compartilhar</button>
        </div>
      </div>
      <p class="share-link" *ngIf="shareSlugs[list.id]">
        Link público: <span class="slug">/api/public/lists/{{ shareSlugs[list.id] }}</span>
      </p>
      <div class="poster-grid" *ngIf="opened === list.id">
        <div class="poster" *ngFor="let t of items" (click)="open(t)">
          <img [src]="IMG + t.posterPath" [alt]="t.name" loading="lazy" />
          <div class="p-name">{{ t.name }}</div>
        </div>
        <p *ngIf="!items.length" class="muted">Lista vazia — adicione títulos pela página de detalhes.</p>
      </div>
    </div>
  `,
  styles: [`
    h2 { margin-bottom: 16px; }
    .new-list { display: flex; gap: 10px; margin-bottom: 20px; max-width: 480px; }
    .list { margin-bottom: 14px; }
    .list-header { display: flex; justify-content: space-between; align-items: center; }
    .small { padding: 6px 12px; font-size: 12px; margin-left: 8px; }
    .share-link { font-size: 12px; color: var(--muted); margin-top: 8px; }
    .slug { color: var(--accent); }
    .muted { color: var(--muted); font-size: 13px; }
    .poster-grid { margin-top: 14px; }
  `]
})
export class ListsComponent implements OnInit {
  IMG = IMG;
  lists: any[] = [];
  items: Title[] = [];
  opened: number | null = null;
  newName = '';
  shareSlugs: Record<number, string> = {};

  constructor(private api: ApiService, private router: Router) {}

  ngOnInit() { this.load(); }

  load() { this.api.watchlists().subscribe(l => this.lists = l); }

  create() {
    if (!this.newName.trim()) return;
    this.api.createWatchlist(this.newName).subscribe(() => {
      this.newName = '';
      this.load();
    });
  }

  toggle(list: any) {
    if (this.opened === list.id) { this.opened = null; return; }
    this.opened = list.id;
    this.items = [];
    this.api.watchlistItems(list.id).subscribe(i => this.items = i);
  }

  share(list: any) {
    this.api.shareWatchlist(list.id).subscribe(res => this.shareSlugs[list.id] = res.slug);
  }

  open(t: Title) { this.router.navigate(['/title', t.mediaType, t.tmdbId]); }
}
