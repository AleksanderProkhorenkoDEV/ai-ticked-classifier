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

  render() {
    return html`
      <main role="main" tabindex="-1" @ticket-created=${(e: CustomEvent) => this._toast.show(e.detail.message, e.detail.type ?? 'success')}>
        <slot></slot>
        <toast-notification></toast-notification>
      </main>
    `;
  }
}
