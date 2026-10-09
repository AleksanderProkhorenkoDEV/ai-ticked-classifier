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
                width:fit-content;
            }
            
            .content__cards{
                display:flex;
                gap:3rem;
                align-items:center;
                justify-content:center;
            }
        `
    ]

    async connectedCallback() {
        super.connectedCallback();
        try {
            this._stats = await getTicketStats();
        } catch (error) {
            console.error('No se pudieron cargar las estadísticas', error);
        }
    }

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