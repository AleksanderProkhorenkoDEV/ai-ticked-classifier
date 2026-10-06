import { css } from "lit";

export const baseStyles = css` 

*,
*::before,
*::after {
  box-sizing: border-box;
}

a{
  text-decoration: none;
  color: var(--color-text)
}

h1,h2,h3, p, main{
  margin:0;
  padding: 0;
}

ul,ol {
    list-style:none;
    margin:0;
    padding:0
}

strong{
  color:var(--color-primary);
}

`