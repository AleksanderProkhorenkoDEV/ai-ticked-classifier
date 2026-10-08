import { css, CSSResultGroup, html, LitElement } from "lit";
import { customElement, property } from "lit/decorators.js";
import { baseStyles } from "../../css/base-styles.css";

@customElement("dashboard-card")
export class DashboardCard extends LitElement {

    @property({ type: String, reflect: true })
    public number: String = "0";

    @property({ type: String })
    public text: String = "Tickets";

    @property({ type: String, reflect: true })
    stats: "total" | "resolved" | "open" | "high" = "total";

    static styles?: CSSResultGroup = [
        css` 
            .card{
                background-color: var(--color-white);

                aspect-ratio: 1/1;
                width:150px;

                border-radius:.3rem;

                display:flex;
                flex-direction:column;
                align-items:center;
                justify-content:center;
                gap:.4rem;

                font-family: "Saira";
            }

            .card__number{
                font-size: 35px;
                font-weight: 800;
            }

            :host([stats="open"]) .card__number {
                color: var(--color-primary);
            }

            :host([stats="high"]) .card__number {
                color: var(--color-red);
            }

            :host([stats="resolved"]) .card__number {
                color: var(--color-green);
            }

            .card__text{
                text-transform:uppercase;
            }
        `,
        baseStyles
    ]

    render() {
        return html`
            <article class="card">
                <p class="card__number">${this.number}</p>
                <p class="card__text">${this.text}</p>
            </article>
        `
    }
}