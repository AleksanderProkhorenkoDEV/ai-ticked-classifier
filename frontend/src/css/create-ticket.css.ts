import { css } from "lit";

export const createTicketStyle = css` 


/* ---- CONTAINERS ---- */

:host {
    display:block;
    height:100%;

    --gutter-inline: clamp(1.5rem, 5vw, 5rem);
    --gutter-block: clamp(1.5rem, 4vh, 3rem);
}

main {
    box-sizing:border-box;

    display:grid;
    grid-template-columns: 1fr 1fr;
    grid-template-rows: auto 1fr;

    gap: var(--gutter-block) 3rem;

    width: 100%;

    min-height: 100%;
}

aside {
   grid-row: 2/3;
   grid-column: 1/2;

   width:100%;

   padding-inline: var(--gutter-inline);

   display:flex;
   align-items:center;
   justify-content:center;
}

article {
    grid-row: 2/3;
    grid-column: 2/3;

    width:100%;

    padding-inline: var(--gutter-inline);

    display:flex;
    align-items:center;
    justify-content:center;
}

/* ---- NAVIGATION ---- */

.link{
    display:flex;
    align-items: center;
    gap:0.2rem;

    grid-column: 1 / -1;
    justify-self: start;

    width:fit-content;

    margin-block-start: var(--gutter-block);
    margin-inline-start: var(--gutter-inline);

    font-weight:bold;
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
    max-width: 100%;

    margin-inline: auto;

    padding: clamp(1.25rem, 3vw, 2rem);

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

/* ---- MEDIA QUERYS ---- */

@media (width <= 1024px) {
    main {
        column-gap: 2rem;
    }
}

@media (width <= 767px) {
    main {
        grid-template-columns: 1fr;
        grid-template-rows: auto;

        row-gap: 1.5rem;
    }

    aside,
    article {
        grid-row: auto;
        grid-column: 1;
    }
    
    article {
        padding-block-end: var(--gutter-block);
    }
}

@media (width <= 480px) {
    .description__list {
        margin-left: 0;
    }

    .description__item {
        padding-inline: 0.75rem;
    }
}
`