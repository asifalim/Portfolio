# Angular Portfolio Frontend

## Project Structure
```
portfolio-frontend/
├── src/
│   ├── app/
│   │   ├── core/
│   │   │   ├── services/
│   │   │   │   ├── chat.service.ts
│   │   │   │   └── contact.service.ts
│   │   │   └── models/
│   │   │       ├── chat.model.ts
│   │   │       └── contact.model.ts
│   │   ├── shared/
│   │   │   └── components/
│   │   │       └── navbar/
│   │   ├── pages/
│   │   │   ├── home/
│   │   │   ├── about/
│   │   │   ├── experience/
│   │   │   ├── projects/
│   │   │   ├── skills/
│   │   │   ├── achievements/
│   │   │   ├── contact/
│   │   │   └── chat/          ← AI Chatbot page
│   │   ├── app.component.ts
│   │   ├── app.routes.ts
│   │   └── app.config.ts
│   ├── environments/
│   │   ├── environment.ts
│   │   └── environment.prod.ts
│   ├── styles.scss
│   └── index.html
├── angular.json
└── package.json
```

## Setup Commands
```bash
ng new portfolio-frontend --routing=true --style=scss
cd portfolio-frontend
npm install @angular/material @angular/cdk @angular/animations
npm install @angular/router
```

## Key Angular Service: chat.service.ts
The ChatService communicates with the Spring Boot backend:

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
}

export interface ChatRequest {
  message: string;
  history: ChatMessage[];
}

export interface ChatResponse {
  message: string;
  role: string;
  success: boolean;
  error?: string;
}

@Injectable({ providedIn: 'root' })
export class ChatService {
  private apiUrl = `${environment.apiUrl}/api/v1/chat`;

  constructor(private http: HttpClient) {}

  sendMessage(request: ChatRequest): Observable<ChatResponse> {
    return this.http.post<ChatResponse>(this.apiUrl, request);
  }
}
```

## Key Angular Service: contact.service.ts
```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ContactRequest {
  name: string;
  email: string;
  subject?: string;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class ContactService {
  private apiUrl = `${environment.apiUrl}/api/v1/contact`;

  constructor(private http: HttpClient) {}

  sendMessage(request: ContactRequest): Observable<any> {
    return this.http.post(this.apiUrl, request);
  }
}
```

## environment.ts
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080'
};
```

## environment.prod.ts
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://your-api-domain.com'
};
```

## app.routes.ts
```typescript
import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';
import { ChatComponent } from './pages/chat/chat.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'chat', component: ChatComponent },
  { path: '**', redirectTo: '' }
];
```

## Chat Component: chat.component.ts
```typescript
import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { ChatService, ChatMessage } from '../../core/services/chat.service';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.scss']
})
export class ChatComponent {
  messages: ChatMessage[] = [
    { role: 'assistant', content: "Hey! I'm Alim's AI Agent. Ask me anything about Alim's skills, projects, or experience! 👋" }
  ];
  
  inputText = '';
  isTyping = false;

  suggestions = [
    "What's your tech stack?",
    "Tell me about your experience",
    "What projects have you built?",
    "Are you open to work?"
  ];

  constructor(private chatService: ChatService) {}

  sendMessage(text?: string) {
    const message = text || this.inputText.trim();
    if (!message) return;

    this.messages.push({ role: 'user', content: message });
    this.inputText = '';
    this.isTyping = true;

    const history = this.messages.slice(0, -1).map(m => ({
      role: m.role as 'user' | 'assistant',
      content: m.content
    }));

    this.chatService.sendMessage({ message, history }).subscribe({
      next: (res) => {
        this.isTyping = false;
        if (res.success) {
          this.messages.push({ role: 'assistant', content: res.message });
        }
      },
      error: () => {
        this.isTyping = false;
        this.messages.push({
          role: 'assistant',
          content: "I'm having trouble connecting. Please try again!"
        });
      }
    });
  }
}
```
