import { customElement } from 'lit/decorators.js';
import { html, LitElement } from 'lit';

@customElement('home-page')
export class HomePage extends LitElement {


  protected createRenderRoot(): HTMLElement | DocumentFragment {
    return this;
  }

  render() {
    return html`
      <header>
          <nav>
              <navigation-menu></navigation-menu>
          </nav>
      </header>
    `;
  }
}
