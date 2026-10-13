import { startApp } from '@open-cells/core';
import { LitElement, html } from 'lit';
import { customElement, query } from 'lit/decorators.js';
import { ElementController } from '@open-cells/element-controller';
import { routes } from '../router/routes.js';
import { styles } from './app-index.css.js';
import { ToastNotification } from './toast/toast.js';

startApp({
  routes,
  mainNode: 'app-content',
});

@customElement('app-index')
export class AppIndex extends LitElement {
  elementController = new ElementController(this);

  static styles = styles;

  @query('toast-notification')
  private _toast!: ToastNotification;

  private _showToast = (e: CustomEvent<{ message: string; type?: 'success' | 'error' }>) => {
    this._toast.show(e.detail.message, e.detail.type ?? 'success');
  }

  render() {
    return html`
      <main role="main" tabindex="-1"
        @ticket-created=${this._showToast}
        @ticket-archived=${this._showToast}>
        <slot></slot>
        <toast-notification></toast-notification>
      </main>
    `;
  }
}
