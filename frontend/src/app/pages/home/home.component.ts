import { Component, OnInit, AfterViewInit } from '@angular/core';

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit, AfterViewInit {

    constructor() { }

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
}
