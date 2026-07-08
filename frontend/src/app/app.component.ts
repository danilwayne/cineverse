import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { AuthService } from './services/auth.service';
import { GamificationWidgetComponent } from './pages/gamification-widget.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, GamificationWidgetComponent],
  template: `
    <nav *ngIf="auth.isLoggedIn()">
      <a routerLink="/home" class="logo">Cine<span>Verse</span></a>
      <a routerLink="/home">Explorar</a>
      <a routerLink="/lists">Minhas listas</a>
      <a routerLink="/configuracoes">Configurações</a>
      <span class="spacer"></span>
      <app-gamification-widget />
      <button class="secondary" (click)="logout()">Sair</button>
    </nav>
    <main><router-outlet /></main>
  `,
  styles: [`
    nav {
      display: flex; align-items: center; gap: 22px;
      padding: 14px 28px;
      background: var(--surface);
      border-bottom: 1px solid var(--border);
      position: sticky; top: 0; z-index: 10;
    }
    .logo {
      font-family: 'Space Grotesk', sans-serif;
      font-weight: 700; font-size: 19px;
      color: var(--text); text-decoration: none;
    }
    .logo span { color: var(--accent); }
    nav a:not(.logo) { color: var(--muted); text-decoration: none; font-size: 14px; }
    nav a:not(.logo):hover { color: var(--text); }
    .spacer { flex: 1; }
    main { max-width: 1150px; margin: 0 auto; padding: 26px 20px; }
  `]
})
export class AppComponent {
  constructor(public auth: AuthService, private router: Router) {}

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
