import { customElement } from "lit/decorators.js";
import { html, LitElement } from "lit";

@customElement('create-ticket-page')
export class CreateTicketPage extends LitElement {

    protected createRenderRoot(): HTMLElement | DocumentFragment {
        return this;
    }

    render() {
        return html`
            <main>
                <aside>
                    <div>
                        <h3>Clasificación Automática</h3>
                        <p>Al crear tu incidencia, la IA la analiza y asigna:</p>

                        <ul>
                            <li>Categoría</li>
                            <li>Nivel de urgencia</li>
                            <li>Sentimiento</li>
                        </ul>

                        <p>Sin que tengas que rellenar nada más.</p>
                    </div>
                </aside>
                <article>
                    <h1>Crear Incidencia</h1>
                    <form>
                        <label>
                            Titulo
                            <input />
                        </label>

                        <label>
                            Descripción
                            <input />
                        </label>

                        <button>Crear ticket</button>
                    </form>
                </article>
            </main>
        `
    }
}