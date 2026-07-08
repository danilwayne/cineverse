import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="wrapper">
      <div class="card box">
        <h1>Cine<span style="color:var(--accent)">Verse</span></h1>
        <p class="sub">Esqueceu a senha? Informe seu e-mail e enviaremos um link para criar uma nova.</p>

        <ng-container *ngIf="!sent">
          <input [(ngModel)]="email" type="email" placeholder="E-mail"
                 (keyup.enter)="submit()" />
          <p class="error" *ngIf="error">{{ error }}</p>
          <button (click)="submit()" [disabled]="loading">
            {{ loading ? 'Enviando…' : 'Enviar link de redefinição' }}
          </button>
        </ng-container>

        <p class="ok" *ngIf="sent">{{ message }}</p>

        <div class="divider"><span>ou</span></div>
        <a [href]="whatsappLink" target="_blank" rel="noopener" class="whats">
          Falar com o suporte no WhatsApp
        </a>
        <p class="hint">Suporte temporário para redefinir a senha manualmente.</p>

        <a routerLink="/login" class="link">← Voltar para o login</a>
      </div>
    </div>
  `,
  styles: [`
    .wrapper { min-height: 100vh; display: grid; place-items: center;
               background: radial-gradient(ellipse at top, #1c1730 0%, var(--bg) 60%); }
    .box { width: 370px; display: flex; flex-direction: column; gap: 12px; }
    h1 { font-size: 28px; }
    .sub { color: var(--muted); font-size: 13px; margin-bottom: 8px; }
    .error { color: var(--danger); font-size: 13px; }
    .ok { color: var(--accent); font-size: 14px; }
    .link { color: var(--muted); font-size: 13px; text-align: center; text-decoration: none; }
    .divider { display: flex; align-items: center; gap: 10px; color: var(--muted); font-size: 12px; }
    .divider::before, .divider::after { content: ''; flex: 1; height: 1px; background: var(--border); }
    .whats { display: block; text-align: center; text-decoration: none;
             background: #25D366; color: #05320f; font-weight: 700;
             padding: 11px; border-radius: 8px; }
    .hint { color: var(--muted); font-size: 12px; text-align: center; }
  `]
})
export class ForgotPasswordComponent {
  email = '';
  loading = false;
  error = '';
  sent = false;
  message = '';

  constructor(private auth: AuthService) {}

  /** wa.me com mensagem pré-preenchida (inclui o e-mail digitado, se houver). */
  get whatsappLink(): string {
    const msg = `Olá! Preciso de ajuda para redefinir a senha da minha conta CineVerse.`
      + (this.email ? ` Meu e-mail de cadastro é: ${this.email}` : '');
    return `https://wa.me/556194265625?text=${encodeURIComponent(msg)}`;
  }

  submit() {
    if (!this.email) { this.error = 'Digite seu e-mail.'; return; }
    this.loading = true; this.error = '';
    this.auth.forgotPassword(this.email).subscribe({
      next: (res) => { this.sent = true; this.message = res.message; },
      error: () => {
        this.loading = false;
        this.error = 'Falha ao conectar. Tente novamente.';
      }
    });
  }
}
