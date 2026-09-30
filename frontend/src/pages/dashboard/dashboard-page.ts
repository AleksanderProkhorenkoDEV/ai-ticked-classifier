import { customElement } from "lit/decorators.js";
import { html, LitElement } from "lit";

@customElement('dashboard-page')
export class DashboardPage extends LitElement {
   
    render() {
        return html`
          <h1>Dashboard</h1>
        `;
    }
}