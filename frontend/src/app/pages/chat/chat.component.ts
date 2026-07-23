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

  constructor(private chatService: ChatService) {}

  ngOnInit(): void {
    console.log('ai chat open');
  }

  onInput(event: Event) {
    const el = event.target as HTMLTextAreaElement;
    this.inputText = el.value;

    el.style.height = 'auto';
    el.style.height = Math.min(el.scrollHeight, 120) + 'px';
  }

  onInputFocus() {
    // On mobile, scroll input into view after keyboard opens
    setTimeout(() => {
      const inputArea = document.querySelector('.chat-input-area') as HTMLElement;
      if (inputArea) {
        inputArea.scrollIntoView({ behavior: 'smooth', block: 'end' });
      }
      this.scrollToBottom();
    }, 300);
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

    // Build history: all messages BEFORE the one we just pushed (the current user turn).
    // We skip messages[0] (the initial greeting) because it's a UI-only message, not
    // part of the real conversation. We send everything between that and the current message.
    const history = this.messages.slice(1, -1).map(m => ({
      role: m.role as 'user' | 'assistant',
      content: m.content
    }));

    const assistantMessage: ChatMessage = { role: 'assistant', content: '' };
    let firstChunk = true;

    const typingQueue: string[] = [];
    let isTypingEffectRunning = false;

    const processTypingQueue = () => {
      if (typingQueue.length === 0) {
        isTypingEffectRunning = false;
        return;
      }
      isTypingEffectRunning = true;
      assistantMessage.content += typingQueue.shift();
      this.scrollToBottom();
      setTimeout(processTypingQueue, 25); // 25ms per chunk for a natural reading speed
    };

    this.chatService.streamMessage({ message, history }, (chunk: string) => {
      if (firstChunk) {
        this.isTyping = false;
        this.messages.push(assistantMessage);
        firstChunk = false;
      }

      // Groq is so fast it often sends everything in just a few bursts.
      // We queue the chunks and render them at a natural speed.
      typingQueue.push(chunk);
      if (!isTypingEffectRunning) {
        processTypingQueue();
      }
    }).then(() => {
      this.isTyping = false;
      if (firstChunk) {
        this.messages.push({
          role: 'assistant',
          content: "Sorry, I couldn't generate a response. Please try again!"
        });
        this.scrollToBottom();
      }
    }).catch(err => {
      this.isTyping = false;
      if (firstChunk) {
        this.messages.push({
          role: 'assistant',
          content: this.getFallback(message)
        });
        this.scrollToBottom();
      }
    });
  }

  getFallback(msg: string) {
    return "That's a great question! To give you the most accurate answer, I'd recommend reaching out to Alim directly at asifalimnstu@gmail.com. He usually responds within 24 hours and loves connecting with people in the tech community!";
  }

}
