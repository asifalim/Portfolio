import { Component } from '@angular/core';
import { Router, NavigationEnd } from "@angular/router";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'portfolio-frontend';
  isChatPage = false;
  isMobileMenuOpen = false;

  constructor(private router: Router) {
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        const url = event.urlAfterRedirects || event.url;
        this.isChatPage = url.includes('/chat');
        // Close mobile menu on navigation
        this.isMobileMenuOpen = false;

        // Handle scrolling for section paths
        const section = url.split('/').pop();
        if (section && section !== 'chat' && section !== '') {
          setTimeout(() => this.scrollToSection(section), 100);
        } else if (section === '' || !section) {
          window.scrollTo({ top: 0, behavior: 'smooth' });
        }
      }
    });
  }

  private scrollToSection(sectionId: string) {
    const el = document.getElementById(sectionId);
    if (el) {
      const offset = 64; // Navbar height
      const bodyRect = document.body.getBoundingClientRect().top;
      const elementRect = el.getBoundingClientRect().top;
      const elementPosition = elementRect - bodyRect;
      const offsetPosition = elementPosition - offset;

      window.scrollTo({
        top: offsetPosition,
        behavior: 'smooth'
      });
    }
  }

  toggleMobileMenu() {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }
}
