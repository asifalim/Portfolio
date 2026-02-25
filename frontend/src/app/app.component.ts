import {Component, OnInit} from '@angular/core';
import {Router} from "@angular/router";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit{
  title = 'portfolio-frontend';
  isChatPage = false;

  constructor(private router:Router) {
    this.router.events.subscribe(event => {
      // @ts-ignore
      if (event.constructor.name === 'NavigationEnd') {
        // @ts-ignore
        this.isChatPage = event.urlAfterRedirects.includes('/chat');
      }
    });
  }

  ngOnInit(): void {
      console.log('hi ngOnInit');
  }
  goToChatBot(){
    console.log('ai page');
    this.router.navigateByUrl('/chat')
  }
}
