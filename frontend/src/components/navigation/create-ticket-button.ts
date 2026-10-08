import { PageController } from "@open-cells/page-controller";
import { css, CSSResultGroup, html, LitElement } from "lit";
import { customElement } from "lit/decorators.js";
import { baseStyles } from "../../css/base-styles.css";

@customElement("create-ticket-button")
export class CreateTicketButton extends LitElement {
    pageController = new PageController(this)

    static styles?: CSSResultGroup = [
        css` 
            .button {

                display: flex;
                flex-direction: row;
                gap: .3rem;
                align-items: center;
                justify-content: center;

                width: fit-content;

                background-color: var(--color-primary);
                color: var(--color-white);

                padding: .4rem .7rem;

                border-radius: 2rem;

                font-family: "Montserrat";
                font-size: clamp(1rem, 0.967rem + 0.175vw, 1.125rem);
            }
        `,
        baseStyles
    ]

    handleNavigation = (e: Event, route: string) => {
        e.preventDefault()
        this.pageController.navigate(route);
    }

    render() {
        return html`
            <a class="button" href="/create-ticket" @click="${(e: Event) => this.handleNavigation(e, "/Create Ticket")}">Create ticket <img src="/icons/add.svg" alt="signo sumar" /></a>
        `
    }
}