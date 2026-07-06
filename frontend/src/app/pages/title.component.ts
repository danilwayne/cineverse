import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ApiService, IMG } from '../services/api.service';

@Component({
  selector: 'app-title',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="detail" *ngIf="data">
      <img class="poster-img" [src]="img(data.title.posterPath)" [alt]="data.title.name" />
      <div class="info">
        <h1>{{ data.title.name }}</h1>
        <p class="meta">
          <span class="pill" *ngFor="let g of data.title.genres">{{ g }}</span>
          <span class="pill" *ngIf="data.title.voteAverage">★ {{ data.title.voteAverage }}</span>
        </p>
        <p class="overview">{{ data.title.overview }}</p>

        <h3>Onde assistir (legalmente)</h3>
        <p class="providers" *ngIf="data.providers?.length; else noProviders">
          <a class="pill provider" *ngFor="let p of data.providers"
             [href]="p.url" target="_blank" rel="noopener">{{ p.provider }} · {{ p.kind }}</a>
        </p>
        <ng-template #noProviders>
          <p class="muted">Nenhum provedor encontrado para o Brasil.</p>
        </ng-template>

        <div class="actions">
          <button (click)="setStatus('WATCHED')">✓ Já assisti</button>
          <button class="ghost" (click)="setStatus('WATCHING')">▶ Assistindo</button>
          <button class="ghost" (click)="setStatus('PLANNED')">+ Quero ver</button>
          <select [(ngModel)]="selectedList" class="list-select">
            <option [ngValue]="null">Adicionar à lista…</option>
            <option *ngFor="let l of lists" [ngValue]="l.id">{{ l.name }}</option>
          </select>
          <button class="secondary" (click)="addToList()" [disabled]="!selectedList">Adicionar</button>
        </div>
        <p class="ok" *ngIf="feedback">{{ feedback }}</p>

        <h3 style="margin-top:24px">Avaliações</h3>
        <div class="review-form">
          <select [(ngModel)]="rating">
            <option *ngFor="let n of ratings" [ngValue]="n">{{ n }}/10</option>
          </select>
          <textarea [(ngModel)]="reviewBody" rows="2" placeholder="O que você achou? (opcional)"></textarea>
          <button (click)="submitReview()">Publicar avaliação</button>
        </div>
        <div class="card review" *ngFor="let r of reviews">
          <strong>★ {{ r.rating }}/10</strong>
          <p *ngIf="r.body">{{ r.body }}</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .detail { display: grid; grid-template-columns: 260px 1fr; gap: 26px; }
    @media (max-width: 700px) { .detail { grid-template-columns: 1fr; } }
    .poster-img { width: 100%; border-radius: 12px; border: 1px solid var(--border); }
    h1 { font-size: 26px; margin-bottom: 10px; }
    h3 { font-size: 15px; margin: 18px 0 8px; }
    .overview { color: var(--muted); margin-top: 12px; }
    .provider { text-decoration: none; }
    .provider:hover { border-color: var(--accent); color: var(--accent); }
    .actions { display: flex; flex-wrap: wrap; gap: 10px; margin-top: 18px; align-items: center; }
    .list-select { width: auto; }
    .ok { color: var(--accent); font-size: 13px; margin-top: 8px; }
    .muted { color: var(--muted); font-size: 13px; }
    .review-form { display: flex; flex-direction: column; gap: 8px; margin-bottom: 14px; max-width: 480px; }
    .review-form select { width: 100px; }
    .review { margin-bottom: 10px; padding: 12px 16px; }
  `]
})
export class TitleComponent implements OnInit {
  data: any;
  lists: any[] = [];
  reviews: any[] = [];
  selectedList: number | null = null;
  rating = 8;
  ratings = [1,2,3,4,5,6,7,8,9,10];
  reviewBody = '';
  feedback = '';

  constructor(private route: ActivatedRoute, private api: ApiService) {}

  ngOnInit() {
    const type = this.route.snapshot.paramMap.get('type')!;
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.api.details(type, id).subscribe(d => {
      this.data = d;
      this.api.reviews(d.title.id).subscribe(r => this.reviews = r);
    });
    this.api.watchlists().subscribe(l => this.lists = l);
  }

  img(path: string) { return path ? IMG + path : ''; }

  setStatus(status: string) {
    this.api.setActivity(this.data.title.id, status)
      .subscribe(() => this.feedback = 'Salvo! +XP 🎉');
  }

  addToList() {
    if (!this.selectedList) return;
    this.api.addToWatchlist(this.selectedList, this.data.title.id)
      .subscribe(() => this.feedback = 'Adicionado à lista! +XP 🎉');
  }

  submitReview() {
    this.api.postReview(this.data.title.id, this.rating, this.reviewBody).subscribe(() => {
      this.feedback = 'Avaliação publicada! +XP 🎉';
      this.reviewBody = '';
      this.api.reviews(this.data.title.id).subscribe(r => this.reviews = r);
    });
  }
}
