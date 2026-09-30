import { PageController } from '@open-cells/page-controller';
import { customElement } from 'lit/decorators.js';
import { html, LitElement } from 'lit';

// @ts-ignore
@customElement('home-page')
export class HomePage extends LitElement {
  pageController = new PageController(this);

  protected createRenderRoot(): HTMLElement | DocumentFragment {
    // @ts-ignore
    return this;
  }

  render() {
    return html`
      <h1>Test</h1>
    `;
  }
}
