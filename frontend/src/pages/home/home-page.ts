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
      <main>
        
      
        <div class="hero">
          <h1 class="hero__title"><span class="hero__title_emphasise">AI</span> Ticket Clasifier</h1>
          <p class="hero__text">Organize and classify your tickets effortlessly, <br/> powered by LLMs</p>
          <a class="hero__action">Create Ticket <img src="/icons/arrow-forward.svg" alt="arrow forward" /></a>
        </div>


        <article class="incoming">
          <div class="incoming__title">
            <div class="incoming__icon">
              <img src="/icons/mail.svg" alt="mail icon" />
            </div>
            <p>Incoming Ticket</p>
            <p>2m ago</p>
          </div>
          <p>
            "User can resseting their password and are getting an error message"
          </p>
        </article>
        <article>
          <img src="/icons/ai.svg" alt="ai brain" />
          <p>AI is clasifing...</p>
        </article>
        <article>
          <div>
            <div>
              <img src="/icons/priority.svg" alt="Exclamation icon" />
            </div>
            <div>
              <p>priority</p>
              <p>HIGH</p>
            </div>
          </div>
          <div>
            <div>
              <img src="/icons/bug.svg" alt="Exclamation icon" />
            </div>
            <div>
              <p>category</p>
              <p>BUG</p>
            </div>
          </div>
          <div>
            <div>
              <img src="/icons/feedback.svg" alt="Exclamation icon" />
            </div>
            <div>
              <p>feeling</p>
              <p>NEUTRAL</p>
            </div>
          </div>
        </article>
        <article>
          <div>
            <div>
              <span>Bug</span>
              <p>Error uploading images</p>
            </div>
            <p>High</p>
          </div>
          <div>
            <div>
              <span>Consultant</span>
              <p>Could the hero's image be updated</p>
            </div>
            <p>Low</p>
          </div>
          <div>
            <div>
              <span>Incident</span>
              <p>The home takes a few seconds to load</p>
            </div>
            <p>Medium</p>
          </div>
        </article>

        <div id="big-circle"></div>
        <div id="small-circle"></div>
        <div id="small-circle"></div>
      </main>
    `;
  }
}
