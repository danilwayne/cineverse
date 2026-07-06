import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService, GamificationStatus } from '../services/api.service';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-gamification-widget',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="widget" *ngIf="status" [title]="tooltip()">
      <span class="streak" *ngIf="status.streakDays > 0">🔥 {{ status.streakDays }}</span>
      <span class="level">Nv. {{ status.level }}</span>
      <span class="xp">{{ status.xp }} XP</span>
    </div>
  `,
  styles: [`
    .widget {
      display: flex; gap: 10px; align-items: center;
      background: var(--surface-2);
      border: 1px solid var(--border);
      border-radius: 999px;
      padding: 6px 14px;
      font-size: 13px;
    }
    .level { color: var(--accent); font-weight: 600; }
    .xp { color: var(--muted); }
  `]
})
export class GamificationWidgetComponent implements OnInit {
  status?: GamificationStatus;

  constructor(private api: ApiService, private auth: AuthService) {}

  ngOnInit() {
    if (this.auth.profileId() > 0) {
      this.fetch();
    } else {
      // Logo após o login o profileId ainda não foi gravado; carrega os perfis antes.
      this.auth.loadProfiles().subscribe(() => {
        if (this.auth.profileId() > 0) this.fetch();
      });
    }
  }

  private fetch() {
    this.api.gamification().subscribe(s => this.status = s);
  }

  tooltip() {
    const names = this.status?.achievements.map(a => a.name).join(', ');
    return names ? 'Conquistas: ' + names : 'Ganhe XP avaliando e organizando títulos!';
  }
}
