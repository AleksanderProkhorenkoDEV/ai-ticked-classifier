import { css } from "lit";

export const createTicketFormStyles = css`

.form{
    border-radius:.3rem;

    display: flex;
    flex-direction:column;
    gap:1rem;

    padding: 1rem 2rem;

    background-color:var(--color-white);
    box-shadow: 6px 9px 20px rgba(13, 19, 33, 0.5);   
}

.form__label{
    display: flex;
    flex-direction:column;
    gap:.5rem;

    position:relative;

    color: rgb(from var(--color-text) r g b / 0.7);
}

.form__label[required="required"]::before{
    content: "*";

    position:absolute;
    left:-8px;

    color:var(--color-red);
}

.form__input {
    width: 100%;

    padding: .5rem .7rem;
    
    font: inherit;
    font-size: 0.95rem;
    
    color: var(--color-text);
    background: var(--color-white);
    border: 1px solid var(--color-text);
    border-radius: .3rem;
    
    box-shadow: 0 1px 2px rgba(16, 24, 40, 0.04);
    box-sizing: border-box;

    transition:
      border-color 0.15s ease,
      box-shadow 0.15s ease,
      background-color 0.15s ease;
}

.form__input:focus {
    outline: none;
    border-color: var(--color-primary);
}

.form__button{
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
    
    width: 100%;
    
    padding: 0.75rem 1.25rem;
    
    font: inherit;
    font-size: 0.95rem;
    font-weight: 600;
    letter-spacing: 0.01em;
    
    color: var(--color-white);
    background: var(--color-primary);
    
    box-sizing: border-box;
    border: 1px solid transparent;
    border-radius: .3rem;
    box-shadow:
      0 1px 2px rgba(16, 24, 40, 0.08),
      inset 0 1px 0 rgba(255, 255, 255, 0.15);
    
    cursor: pointer;

    transition:
      background-color 0.15s ease,
      box-shadow 0.15s ease,
      transform 0.1s ease;
}

.form__button:focus-visible {
    outline: none;
    box-shadow: 0 0 0 4px var(--accent-ring, rgba(79, 70, 229, 0.25));
}

.form__error{
    color: var(--color-red);
}
`