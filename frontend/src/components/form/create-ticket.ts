import { createTicketFormStyles } from "../../css/form/create-ticket-form.css";
import { customElement, state } from "lit/decorators.js";
import { CSSResultGroup, html, LitElement } from "lit";
import { baseStyles } from "../../css/base-styles.css";
import { createTicket } from "../../lib/action/create";

export interface CreateTicketDTO {
    title: String;
    description: String;
}

interface FormErrors {
    title: { message: String };
    description: { message: String };
}

// Mismos límites que impone el backend (@Size), para no dejar que el usuario descubra el
// rechazo con un 400 después de esperar la clasificación.
const TITLE_MAX_LENGTH = 120;
const DESCRIPTION_MAX_LENGTH = 255;


@customElement("create-ticket-form")
export class CreateTicketForm extends LitElement {

    @state()
    private _values: CreateTicketDTO = { title: "", description: "" };

    @state()
    private _errors?: FormErrors;

    @state()
    private _isSubmitting = false;


    private _validateInputs = (values: CreateTicketDTO): boolean => {

        const result: FormErrors = {
            title: { message: "" },
            description: { message: "" }
        }

        if (values.title.trim().length < 3) {
            result.title.message = "El título debe tener al menos 3 caracteres";
        } else if (values.title.trim().length > TITLE_MAX_LENGTH) {
            result.title.message = `El título no puede superar los ${TITLE_MAX_LENGTH} caracteres`;
        }

        if (values.description.trim().length < 10) {
            result.description.message = "Describe el problema con un poco más de detalle";
        } else if (values.description.trim().length > DESCRIPTION_MAX_LENGTH) {
            result.description.message = `La descripción no puede superar los ${DESCRIPTION_MAX_LENGTH} caracteres`;
        }

        const isValid = Object.values(result).every(field => field.message === "");
        this._errors = result;
        return isValid;
    }

    private _handleSubtmit = async (e: Event) => {
        e.preventDefault();

        const isValid = this._validateInputs(this._values);
        if (!isValid) return;

        this._isSubmitting = true;

        try {
            await createTicket(this._values);

            this.dispatchEvent(new CustomEvent('ticket-created', {
                detail: { message: 'Ticket creado y clasificado con éxito '},
                bubbles: true,
                composed: true,
            }));

            this._values = { title: '', description: '' };
            this._errors = undefined;
        } catch (error) {
            // El backend explica el rechazo (contenido inapropiado, moderación caída, límite de
            // peticiones), así que se enseña su mensaje en lugar de uno genérico.
            const message = error instanceof Error
                ? error.message
                : 'No se pudo crear el ticket, inténtalo de nuevo';

            this.dispatchEvent(new CustomEvent('ticket-created', {
                detail: { message, type: 'error' },
                bubbles: true,
                composed: true,
            }));
        } finally {
            this._isSubmitting = false;
        }
    }

    private _handleChangeInput = (e: Event) => {
        const input = e.target as HTMLInputElement | HTMLTextAreaElement;
        const name = input.name as keyof CreateTicketDTO;
        this._values = { ...this._values, [name]: input.value };
    }

    static styles?: CSSResultGroup = [
        createTicketFormStyles,
        baseStyles
    ]

    render() {
        return html`
            <form class="form" @submit=${this._handleSubtmit}>
                <h1 class="form__title">Crear Incidencia</h1>
                <p class="form__subtitle">Explica detalladamente el problema, para que el modelo de IA pueda analizar y clasificar su problema</p>
                <label class="form__label" required="required">
                    Titulo
                    <input 
                        name="title"
                        class="form__input" 
                        type="text" 
                        required
                        maxlength=${TITLE_MAX_LENGTH}
                        .value=${this._values.title}
                        @input=${this._handleChangeInput}
                    />
                    ${this._errors?.title.message
                ? html`<span class="form__error">${this._errors.title.message}</span>`
                : ''
            }
                </label>

                <label class="form__label" required="required">
                    Descripción
                    <textarea  
                        name="description"
                        class="form__input" 
                        required
                        maxlength=${DESCRIPTION_MAX_LENGTH}
                        .value=${this._values.description}
                        @input=${this._handleChangeInput}
                    >
                    </textarea>
                    ${this._errors?.description.message
                ? html`<span class="form__error">${this._errors.description.message}</span>`
                : ''
            }
                </label>

                <button 
                    type="submit"
                    class="form__button"
                    ?disabled=${this._isSubmitting}
                >
                    ${this._isSubmitting
                ? 'Verificando y clasificando...'
                : html`<img src="/icons/add.svg" alt="" /> Crear ticket`}
                </button>
            </form>
        `
    }
}