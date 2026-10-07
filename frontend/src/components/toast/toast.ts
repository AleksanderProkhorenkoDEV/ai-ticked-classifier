import { baseStyles } from "../../css/base-styles.css";
import { css, CSSResultGroup, html, LitElement } from "lit";
import { customElement, property, state } from "lit/decorators.js";

@customElement("toast-notification")
export class ToastNotification extends LitElement {

    @state()
    private _visible = false;

    @state()
    private _message = '';

    @property({ type: String })
    type: 'success' | 'error' = 'success';


    static styles?: CSSResultGroup = [
        baseStyles,
        css` 
            :host {
                position: fixed;
                bottom: 2rem;
                right: 2rem;
                z-index: 1000;
            }

            .toast {
                padding: 1rem 1.5rem;
                border-radius: 0.5rem;

                background-color: var(--color-green);
                color: var(--color-white);
                box-shadow: 0 4px 12px rgb(from var(--color-text) r g b / 0.3);
                
                opacity: 0;

                transform: translateY(1rem);

                transition: opacity 0.3s ease, transform 0.3s ease;
            }

            .toast--visible {
                opacity: 1;
                transform: translateY(0);
            }

            .toast--error {
                background-color: var(--color-red);
            }
        `
    ]

    show(message: string, type: 'success' | 'error' = 'success') {
        this._message = message;
        this.type = type;
        this._visible = true;

        setTimeout(() => {
            this._visible = false;
        }, 3000);
    }

    render() {
        return html` 
            <div class="toast ${this._visible ? 'toast--visible' : ''} ${this.type === 'error' ? 'toast--error' : ''}">
                ${this._message}
            </div>
        `
    }
}