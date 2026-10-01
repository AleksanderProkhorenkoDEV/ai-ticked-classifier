import { PageController } from "@open-cells/page-controller";
import { css, CSSResultGroup, html, LitElement } from "lit";
import { customElement, property, state } from "lit/decorators.js";

@customElement('navigation-menu')
export class Navigation extends LitElement {
    pageController = new PageController(this);

    @state()
    private _currentPathName: string = "Home"

    @property({ type: String, reflect: true })
    orientation: 'horizontal' | 'vertical' = 'horizontal';

    static styles?: CSSResultGroup = [
        css`
            :host{
                display: flex;
                align-items:center;
                gap: 2rem;

                width: fit-content;
                height:100%;

                margin-left:auto;
            }

            :host([orientation="vertical"]) {
                flex-direction: column;
                align-items: flex-start;

                width: fit-content;
                
                margin-left: 0;
            }

            :host([orientation="horizontal"]) {
                flex-direction: row;
            }

            a{
                text-decoration: none;
                color: var(--color-text);
                font-size: clamp(1rem, 0.896rem + 0.556vw, 1.563rem);
            }

            .active {
                color: var(--color-primary);
            }

        `,

    ]

    private _navRoutes = [
        { name: "Home", path: "/", label: "Home" },
        { name: "Dashboard", path: "/dashboard", label: "Dashboard" }
    ]

    handleChengePath = (e: Event, routeName: string) => {
        e.preventDefault()
        this._currentPathName = routeName;
        this.pageController.navigate(routeName);
    }

    render() {
        return html`
            ${this._navRoutes.map((item) => html`
                  <a
                    href=${item.path}
                    class=${this._currentPathName === item.name ? 'active' : ''}
                    @click="${(e: Event) => this.handleChengePath(e, item.name)}"
                  >
                    ${item.label}
                  </a>
                `)
            }
        `
    }
}