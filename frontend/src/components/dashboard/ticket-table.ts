import { getTickets, setTicketArchived, TicketFilters, TicketRow } from "../../lib/action/tickets";
import { css, CSSResultGroup, html, LitElement } from "lit";
import { customElement, state } from "lit/decorators.js";
import { baseStyles } from "../../css/base-styles.css";
import { tableStyle } from "../../css/table/table-dashboard.css";

const PAGE_SIZE = 10;

const URGENCY_LABEL: Record<string, string> = { ALTA: "Alta", MEDIA: "Media", BAJA: "Baja" };
const CATEGORY_LABEL: Record<string, string> = { BUG: "Bug", INCIDENCIA: "Incidencia", CONSULTA: "Consulta" };
const FEELING_LABEL: Record<string, string> = { NEGATIVO: "Negativo", NEUTRO: "Neutro", POSITIVO: "Positivo" };
const STATUS_LABEL: Record<string, string> = {
    ABIERTO: "Abierto",
    EN_PROGRESO: "En progreso",
    CERRADO: "Cerrado",
    ARCHIVADO: "Archivado",
};


@customElement("ticket-table")
export class TicketTable extends LitElement {

    @state()
    private _tickets: TicketRow[] = [];

    @state()
    private _page = 0;

    @state()
    private _totalPages = 0;

    @state()
    private _loading = false;

    @state()
    private _error = false;

    @state()
    private _filters: TicketFilters = { search: "", category: "", urgency: "" };

    @state()
    private _updatingId?: number;

    private _debounceTimer?: number;
    private _requestId = 0;

    static styles?: CSSResultGroup = [
        css`
            :host {
                --line: rgb(from var(--color-text) r g b / 0.15);
            }
        `,
        baseStyles,
        tableStyle
    ];

    connectedCallback() {
        super.connectedCallback();
        this._load();
        window.addEventListener("ticket-created", this._load);
    }

    disconnectedCallback() {
        window.removeEventListener("ticket-created", this._load);
        clearTimeout(this._debounceTimer);
        super.disconnectedCallback();
    }

    // Se descartan las respuestas que llegan tarde (si el usuario sigue escribiendo
    // o cambia de filtro, solo cuenta la última petición).
    private _load = async () => {
        const id = ++this._requestId;
        this._loading = true;

        try {
            const response = await getTickets(this._page, PAGE_SIZE, this._filters);
            if (id !== this._requestId) return;

            this._tickets = response.content;
            this._totalPages = response.totalPages;
            this._error = false;
        } catch (error) {
            if (id !== this._requestId) return;
            console.error("No se pudieron cargar los tickets", error);
            this._error = true;
        } finally {
            if (id === this._requestId) this._loading = false;
        }
    };

    private _onSearch = (event: Event) => {
        const value = (event.target as HTMLInputElement).value;

        clearTimeout(this._debounceTimer);
        this._debounceTimer = window.setTimeout(() => {
            this._filters = { ...this._filters, search: value };
            this._page = 0;
            this._load();
        }, 300);
    };

    private _onFilter = (event: Event) => {
        const select = event.target as HTMLSelectElement;
        this._filters = { ...this._filters, [select.name]: select.value };
        this._page = 0;
        this._load();
    };

    private _goTo(page: number) {
        if (page < 0 || page >= this._totalPages) return;
        this._page = page;
        this._load();
    }

    // El backend responde con un GlobalResponseDTO sin el ticket, asi que solo se
    // actualiza la fila afectada en local: evita el refetch de la pagina y conserva
    // la paginacion y los filtros activos.
    private _onToggleArchived = async (ticket: TicketRow) => {
        const archived = ticket.status !== "ARCHIVADO";
        this._updatingId = ticket.id;

        try {
            await setTicketArchived(ticket.id, archived);

            this._tickets = this._tickets.map((row) =>
                row.id === ticket.id
                    ? { ...row, status: archived ? "ARCHIVADO" : "ABIERTO" }
                    : row
            );

            this._notifyTicketChange(
                archived ? "Ticket archivado" : "Ticket desarchivado"
            );
        } catch (error) {
            console.error("No se pudo actualizar el estado del ticket", error);
            this._notifyTicketChange(
                "No se pudo actualizar el ticket, inténtalo de nuevo",
                "error"
            );
        } finally {
            this._updatingId = undefined;
        }
    };

