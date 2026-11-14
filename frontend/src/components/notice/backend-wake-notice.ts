import { LitElement, html, css } from "lit";
import { customElement, state } from "lit/decorators.js";
import { baseStyles } from "../../css/base-styles.css";

const STORAGE_KEY = "backend-wake-notice:dismissed";

@customElement("backend-wake-notice")
export class BackendWakeNotice extends LitElement {
  @state()
  private _visible = false;

  static styles = [
    baseStyles,
    css`
      :host {
        display: block;
      }

      .notice {
        display: flex;
        align-items: flex-start;
        justify-content: space-between;
        gap: 1rem;
        padding: 0.75rem 1rem;
        background-color: var(--color-yellow, #f59e0b);
        color: var(--color-text, #1f2937);
        border-bottom: 1px solid rgb(from var(--color-text, #1f2937) r g b / 0.15);
      }

      .notice__content {
        display: flex;
        flex-direction: column;
        gap: 0.25rem;
      }

      .notice__title {
        font-weight: 600;
        margin: 0;
      }

      .notice__message {
        margin: 0;
        font-size: 0.9rem;
        line-height: 1.4;
      }

      .notice__close {
        flex-shrink: 0;
        background: transparent;
        border: 1px solid rgb(from var(--color-text, #1f2937) r g b / 0.3);
        border-radius: 0.375rem;
        color: inherit;
        cursor: pointer;
        padding: 0.25rem 0.5rem;
        font: inherit;
        line-height: 1.2;
      }

      .notice__close:hover,
      .notice__close:focus-visible {
        background: rgb(from var(--color-text, #1f2937) r g b / 0.08);
      }
    `,
  ];

  connectedCallback(): void {
    super.connectedCallback();
    try {
      this._visible = sessionStorage.getItem(STORAGE_KEY) !== "dismissed";
    } catch {
      this._visible = true;
    }
  }

  private _dismiss(): void {
    this._visible = false;
    try {
      sessionStorage.setItem(STORAGE_KEY, "dismissed");
    } catch {
      // Si el almacenamiento no está disponible, solo se oculta en esta vista.
    }
  }

  render() {
    if (!this._visible) {
      return html``;
    }

    return html`
      <div class="notice" role="status" aria-live="polite">
        <div class="notice__content">
          <p class="notice__title">Servidor en reposo</p>
          <p class="notice__message">
            El backend está alojado en Render y puede estar desactivado. La primera petición
            puede tardar entre 1 y 2 minutos mientras el servidor se inicia.
          </p>
        </div>
        <button
          type="button"
          class="notice__close"
          @click=${this._dismiss}
          aria-label="Cerrar aviso"
        >
          Cerrar
        </button>
      </div>
    `;
  }
}

declare global {
  interface HTMLElementTagNameMap {
    "backend-wake-notice": BackendWakeNotice;
  }
}
