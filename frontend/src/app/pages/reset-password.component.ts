import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="wrapper">
      <div class="card box">
        <h1>Cine<span style="color:var(--accent)">Verse</span></h1>
        <p class="sub">Crie sua nova senha.</p>

        <ng-container *ngIf="token; else semToken">
          <input [(ngModel)]="password" type="password" placeholder="Nova senha (mín. 8 caracteres)" />
          <input [(ngModel)]="confirm" type="password" placeholder="Repita a nova senha"
                 (keyup.enter)="submit()" />
          <p class="error" *ngIf="error">{{ error }}</p>
          <button (click)="submit()" [disabled]="loading">
            {{ loading ? 'Salvando…' : 'Salvar nova senha' }}
          </button>
        </ng-container>

        <ng-template #semToken>
          <p class="error">Link inválido. Peça um novo em "Esqueci minha senha".</p>
        </ng-template>

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
    .link { color: var(--muted); font-size: 13px; text-align: center; text-decoration: none; }
  `]
})
export class ResetPasswordComponent implements OnInit {
  token = '';
  password = '';
  confirm = '';
  loading = false;
  error = '';

  constructor(private auth: AuthService, private route: ActivatedRoute, private router: Router) {}

  ngOnInit() {
    this.token = this.route.snapshot.queryParamMap.get('token') ?? '';
  }

  submit() {
    if (this.password.length < 8) { this.error = 'A senha precisa ter ao menos 8 caracteres.'; return; }
    if (this.password !== this.confirm) { this.error = 'As senhas não conferem.'; return; }

    this.loading = true; this.error = '';
    this.auth.resetPassword(this.token, this.password).subscribe({
      next: () => this.router.navigate(['/login'], { queryParams: { reset: '1' } }),
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message ?? 'Não foi possível redefinir. O link pode ter expirado.';
      }
    });
  }
}