    // Las tarjetas de estadisticas escuchan este evento en window para actualizarse
    private _notifyTicketChange(message: string, type: "success" | "error" = "success") {
        this.dispatchEvent(new CustomEvent("ticket-archived", {
            detail: { message, type },
            bubbles: true,
            composed: true,
        }));
    }

    private _renderRow(ticket: TicketRow) {
        const isArchived = ticket.status === "ARCHIVADO";
        const isUpdating = this._updatingId === ticket.id;

        return html`
            <tr>
                <td class="cell-id">#&nbsp;${ticket.id}</td>
                <td>${ticket.title}</td>
                <td class="cell-desc col-desc" title=${ticket.description}>${ticket.description}</td>
                <td class="urgency urgency--${ticket.urgency.toLowerCase()}">${URGENCY_LABEL[ticket.urgency] ?? ticket.urgency}</td>
                <td class="category category--${ticket.category.toLowerCase()}">${CATEGORY_LABEL[ticket.category] ?? ticket.category}</td>
                <td class="feeling feeling--${ticket.feeling.toLowerCase()}">${FEELING_LABEL[ticket.feeling] ?? ticket.feeling}</td>
                <td class="status status--${ticket.status.toLowerCase()}">${STATUS_LABEL[ticket.status] ?? ticket.status}</td>
                <td class="col-actions">
                    <button
                        type="button"
                        class="archive-btn"
                        ?disabled=${isUpdating}
                        aria-label=${isArchived
                    ? `Desarchivar el ticket ${ticket.id}`
                    : `Archivar el ticket ${ticket.id}`}
                        @click=${() => this._onToggleArchived(ticket)}
                    >
                        <img src="/icons/archive.svg" alt="" />
                        ${isArchived ? "Desarchivar" : "Archivar"}
                    </button>
                </td>
            </tr>
        `;
    }

    render() {
        const totalPages = Math.max(this._totalPages, 1);

        return html`
            <section class="table-card">
                <div class="toolbar">
                    <label class="search">
                        <img src="/icons/search.svg" alt="" />
                        <input
                            type="search"
                            placeholder="Buscar tickets..."
                            aria-label="Buscar tickets por título"
                            @input=${this._onSearch}
                        />
                    </label>
 
                    <div class="filters">
                        <select name="category" aria-label="Filtrar por tipo" @change=${this._onFilter}>
                            <option value="">Todos los tipos</option>
                            <option value="BUG">Bug</option>
                            <option value="INCIDENCIA">Incidencia</option>
                            <option value="CONSULTA">Consulta</option>
                        </select>
 
                        <select name="urgency" aria-label="Filtrar por prioridad" @change=${this._onFilter}>
                            <option value="">Todas las prioridades</option>
                            <option value="ALTA">Alta</option>
                            <option value="MEDIA">Media</option>
                            <option value="BAJA">Baja</option>
                        </select>
                    </div>
                </div>
 
                <div class="table-wrap" aria-busy=${this._loading ? "true" : "false"}>
                    <table>
                        <caption class="sr-only">Lista de tickets</caption>
                        <thead >
                            <tr>
                                <th scope="col">Número</th>
                                <th scope="col">Título</th>
                                <th scope="col" class="col-desc">Descripción</th>
                                <th scope="col">Prioridad</th>
                                <th scope="col">Tipo</th>
                                <th scope="col">Sentimiento</th>
                                <th scope="col">Estado</th>
                                <th scope="col" class="col-actions">Acciones</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${this._tickets.map((ticket) => this._renderRow(ticket))}
                        </tbody>
                    </table>
 
                    ${this._error
                ? html`<p class="error" role="alert">No se pudieron cargar los tickets. Comprueba que el servidor está en marcha e inténtalo de nuevo.</p>`
                : !this._loading && this._tickets.length === 0
                    ? html`<p class="empty">No hay tickets que coincidan con la búsqueda.</p>`
                    : ""}
                </div>
 
                <nav class="pagination" aria-label="Paginación de tickets">
                    <button
                        type="button"
                        ?disabled=${this._page === 0}
                        @click=${() => this._goTo(this._page - 1)}
                    >
                        Anterior
                    </button>
                    <span aria-live="polite">Página ${this._page + 1} de ${totalPages}</span>
                    <button
                        type="button"
                        ?disabled=${this._page >= totalPages - 1}
                        @click=${() => this._goTo(this._page + 1)}
                    >
                        Siguiente
                    </button>
                </nav>
            </section>
        `;
    }
}