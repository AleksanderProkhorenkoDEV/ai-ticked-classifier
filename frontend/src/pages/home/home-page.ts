import { PageController } from '@open-cells/page-controller';
import { customElement } from 'lit/decorators.js';
import { html, LitElement } from 'lit';

@customElement('home-page')
export class HomePage extends LitElement {
  pageController = new PageController(this);

  protected createRenderRoot(): HTMLElement | DocumentFragment {
    return this;
  }


  render() {
    return html`
      <header>
          <nav>
              <navigation-menu current-path="/"></navigation-menu>
          </nav>
      </header>
      <main>
        
      
        <div class="hero">
          <h1 class="hero__title"><span class="hero__title_emphasise">AI</span> Ticket Clasifier</h1>
          <p class="hero__text">Organize and classify your tickets effortlessly, <br/> powered by LLMs</p>
          <create-ticket-button></create-ticket-button>
        </div>


        <div class="cards__container">
          <article class="incoming">
            <div class="incoming__title">
              <div class="incoming__icon">
                <img src="/icons/mail.svg" alt="mail icon" />
              </div>
              <p>Incoming Ticket</p>
              <p class="incoming__time">2m ago</p>
            </div>
            <p>
              "User can resseting their password and are getting an error message"
            </p>
          </article>

          <article class="loading">
            <img src="/icons/ai.svg" alt="ai brain" class="loading__image" />
            <p class="loading__text">AI is clasifing...</p>
          </article>

          <article class="categories">
            
            <div class="categories__item">
              <div class="icon__background" data-variant="blue">
                <img src="/icons/priority.svg" alt="Exclamation icon" class="icon" />
              </div>
              <div class="categories__text">
                <p class="category__type">priority</p>
                <p class="category__result">HIGH</p>
              </div>
            </div>

            <div class="categories__item">
              <div class="icon__background" data-variant="red">
                <img src="/icons/bug.svg" alt="Exclamation icon" class="icon" />
              </div>
              <div class="categories__text">
                <p class="category__type">category</p>
                <p class="category__result">BUG</p>
              </div>
            </div>

            <div class="categories__item">
              <div class="icon__background" data-variant="green">
                <img src="/icons/feedback.svg" alt="Exclamation icon" class="icon" />
              </div>
              <div class="categories__text">
                <p class="category__type">feeling</p>
                <p class="category__result">NEUTRAL</p>
              </div>
            </div>

          </article>
        </div>

        <article class="list">

          <div class="list__item">
            <div class="item__content">
              <span class="item__type">Bug</span>
              <p>Error uploading images</p>
            </div>
            <p class="item__priority" data-variant="red">High</p>
          </div>

          <div class="list__item">
            <div class="item__content">
              <span class="item__type">Consultant</span>
              <p>Could the hero's image be updated</p>
            </div>
            <p class="item__priority" data-variant="green">Low</p>
          </div>

          <div class="list__item">
            <div class="item__content">
              <span class="item__type">Incident</span>
              <p>The home takes a few seconds to load</p>
            </div>
            <p class="item__priority" data-variant="yellow">Medium</p>
          </div>

        </article>

        <div class="big-circle"></div>
        <div class="small-circle one"></div>
        <div class="small-circle two"></div>
      </main>
    `;
  }
}
