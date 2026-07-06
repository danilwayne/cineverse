import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login.component';
import { HomeComponent } from './pages/home.component';
import { TitleComponent } from './pages/title.component';
import { ListsComponent } from './pages/lists.component';
import { authGuard } from './services/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'home', component: HomeComponent, canActivate: [authGuard] },
  { path: 'title/:type/:id', component: TitleComponent, canActivate: [authGuard] },
  { path: 'lists', component: ListsComponent, canActivate: [authGuard] },
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: '**', redirectTo: 'home' }
];
