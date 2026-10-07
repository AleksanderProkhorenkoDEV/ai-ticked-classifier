import { css } from "lit";

export const createTicketStyle = css` 


/* ---- CONTAINERS ---- */
main {
    box-sizing:border-box;

    display:grid;
    grid-template-columns: 1fr 1fr;
    grid-template-rows: 200px 1fr;

    
    width: 100%;
    height: 100%;
}

aside {
   grid-row: 2/3;
   grid-column: 1/2;

   margin:auto;
}

article {
    grid-row: 2/3;
    grid-column: 2/3;

    margin:auto;

    display:flex;
    flex-direction:column;
    gap: 2rem;
}

/* ---- NAVIGATION ---- */

.link{
    display:flex;
    align-items: center;
    gap:0.2rem;

    width:fit-content;

    font-weight:bold;

    margin-left:5rem;
}

.icon {
    display: inline-block;
    width: 16px;
    height: 16px;
    background-color: currentColor;
    -webkit-mask: url('/icons/arrow-cancel.svg') no-repeat center / contain;
    mask: url('/icons/arrow-cancel.svg') no-repeat center / contain;
}

.link:hover{
    color:var(--color-primary);
    
    transition:ease-in-out;
    transition-duration: 200ms;
}


/* ---- EXPLANATION ---- */


.description{
    background-color: var(--color-white);
    box-shadow: 6px 9px 20px rgba(13, 19, 33, 0.5);

    border-radius:.3rem;

    width: fit-content;
    height: fit-content;

    padding:2rem;

    display:flex;
    flex-direction:column;
    gap:1rem;
}

.description__title{
    font-size: clamp(1rem, 0.868rem + 0.702vw, 1.5rem);
}

.description__text{
    text-wrap: balance;
    color: rgb(from var(--color-text) r g b / 0.7);
}

.description__list{
    margin-left: 0.5rem;

    display:flex;
    flex-direction:column;
    gap:.4rem;
}

.description__item {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    
    padding: 0.625rem 0.875rem;
    background: rgb(from var(--color-primary) r g b / .2);
    
    border-radius: 10px;
    
    font-weight: 500;
    
    color: var(--color-primary);
  }

.description__item::before {
    content: '';
    flex: none;
    width: 1.25rem;
    height: 1.25rem;
    border-radius: 50%;
    background: var(--color-green)
      url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 20 20' fill='none' stroke='white' stroke-width='2.5' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M5 10.5l3.2 3.2L15 7'/%3E%3C/svg%3E")
      center / 70% no-repeat;
}
`