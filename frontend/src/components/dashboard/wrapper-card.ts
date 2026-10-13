import { getTicketStats, TicketStatsDTO } from "../../lib/action/stats";
import { customElement, state } from "lit/decorators.js";
import { css, CSSResultGroup, html, LitElement } from "lit";

@customElement('wrapper-card')
export class WrapperCard extends LitElement {

    @state()
    private _stats: TicketStatsDTO = { total: 0, open: 0, high: 0, resolved: 0 };

    static styles?: CSSResultGroup = [
        css` 
            :host{
                flex:none;

                width:100%;
            }
            
            .content__cards{
                display:grid;
                grid-template-columns:repeat(auto-fit, minmax(8rem, 9.4rem));
                gap:clamp(1rem, 3vw, 3rem);
                align-items:center;
                justify-content:center;
            }

            @media (height <= 50rem){
                .content__cards{
                    grid-template-columns:repeat(auto-fit, minmax(6rem, 7rem));
                    gap:clamp(.75rem, 3vw, 1.5rem);
                }
            }
        `
    ]

    connectedCallback() {
        super.connectedCallback();
        this._loadStats();
        window.addEventListener("ticket-created", this._loadStats);
        window.addEventListener("ticket-archived", this._loadStats);
    }

    disconnectedCallback() {
        window.removeEventListener("ticket-created", this._loadStats);
        window.removeEventListener("ticket-archived", this._loadStats);
        super.disconnectedCallback();
    }

    private _loadStats = async () => {
        try {
            this._stats = await getTicketStats();
        } catch (error) {
            console.error('No se pudieron cargar las estadísticas', error);
        }
    };

    render() {
        return html` 
            <div class="content__cards">
                <dashboard-card .number=${this._stats.total} .text=${"Total"}></dashboard-card>
                <dashboard-card .number=${this._stats.open} .text=${"Open"} stats="open"></dashboard-card>
                <dashboard-card .number=${this._stats.high} .text=${"High"} stats="high"></dashboard-card>
                <dashboard-card .number=${this._stats.resolved} .text=${"Resolved"} stats="resolved"></dashboard-card>
            </div>
        `
    }
}