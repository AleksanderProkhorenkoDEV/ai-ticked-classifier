import { css } from "lit";

export const dashboardStyles = css` 

:host{
    display:block;
    overflow-x:hidden;

    --sidebar-width: 300px;
}

/* ---- HEADER OF DASHBOARD ----  */
header{
    height: 5rem;

    display:flex;
    flex-direction:row;
    justify-content:space-between;
    align-items:center;
    gap:.5rem;

    padding-inline: clamp(1rem, 5vw, 5rem);

    background-color: var(--color-white);
    border-bottom-right-radius: .3rem;
}

.header__group{
    display:flex;
    align-items:center;
    gap:.75rem;
    min-width:0;
}

/* ---- SIDEBAR TOGGLE ---- */

.sidebar-toggle{
    flex:none;

    display:flex;
    align-items:center;
    justify-content:center;

    width:2.5rem;
    height:2.5rem;

    padding:0;

    background-color:transparent;
    border:none;
    border-radius:100%;

    color: var(--color-text);

    cursor:pointer;
}

.sidebar-toggle:hover{
    background-color: rgb(from var(--color-primary) r g b / .12);
}

/* Los iconos son <img> desde /public/icons, no <svg> en linea */
.sidebar-toggle img{
    display:block;

    width:1.3rem;
    height:1.3rem;
}

.header__title{
    font-family: "Saira";
    font-weight: 400;
    font-size:clamp(1rem, 0.77rem + 1.228vw, 1.875rem);

    min-width:0;
    overflow:hidden;
    text-overflow:ellipsis;
    white-space:nowrap;
}

.title__accent{
    color: var(--color-primary);
}

.icon{
    padding:.4rem;
    border-radius:100%;
}

.icon.notification{
    background-color:rgb(from var(--color-yellow) r g b / .3);
}
.icon.settings{
    background-color:rgb(from var(--color-primary) r g b / .3)
}

/* ---- MAIN BODY OF DASHBOARD ---- */

main{
    height: calc(100% - 5rem);

    display:grid;
    grid-template-columns: 0 1fr;

    transition: grid-template-columns 200ms ease;
}

/* El sidebar viene plegado: el atributo lo pone la pagina segun el ancho */
:host([sidebar-open]) main{
    grid-template-columns: var(--sidebar-width) 1fr;
}


/* ---- SIDEBAR ---- */

aside{
    min-width:0;
    overflow:hidden;

    display:grid;
    grid-template-rows: 1fr 5rem;

    padding:.4rem;

    background-color: var(--color-white);
}

.sidebar__link{

    width:90%;

    display:flex;
    align-items: center;
    justify-content:center;
    gap:.2rem;

    padding: .4rem 1rem;
    margin:auto;

    font-weight:400;
    font-family: "Saira";
    font-size:20px;

    background-color:var(--color-red);
    border-radius:.3rem;
    color:var(--color-white);
}

/* ---- BACKDROP (solo visible en movil) ---- */

.sidebar-backdrop{
    display:none;

    position:fixed;
    inset:0;

    z-index:19;

    background-color: rgb(from var(--color-text) r g b / .45);
}

/* ---- CONTENT ---- */

.content{

    min-width:0;

    width:min(100% - 2rem, 1200px);
    height:auto;

    display:flex;
    flex-direction:column;
    align-items:center;
    gap:clamp(1rem, 2vw, 2rem);

    margin:auto;
    padding-block:1rem;
}

.content__header{
    min-height:4rem;
    width:100%;
    
    display:flex;
    flex-wrap:wrap;
    gap:.5rem;
    justify-content:space-between;
    align-items:center;

}

.content__title{
    font-family: "Saira";
    font-weight:400;
}

/* ---- RESPONSIVE ---- */

@media (width <= 1024px){
    /* El aside pasa a fixed y sale del flujo, asi que el grid se queda a una columna.
       Hay que repetir el selector del sidebar: si no, :host([sidebar-open]) main gana
       por especificidad y el contenido cae en la columna de 300px. */
    main,
    :host([sidebar-open]) main{
        grid-template-columns: 1fr;
    }

    aside{
        position:fixed;
        inset-block:0;
        left:0;

        width:min(80vw, var(--sidebar-width));

        z-index:20;

        overflow-y:auto;

        padding-top:1rem;

        transform:translateX(-100%);
        transition:transform 200ms ease;

        box-shadow:4px 0 16px rgb(from var(--color-text) r g b / .25);
    }

    :host([sidebar-open]) aside{
        transform:translateX(0);
    }

    :host([sidebar-open]) .sidebar-backdrop{
        display:block;
    }
}

@media (width <= 480px){
    header{
        padding-inline:1rem;
    }

    .content{
        width:calc(100% - 1.5rem);
    }

    .content__title{
        font-size:1.25rem;
    }
}

@media (prefers-reduced-motion: reduce){
    main,
    aside{
        transition:none;
    }
}
`