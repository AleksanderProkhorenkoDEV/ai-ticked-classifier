import { PageController } from "@open-cells/page-controller";
import { dashboardStyles } from "../../css/dashboard.css";
import { CSSResultGroup, html, LitElement } from "lit";
import { baseStyles } from "../../css/base-styles.css";
import { customElement, property } from "lit/decorators.js";

const MOBILE_QUERY = "(width <= 1024px)";

@customElement('dashboard-page')
export class DashboardPage extends LitElement {
    pageController = new PageController(this)

    private _mobile = window.matchMedia(MOBILE_QUERY);

    // Reflejado al atributo del host, el CSS decide si el sidebar esta desplegado
    // (sin `attribute` explicito Lit reflejaria "sidebaropen" y el CSS no casaria)
    @property({ type: Boolean, reflect: true, attribute: 'sidebar-open' })
    sidebarOpen = false;

    static styles?: CSSResultGroup = [
        dashboardStyles,
        baseStyles
    ]

    connectedCallback() {
        super.connectedCallback();
        this.sidebarOpen = !this._mobile.matches;
        this._mobile.addEventListener("change", this._onMobileChange);
        window.addEventListener("keydown", this._onKeyDown);
    }

    disconnectedCallback() {
        this._mobile.removeEventListener("change", this._onMobileChange);
        window.removeEventListener("keydown", this._onKeyDown);
        super.disconnectedCallback();
    }

    // Al cruzar el breakpoint el sidebar vuelve a su estado por defecto
    private _onMobileChange = (event: MediaQueryListEvent) => {
        this.sidebarOpen = !event.matches;
    }

    private _onKeyDown = (event: KeyboardEvent) => {
        if (event.key === "Escape") {
            this.sidebarOpen = false;
        }
    }

    private _toggleSidebar = () => {
        this.sidebarOpen = !this.sidebarOpen;
    }

    // En escritorio el sidebar se mantiene, en movil se cierra al navegar
    private _closeSidebarOnMobile = () => {
        if (this._mobile.matches) {
            this.sidebarOpen = false;
        }
    }

    handleNavigation = (e: Event, route: string) => {
        e.preventDefault()
        this.pageController.navigate(route);
        this._closeSidebarOnMobile();
    }

    render() {
        return html`
          <div class="sidebar-backdrop" @click=${this._toggleSidebar}></div>
          <header>
             <div class="header__group">
                <button
                    type="button"
                    class="sidebar-toggle"
                    aria-controls="app-sidebar"
                    aria-expanded=${this.sidebarOpen ? "true" : "false"}
                    aria-label=${this.sidebarOpen ? "Ocultar el menú" : "Mostrar el menú"}
                    @click=${this._toggleSidebar}
                >
                    ${this._renderToggleIcon()}
                </button>
                <h1 class="header__title"><span class="title__accent">AI</span> Ticket Clasifier</h1>
             </div>
             <div>
                <img src="/icons/bell.svg"  alt="campana de notificaciones" class="icon notification" />
                <img src="/icons/person.svg" alt="ajustes personales" class="icon settings"/>
              </div>
          </header>
          <main>
            <aside id="app-sidebar" ?inert=${!this.sidebarOpen}>
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
                <ticket-table></ticket-table>
            </section>
          </main>
        `;
    }

    private _renderToggleIcon() {
        return this.sidebarOpen
            ? html`<img src="/icons/close.svg" alt="" />`
            : html`<img src="/icons/menu.svg" alt="" />`;
    }
}