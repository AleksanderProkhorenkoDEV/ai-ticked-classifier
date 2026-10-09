import { customElement, state } from "lit/decorators.js";
import { CSSResultGroup, html, LitElement } from "lit";
import { PageController } from "@open-cells/page-controller";
import { dashboardStyles } from "../../css/dashboard.css";
import { baseStyles } from "../../css/base-styles.css";
import { getTicketStats, TicketStatsDTO } from "../../lib/action/stats";

@customElement('dashboard-page')
export class DashboardPage extends LitElement {
    pageController = new PageController(this)


    @state()
    private _stats: TicketStatsDTO = { total: 0, open: 0, high: 0, resolved: 0 };

    static styles?: CSSResultGroup = [
        dashboardStyles,
        baseStyles
    ]

    handleNavigation = (e: Event, route: string) => {
        e.preventDefault()
        this.pageController.navigate(route);
    }

    async connectedCallback() {
        super.connectedCallback();
        try {
            this._stats = await getTicketStats();
        } catch (error) {
            console.error('No se pudieron cargar las estadísticas', error);
        }
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
                <div class="content__cards">
                    <dashboard-card .number=${this._stats.total} .text=${"Total"}></dashboard-card>
                    <dashboard-card .number=${this._stats.open} .text=${"Open"} stats="open"></dashboard-card>
                    <dashboard-card .number=${this._stats.high} .text=${"High"} stats="high"></dashboard-card>
                    <dashboard-card .number=${this._stats.resolved} .text=${"Resolved"} stats="resolved"></dashboard-card>
                </div>
                <!-- TABLA CON LA LISTA -->
            </section>
          </main>
        `;
    }
}