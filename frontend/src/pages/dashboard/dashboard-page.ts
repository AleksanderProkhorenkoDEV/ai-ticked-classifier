import { PageController } from "@open-cells/page-controller";
import { dashboardStyles } from "../../css/dashboard.css";
import { CSSResultGroup, html, LitElement } from "lit";
import { baseStyles } from "../../css/base-styles.css";
import { customElement } from "lit/decorators.js";

@customElement('dashboard-page')
export class DashboardPage extends LitElement {
    pageController = new PageController(this)

    static styles?: CSSResultGroup = [
        dashboardStyles,
        baseStyles
    ]

    handleNavigation = (e: Event, route: string) => {
        e.preventDefault()
        this.pageController.navigate(route);
    }

    render() {
        return html`
          <header>
             <h1 class="header__title"><span class="title__accent">AI</span> Ticket Clasifier</h1>
             <div>
                <img src="/icons/bell.svg"  alt="campana de notificaciones" class="icon notification" />
                <img src="/icons/person.svg" alt="ajustes personales" class="icon settings"/>
              </div>
          </header>
          <main>
            <aside>
                <navigation-menu current-path="/dashboard" orientation="vertical"></navigation-menu>

                <a 
                    href="/" 
                    class="sidebar__link"
                    @click="${(e: Event) => this.handleNavigation(e, "Home")}"
                >
                    Salir 
                    <img src="/icons/arrow-forward.svg" alt="flecha" />
                </a>
            </aside>
            <section class="content">
                <div class="content__header">
                    <h2 class="content__title">Tickets</h2>
                    <create-ticket-button></create-ticket-button>
                </div>
                <wrapper-card></wrapper-card>
                <!-- TABLA CON LA LISTA -->
                 
            </section>
          </main>
        `;
    }
}