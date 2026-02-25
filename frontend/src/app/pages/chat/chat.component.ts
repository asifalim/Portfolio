import { Component, OnInit } from '@angular/core';
import { ChatMessage, ChatService } from "../../core/services/chat.service";

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.scss']
})
export class ChatComponent implements OnInit {

  messages: ChatMessage[] = [
    { role: 'assistant', content: "Hey there! 👋 I'm Alim's AI Agent — a digital version of Asif Alim, Software Engineer. I can tell you about Alim's experience, skills, projects, education, and career goals. What would you like to know?" }
  ];

  inputText = '';
  isTyping = false;

  suggestions = [
    "What's your tech stack?",
    "Tell me about your experience",
    "What projects have you built?",
    "Are you open to work?"
  ];

  constructor(private chatService: ChatService) { }

  ngOnInit(): void {
    console.log('ai chat open');
  }

  onInput(event: Event) {
    const el = event.target as HTMLTextAreaElement;
    this.inputText = el.value;

    el.style.height = 'auto';
    el.style.height = Math.min(el.scrollHeight, 120) + 'px';
  }

  handleChatKey(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  scrollToBottom() {
    setTimeout(() => {
      const chatMessages = document.getElementById('chatMessages');
      if (chatMessages) {
        chatMessages.scrollTop = chatMessages.scrollHeight;
      }
    }, 50);
  }

  sendMessage(text?: string) {
    const message = text || this.inputText.trim();
    if (!message) return;

    this.messages.push({ role: 'user', content: message });
    this.inputText = '';

    // Reset textarea height
    const textarea = document.getElementById('chat-input') as HTMLTextAreaElement;
    if (textarea) {
      textarea.style.height = 'auto';
    }

    this.isTyping = true;
    this.scrollToBottom();

    const history = this.messages.slice(0, -1).map(m => ({
      role: m.role as 'user' | 'assistant',
      content: m.content
    }));

    this.chatService.sendMessage({ message, history }).subscribe({
      next: (res) => {
        this.isTyping = false;
        if (res) {
          this.messages.push({ role: 'assistant', content: res.response });
        }
        this.scrollToBottom();
      },
      error: () => {
        this.isTyping = false;
        this.messages.push({
          role: 'assistant',
          content: this.getFallback(message)
        })
        this.scrollToBottom();
      }
    });
  }

  getFallback(msg: string) {
    const lower = msg.toLowerCase();
    if (lower.includes('stack') || lower.includes('tech') || lower.includes('skill'))
      return "My main stack is Java + Spring Boot on the backend and Angular on the frontend, with PostgreSQL as my primary database. I also work with Docker, Redis, and CI/CD pipelines daily. I'd say backend is my strength, but I'm comfortable going full-stack!";
    if (lower.includes('experience') || lower.includes('work'))
      return "I'm currently at Brac IT Services as a Software Engineer, where I build REST APIs, handle authentication, and optimize database queries for a SaaS product serving 50k+ users. Before that, I interned at DataBridge Technologies for 6 months. About 1.5 years of total industry experience!";
    if (lower.includes('project'))
      return "I've built a few things I'm proud of! ShopStream is a full e-commerce platform, TaskFlow is a kanban board app with real-time collaboration, and you're literally using my AI Chatbot project right now 😄. Check the Projects section for more details!";
    if (lower.includes('open') || lower.includes('job') || lower.includes('hire') || lower.includes('available'))
      return "Yes, I'm open to new opportunities! I'm particularly interested in backend-heavy or full-stack roles where I can grow. I'm based in Dhaka but open to remote work too. Feel free to reach out at alex@example.com!";
    if (lower.includes('education') || lower.includes('degree') || lower.includes('university'))
      return "I graduated in 2024 with a B.Sc. in Computer Science and Telecommunication Engineering with a CGPA of 3.46/4.0. My final year project was on distributed microservices architecture — which is kind of how I fell in love with backend development!";
    if (lower.includes('goal') || lower.includes('future') || lower.includes('plan'))
      return "My goal is to grow into a Senior Engineer within the next 2-3 years, and eventually move into a tech lead role. I want to become someone who not only writes excellent code but also shapes engineering culture. I'm also really interested in system design and distributed systems!";
    if (lower.includes('hello') || lower.includes('hi') || lower.includes('hey'))
      return "Hey! Great to meet you! I'm Alim's AI Agent. Feel free to ask me about my skills, projects, experience, or anything else you'd like to know. What's on your mind? 😊";
    return "That's a great question! To give you the most accurate answer, I'd recommend reaching out to Alim directly at asifalimnstu@gmail.com. He usually responds within 24 hours and loves connecting with people in the tech community!";
  }

}
