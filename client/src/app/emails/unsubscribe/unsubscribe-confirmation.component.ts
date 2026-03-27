import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';

@Component({
  selector: 'app-unsubscribe-confirmation',
  template: `
    <div class="page">
      <div class="card">
        <h1>Email Preferences Updated</h1>
        <p [innerHTML]="message"></p>
      </div>
    </div>
  `,
  styles: [`
    .page {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 24px;
      background: #f5f7fb;
      color: #1e293b;
      font-family: "Segoe UI", Arial, sans-serif;
    }

    .card {
      width: 100%;
      max-width: 760px;
      background: #ffffff;
      border: 1px solid #dbe3ef;
      border-radius: 10px;
      box-shadow: 0 6px 20px rgba(15, 23, 42, 0.06);
      padding: 24px;
    }

    h1 {
      margin: 0 0 12px;
      font-size: 1.4rem;
    }

    p {
      margin: 0;
      line-height: 1.5;
      font-size: 1.02rem;
      word-break: break-word;
    }
  `],
  standalone: false
})
export class UnsubscribeConfirmationComponent implements OnInit {
  message = '';

  constructor(private activatedRoute: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.activatedRoute.queryParamMap.subscribe(params => {
      const email = this.escapeHtml(params.get('email') || '');
      const status = params.get('status') || 'success';

      if (status === 'success') {
        this.message = `User with email address <strong>${email}</strong> has been successfully unsubscribed.`;
      } else if (status === 'not_found') {
        this.message = `We could not find a user for email address <strong>${email}</strong>.`;
      } else {
        this.message = `We were unable to update email subscription for <strong>${email}</strong>. Please try again later.`;
      }
    });
  }

  private escapeHtml(value: string): string {
    return value
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }
}
