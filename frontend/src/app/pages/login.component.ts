import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="wrapper">
      <div class="card box">
        <h1>Cine<span style="color:var(--accent)">Verse</span></h1>
        <p class="sub">Descubra, organize e compartilhe seu universo de filmes e séries.</p>

        <input *ngIf="isRegister" [(ngModel)]="name" placeholder="Seu nome" />
        <input [(ngModel)]="email" type="email" placeholder="E-mail" />
        <input [(ngModel)]="password" type="password" placeholder="Senha (mín. 8 caracteres)"
               (keyup.enter)="submit()" />

        <p class="ok" *ngIf="justReset">Senha alterada! Faça login com a nova senha.</p>
        <p class="error" *ngIf="error">{{ error }}</p>

        <button (click)="submit()" [disabled]="loading">
          {{ loading ? 'Aguarde…' : (isRegister ? 'Criar conta grátis' : 'Entrar') }}
        </button>
        <button class="secondary" (click)="isRegister = !isRegister; error = ''">
          {{ isRegister ? 'Já tenho conta' : 'Criar nova conta' }}
        </button>
        <a *ngIf="!isRegister" routerLink="/esqueci-senha" class="link">Esqueci minha senha</a>
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
    .ok { color: var(--accent); font-size: 13px; }
    .link { color: var(--muted); font-size: 13px; text-align: center; text-decoration: none; }
  `]
})
export class LoginComponent implements OnInit {
  name = ''; email = ''; password = '';
  isRegister = false; loading = false; error = ''; justReset = false;

  constructor(private auth: AuthService, private router: Router, private route: ActivatedRoute) {}

  ngOnInit() {
    this.justReset = this.route.snapshot.queryParamMap.get('reset') === '1';
  }

  submit() {
    this.loading = true; this.error = '';
    const obs = this.isRegister
      ? this.auth.register(this.name, this.email, this.password)
      : this.auth.login(this.email, this.password);

    obs.subscribe({
      next: () => this.auth.loadProfiles().subscribe(() => this.router.navigate(['/home'])),
      error: (err) => {
        this.loading = false;
        this.error = err.error?.error ?? (err.status === 403 ? 'E-mail ou senha inválidos.' : 'Falha ao conectar.');
      }
    });
  }
}
