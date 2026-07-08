import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="card box">
      <h2>Configurações</h2>

      <section>
        <h3>Trocar senha</h3>
        <input [(ngModel)]="current" type="password" placeholder="Senha atual" />
        <input [(ngModel)]="novaSenha" type="password" placeholder="Nova senha (mín. 8 caracteres)" />
        <input [(ngModel)]="confirm" type="password" placeholder="Repita a nova senha"
               (keyup.enter)="submit()" />

        <p class="error" *ngIf="error">{{ error }}</p>
        <p class="ok" *ngIf="ok">{{ ok }}</p>

        <button (click)="submit()" [disabled]="loading">
          {{ loading ? 'Salvando…' : 'Salvar nova senha' }}
        </button>
      </section>
    </div>
  `,
  styles: [`
    .box { max-width: 440px; display: flex; flex-direction: column; gap: 12px; }
    section { display: flex; flex-direction: column; gap: 10px; margin-top: 8px; }
    h3 { font-size: 15px; color: var(--muted); font-weight: 600; }
    .error { color: var(--danger); font-size: 13px; }
    .ok { color: var(--accent); font-size: 13px; }
  `]
})
export class SettingsComponent {
  current = '';
  novaSenha = '';
  confirm = '';
  loading = false;
  error = '';
  ok = '';

  constructor(private auth: AuthService) {}

  submit() {
    this.error = ''; this.ok = '';
    if (!this.current) { this.error = 'Digite sua senha atual.'; return; }
    if (this.novaSenha.length < 8) { this.error = 'A nova senha precisa ter ao menos 8 caracteres.'; return; }
    if (this.novaSenha !== this.confirm) { this.error = 'As senhas novas não conferem.'; return; }

    this.loading = true;
    this.auth.changePassword(this.current, this.novaSenha).subscribe({
      next: (res) => {
        this.loading = false;
        this.ok = res.message;
        this.current = this.novaSenha = this.confirm = '';
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message ?? 'Não foi possível alterar a senha.';
      }
    });
  }
}
