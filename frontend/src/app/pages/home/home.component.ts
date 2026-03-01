import { Component, OnInit, AfterViewInit } from '@angular/core';
import {ContactRequest, ContactService} from "../../core/services/contact.service";

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit, AfterViewInit {

    constructor(private contactService: ContactService) { }

    ngOnInit(): void {
    }

    ngAfterViewInit(): void {
        // ─── SCROLL ANIMATIONS ───
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(e => {
                if (e.isIntersecting) {
                    e.target.classList.add('visible');
                }
            });
        }, { threshold: 0.1 });

        document.querySelectorAll('.reveal').forEach(el => observer.observe(el));
    }

    handleContact(e: Event) {
        e.preventDefault();
        const target = e.target as HTMLFormElement;
        const btn = target.querySelector('button');
        if(!btn)return;
      const originalText = btn.textContent;
      const formData = new FormData(target);
      const request: ContactRequest = {
        name: formData.get('name') as string,
        email: formData.get('email') as string,
        subject: formData.get('subject') as string,
        message: formData.get('message') as string
      };
      btn.disabled = true;
      btn.textContent = 'Sending...';

      this.contactService.sendMessage(request).subscribe({
        next: () => {
          btn.textContent = '✅ Message Sent!';
          btn.style.background = 'var(--accent)';

          setTimeout(() => {
            btn.textContent = originalText;
            btn.style.background = '';
            btn.disabled = false;
            target.reset();
          }, 3000);
        },
        error: () => {
          btn.textContent = '❌ Failed. Try Again';
          btn.style.background = 'red';

          setTimeout(() => {
            btn.textContent = originalText;
            btn.style.background = '';
            btn.disabled = false;
          }, 3000);
        }
      });
    }
}
