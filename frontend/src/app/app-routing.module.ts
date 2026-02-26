import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ChatComponent } from "./pages/chat/chat.component";
import { HomeComponent } from "./pages/home/home.component";

const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'chat', component: ChatComponent },
  { path: 'about', component: HomeComponent },
  { path: 'education', component: HomeComponent },
  { path: 'experience', component: HomeComponent },
  { path: 'skills', component: HomeComponent },
  { path: 'projects', component: HomeComponent },
  { path: 'achievements', component: HomeComponent },
  { path: 'contact', component: HomeComponent },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
