import { css } from 'lit';

export const tableStyle = css`
  :host {
    display: block;

    height: 770px;

    font-family: 'Saira', sans-serif;
  }

  .table-card {
    display: flex;
    flex-direction: column;

    height: 100%;

    background-color: var(--color-white);

    border-radius: 0.3rem;
    overflow: hidden;
  }

  /* ---- SEARCH + FILTERS TOOLBAR ---- */
  .toolbar {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    justify-content: space-between;
    gap: 1rem;

    padding: 1rem 1.5rem;

    border-bottom: 1px solid var(--line);
  }

  .search {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    flex: 1 1 14rem;
  }

  .search img {
    width: 1.1rem;
    height: 1.1rem;
  }

  .search input {
    flex: 1;

    border: none;
    outline: none;

    background: transparent;
    color: var(--color-text);

    font: inherit;
  }

  .filters {
    display: flex;
    flex-wrap: wrap;
    gap: 1.5rem;
  }

  select {
    border: none;
    background: transparent;
    font: inherit;
    color: var(--color-text);
    cursor: pointer;
    padding: 0.25rem 0;
  }

  input:focus-visible,
  select:focus-visible,
  button:focus-visible {
    outline: 2px solid var(--color-primary);
    outline-offset: 2px;
    border-radius: 0.2rem;
  }

  /* ---- TABLE ---- */
  .table-wrap {
    flex: 1 1 auto;
    min-height: 0;
    overflow: auto;
    scrollbar-width: thin;
  }

  .table-wrap:focus-visible {
    outline: 2px solid var(--color-primary);
    outline-offset: -2px;
  }

  table {
    width: 100%;

    border-collapse: collapse;
  }

  th {
    position: sticky;

    top: 0;
    z-index: 1;

    padding: 1rem;

    background-color: var(--color-white);
    box-shadow: inset 0 -1px 0 var(--line);

    text-align: left;
    font-size: 0.85rem;
    font-weight: 600;

    white-space: nowrap;

    color: rgb(from var(--color-text) r g b / 0.7);
  }

  th:first-child {
    padding-left: 1.5rem; /* alineado con .cell-id */
  }

  td {
    padding: 1.4rem 1rem;

    border-bottom: 1px solid var(--line);
    white-space: nowrap;

    vertical-align: middle;
  }

  tbody tr:last-child td {
    border-bottom: none;
  }

  .cell-id {
    padding-left: 1.5rem;

    color: var(--color-text);
  }

  .cell-desc {
    max-width: 18rem;

    overflow: hidden;

    text-overflow: ellipsis;
  }

  .feeling--neutro {
    color: #454b56;
  }

  .feeling--negativo {
    color: #9e293b;
  }

  .feeling--positivo {
    color: #28695b;
  }

  .category--bug {
    color: #1d5fa8;
  }

  .category--consulta {
    color: #795000;
  }

  .category--incidencia {
    color: #28695b;
  }

  .urgency--baja {
    color: #28695b;
  }

  .urgency--media {
    color: #795000;
  }

  .urgency--alta {
    color: #9e293b;
  }

  .status--abierto,
  .status--en_progreso {
    color: var(--color-primary);
  }
  .status--cerrado {
    color: var(--color-green);
  }
  .status--archivado {
    color: var(--color-text);
    opacity: 0.6;
  }

  .empty,
  .error {
    padding: 2rem 1.5rem;
    text-align: center;
  }

  .error {
    color: var(--color-red);
  }

  /* ---- PAGINATION ---- */
  .pagination {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 1rem;

    padding: 1rem 1.5rem;

    border-top: 1px solid var(--line);
  }

  .pagination button {
    font: inherit;
    color: var(--color-primary);
    background: transparent;
    border: 1px solid var(--color-primary);
    border-radius: 0.4rem;

    padding: 0.4rem 0.9rem;

    cursor: pointer;
  }

  .pagination button:disabled {
    color: var(--color-text);
    border-color: var(--line);

    opacity: 0.5;

    cursor: not-allowed;
  }

  .toolbar,
  .pagination {
    flex-shrink: 0;
  }

  /* Screen readers */
  .sr-only {
    position: absolute;

    width: 1px;
    height: 1px;

    padding: 0;
    margin: -1px;

    overflow: hidden;

    clip: rect(0, 0, 0, 0);
    white-space: nowrap;
    border: 0;
  }

  @media (max-width: 768px) {
    .col-desc {
      display: none;
    }
    td {
      padding: 1.2rem 0.75rem;
    }
    .pagination {
      justify-content: space-between;
    }
  }
`;
