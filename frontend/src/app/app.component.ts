import { Component, OnInit, AfterViewInit } from '@angular/core';
import { Router } from "@angular/router";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit, AfterViewInit {
  title = 'portfolio-frontend';
  isChatPage = false;
  isMobileMenuOpen = false;

  constructor(private router: Router) {
    this.router.events.subscribe(event => {
      if (event.constructor.name === 'NavigationEnd') {
        // @ts-ignore
        const url = event.urlAfterRedirects || event.url;
        this.isChatPage = url.includes('/chat');
        // Close mobile menu on navigation
        this.isMobileMenuOpen = false;
      }
    });
  }

  ngOnInit(): void {
    console.log('hi ngOnInit');
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

    // ─── SMOOTH SCROLL FOR HASH LINKS ───
    // This can also be done via CSS: html { scroll-behavior: smooth; }
  }

  handleContact(e: Event) {
    e.preventDefault();
    const target = e.target as HTMLFormElement;
    const btn = target.querySelector('button');
    if (btn) {
      const originalText = btn.textContent;
      btn.textContent = '✅ Message Sent!';
      btn.style.background = 'var(--accent)';
      setTimeout(() => {
        btn.textContent = originalText;
        btn.style.background = '';
        target.reset();
      }, 3000);
    }
  }
  toggleMobileMenu() {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }

  goToChatBot() {
    this.router.navigateByUrl('/chat');
  }

  showPage(page: string) {
    if (page === 'main') {
      this.router.navigateByUrl('/');
    } else if (page === 'chat') {
      this.router.navigateByUrl('/chat');
    }
  }
}
