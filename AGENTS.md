# AGENTS.md - AI TICKET CLASIFIER

Application for classifying incidents using an LLM to speed up tasks,
they are classified into category, sentiment and priority

## Layout

- Three independent projects, no root workspace/lockfile:
  - `backend/ticked-classifier`: Spring Boot 4.1.1, Java 21, Spring AI 2.0.1 (BOM) with the starter Mistral AI,
  - `frontend`: Lit + Vite + Typescript, routing with Open Cells. Node >= 22, manage with pnpm.
  - `infraestructure` (docker-compose: MySQL only — no backend/frontend containers).
- API base path is `/tickeds` (`TicketController`), app runs on port 8080.
- AI Model: ministral-3b-2512, JSON-schema response parsed into TicketClasification
- DB Schema: JPA generate it. No migrations

## Commands

Infraestructure (run from infraestructure, always with --env-file .env):

- `make up` / `make down` / `make logs`: only MySQL
- `make clean`: delete the volume of the app

Backend (run from backend/ticked-classifier):

- `./mvnw spring-boot:run`: export the root .env variables first; Spring does not read .env. Without them startup fails with Failed to parse the host:port pair 'localhost:${MYSQL_PORT}'. Alternative: the VS Code launch config, which sets envFile.
- `./mvnw test`: requires a running Docker daemon (Testcontainers pulls mysql:latest) and the same env vars.
- Single test: `./mvnw test -Dtest=TickedClassifierApplicationTests`.
- TestTickedClassifierApplication is a main() that runs the app against Testcontainers: use it to develop and test the LLM + DB flow without starting make up.

Frontend (run from frontend):

- `pnpm dev` (Vite, port 5173) · `pnpm build` · `pnpm preview`.
- `pnpm build` runs tsc && vite build and is the only typecheck. There is no lint, formatter, test runner or CI config in the repo.
- frontend/README.md says npm install: that is stale, use pnpm (lockfile is pnpm-lock.yaml).

## Environment

- The root `.env` is gitignored. Create it by copying `.env.example`
- Required variables: `MYSQL_PORT`, `MYSQL_DATABASE`, `MYSQL_ROOT_PASSWORD`, `MISTRAL_URL`, `MISTRAL_API_KEY`
- Never print, commit or hardcode these values.

## Conventions

- Frontend routing (Open Cells): declare pages in `src/router/routes.ts` with lazy loading: `action: async () => import('../pages/...')`.
- Lit components: custom elements only register on import. New components and pages must be imported from `src/components/index.ts` (the barrel that index.html loads); otherwise the element silently never renders.
- Strict TypeScript: noUnusedLocals and noUnusedParameters are on. `tsconfig.json` has emitDeclarationOnly, so tsc emits `.d.ts` files into ./types.
- Backend: Lombok annotation processing is wired explicitly in maven-compiler-plugin. Keep it if you touch the POM.
- **Language:** code, identifiers, comments and commit messages are in English. User-facing text (UI labels, messages, errors shown on the web) is in Spanish.

## Ways of working

- Branches are named `<issue#>-<slug>`. Commits follow conventional commits (feat:, fix:, refactor:, style:). Everything is merged into main through a PR.
- For changes that touch more than one file, propose a plan and wait for confirmation before editing.
- Keep changes small and focused on one thing at a time.
- When finished, explain what changed, in which files, and how it was verified.

## Boundaries

- Always:
  - Follow the conventions of the file you are editing.
  - Verify the change before calling it done (see Verification).

- Ask first:
  - Adding dependencies (Maven or pnpm).
  - Creating new files or changing the folder structure.
  - Changing JPA entities (alters the real schema) or the API data format (/tickeds, DTOs).
  - Touching the POM, docker-compose or the Makefile.

- Never:
  - Run `make clean` without explicit permission.
  - Read, edit, display or commit .env, or print keys such as MISTRAL_API_KEY.
  - Edit frontend/types/ (generated code).
  - Mass-rename ticked/ticket.
  - Push directly to main.

## Verification

- Frontend: `pnpm build` must pass (it is the only typecheck available).
- Backend: `./mvnw test` must pass, with Docker running and the env vars exported.
- If a change touches both backend and frontend, start both and check the full flow.
- If something could not be verified, say so explicitly instead of assuming it works.
