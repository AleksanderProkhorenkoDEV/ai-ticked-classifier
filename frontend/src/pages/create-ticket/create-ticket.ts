import { createTicketStyle } from "../../css/create-ticket.css";
import { PageController } from "@open-cells/page-controller";
import { customElement, property } from "lit/decorators.js";
import { CSSResultGroup, html, LitElement } from "lit";
import { baseStyles } from "../../css/base-styles.css";

@customElement('create-ticket-page')
export class CreateTicketPage extends LitElement {
    pageController = new PageController(this);

    static styles?: CSSResultGroup = [createTicketStyle, baseStyles]

    @property({ type: String, reflect: true })
    required: 'required' | 'no-required' = 'no-required';

    handleClick = (e: Event) => {
        e.preventDefault();
        this.pageController.navigate("Home")
    }

    render() {
        return html`
            
            <main>
                <a class="link" href="/" @click="${(e: Event) => this.handleClick(e)}"><span class="icon" aria-hidden="true"></span>Cancelar</a>
                <aside>
                    <div class="description">
                        <h3 class="description__title">Clasificación Automática</h3>
                        <p class="description__text">Al crear tu incidencia, la IA la <strong>analiza</strong> y <strong>asigna</strong>:</p>

                        <ul class="description__list">
                            <li class="description__item">Categoría</li>
                            <li class="description__item">Nivel de urgencia</li>
                            <li class="description__item">Sentimiento</li>
                        </ul>

                        <p class="description__text">Sin que tengas que rellenar nada más.</p>
                    </div>
                </aside>
                <article>
                    <create-ticket-form></create-ticket-form>
                </article>
            </main>
        `
    }
}