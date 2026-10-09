import { css } from "lit";

export const dashboardStyles = css` 

/* ---- HEADER OF DASHBOARD ----  */
header{
    height: 5rem;

    display:flex;
    flex-direction:row;
    justify-content:space-between;
    align-items:center;

    padding-inline: 5rem;

    background-color: var(--color-white);
    border-bottom-right-radius: .3rem;
}

.header__title{
    font-family: "Saira";
    font-weight: 400;
    font-size:clamp(1rem, 0.77rem + 1.228vw, 1.875rem);
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
    grid-template-columns: 300px 1fr;
}


/* ---- SIDEBAR ---- */

aside{
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

/* ---- CONTENT ---- */

.content{
    border:1px solid red;

    width:85%;
    height:90%;

    display:flex;
    flex-direction:column;
    align-items:center;
    gap:2rem;

    margin:auto;
}

.content__header{
    border:1px solid green;

    height:4rem;
    width:100%;
    
    display:flex;
    justify-content:space-between;
    align-items:center;

}

.content__title{
    font-family: "Saira";
    font-weight:400;
}
`