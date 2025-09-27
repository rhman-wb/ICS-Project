# Repository Guidelines

## Project Structure & Module Organization
- backend/ Spring Boot (Java 17); code in `backend/src/main/java`, tests in `backend/src/test/java`, resources in `backend/src/main/resources`, build output in `backend/target/`.
- frontend/ Vue 3 + Vite + TypeScript; source in `frontend/src`, unit tests in `src/**/*.{test,spec}.ts` or `__tests__`, E2E in `frontend/tests/e2e`, assets in `frontend/public` and `src/assets`.
- database/ SQL init and helpers; nginx/ reverse-proxy conf; scripts/ performance test scripts; Docs/ and Architecture/ for reference docs and standards.

## Build, Test, and Development Commands
- Backend (run inside `backend/`):
  - `mvn spring-boot:run -Dspring-boot.run.profiles=dev -s settings.xml` start dev server.
  - `mvn clean verify -s settings.xml` unit tests + JaCoCo thresholds; reports in `target/site/jacoco/`.
  - `mvn -Pintegration-test verify -s settings.xml` runs `*IT.java` or `*IntegrationTest.java` via Failsafe.
  - `mvn clean package -DskipTests -s settings.xml` build JAR.
- Frontend (run inside `frontend/`):
  - `npm ci` install; `npm run dev` serve; `npm run build` prod build; `npm run lint`/`npm run format` fix style.
  - `npm test` unit tests; `npm run test:coverage` coverage; `npm run test:e2e` Playwright E2E.

## Coding Style & Naming Conventions
- Root `.editorconfig`: UTF-8, LF, final newline, trim trailing whitespace.
- Frontend: ESLint + Prettier (2-space indent, single quotes, no semicolons, print width 100). Vue components PascalCase; events camelCase. Use aliases like `@`, `@components`, `@views`.
- Backend: Standard Java conventions (PascalCase classes, lower-case packages). Mirror production packages in tests. See `Architecture/backend-standards.md`.

## Testing Guidelines
- Backend: JUnit 5, Mockito, Testcontainers. Coverage gates: lines >= 85%, branches >= 80%, methods >= 90% (bundle); per-class line >= 80% excluding config/dto/entity/enums. Use `-Pintegration-test` for integration tests.
- Frontend: Vitest thresholds set in `vitest.config.ts` (lines/statements >= 85%). E2E resides in `frontend/tests/e2e` with reports in `playwright-report/` and `test-results/`.

## Commit & Pull Request Guidelines
- History mixes Conventional Commits and descriptive CN messages. Prefer Conventional Commits: `fix|refactor|chore(scope): subject` (<= 72 chars), body explains why, link issues (e.g., `#123`).
- PRs include: summary, scope (frontend/backend), test evidence (coverage, screenshots, or `playwright-report` link), local run steps, and updated docs/configs when needed.

## Security & Configuration Tips
- Do not commit secrets. Use `.env*` files locally. For Maven speed use `backend/settings.xml`. DB init lives in `database/`; Nginx conf in `nginx/`.

## 基础原则
- 永远使用中文回答, 包括其中的推理过程
- 每次提出需求后, 请先提供方案, 不要直接修改代码
- 每次提供建议在建议上依次标记序号

## Coding Style
- 永远使用中文注释