# Dependency refresh and scan record — 2026-10-15

This record captures the dependency versions and checks completed on 2026-10-15. “Latest” means the latest stable release found in the official npm registry or Maven Central metadata at scan time. A latest version can still contain a vulnerability; this report is not a security certification.

## Dependency updates

- Backend: Spring Boot `4.1.1` with its imported dependency BOM; Java release remains `21`. JUnit is pinned with the `6.1.3` JUnit BOM. PostgreSQL JDBC is `42.7.14`; embedded PostgreSQL binaries are `18.6.0` (wrapper `2.2.2`). Jackson 3 is aligned at `3.2.3` while the API’s Jackson annotation compatibility remains on `com.fasterxml.jackson.annotation` `2.22`.
- The Boot BOM had stable releases behind Maven Central for 19 resolved transitive components. They are explicitly aligned in `pom.xml`: Tomcat `11.0.27`, Logback `1.6.5`, SLF4J `2.0.20`, Log4j `2.26.1`, SnakeYAML `2.7`, Commons Logging `1.4.0`, Checker Framework annotations `4.3.1`, Commons Lang `3.21.0`, XZ `1.12`, Commons IO `2.22.0`, and Commons Codec `1.23.0`, plus Jackson. The final Maven BOM comparison checked all 60 project components against stable numeric releases in Central metadata and found no older component versions.
- Maven build and wrapper versions are pinned: Maven `3.10.0` with the official Maven Wrapper `3.3.4` and a checked `distributionSha256Sum`; Spring Boot Maven plugin `4.1.1`, resources plugin `3.5.0`, compiler plugin `3.16.0`, Surefire `3.6.0`, CycloneDX Maven plugin `2.9.3`.
- Frontend dependencies are exact-pinned in `web/package.json` and `web/package-lock.json`. React and React DOM are `19.3.1`; the remaining direct dependencies are Inter `5.3.0`, TanStack React Query `5.104.2`, Tailwind `4.3.3`, Vite `8.3.4`, TypeScript `7.0.2`, React Vite plugin `6.1.2`, and React types `19.3.0`. Node `>=22.12.0` is declared. `npm outdated` returned an empty result after the lock refresh.
- No Python dependency manifest or third-party Python import was found. The repository’s Python utilities use the standard library, so there is no declared Python dependency graph to audit.

## Vulnerability scan results

| Ecosystem | Tool and data | Result | Coverage |
| --- | --- | --- | --- |
| npm | `npm audit` with npm `11.9.0`, current npm registry advisories | 0 info, 0 low, 0 moderate, 0 high, 0 critical across 115 lockfile packages (7 production, 109 development, 76 optional; scopes overlap) | Completed for the lockfile, including optional platform packages. The result is time-bound and does not prove the packages are vulnerability-free. |
| Maven | OWASP Dependency-Check Maven `13.0.0`, with automatic updates; optional CISA KEV and hosted suppressions disabled for retry | **Incomplete; no vulnerability result produced.** NVD returned HTTP 403 (the scanner reports 403/404 and exits nonzero). Initial attempts also received HTTP 403 from CISA KEV and OWASP hosted suppressions. No API key or other credential was available or created. | Resolved Java dependency graph is represented in the SBOM, but not vulnerability-scanned. No cached result was treated as current. |
| Python | No third-party dependencies declared or imported | No package graph to scan | No `pip-audit` result is claimed. |

The OWASP Dependency-Check attempt used the project’s latest Maven dependency graph and attempted a fresh NVD update. Its final command exited with status 1 after the NVD 403. A prior invocation configured not to fail Maven printed a build success despite the scanner’s fatal analysis error; that invocation is not counted as a scan result.

## SBOMs and licenses

- [Java CycloneDX SBOM](coverstory-java-2026-10-15.cdx.json): CycloneDX 1.6, 60 project components, including test scope. All 60 components have license metadata. The SBOM is the application dependency graph; Maven build plugin implementation dependencies are not included.
- [Web CycloneDX SBOM](coverstory-web-2026-10-15.cdx.json): CycloneDX 1.5 generated from the full lockfile, 115 components including development and optional platform packages. All 115 components have license metadata.
- [npm audit summary](npm-audit-2026-10-15.json) contains the sanitized counts only. Raw scanner output and temporary logs are not committed.

## Regression evidence

- `./mvnw test` passed with 285 tests, zero failures/errors, and one skipped test using Java 21 and the pinned Maven 3.10.0 wrapper.
- `./mvnw -Plearner-tests test` compiled and ran 541 tests. It intentionally ended with 58 assertion failures and 193 errors in learner acceptance tests because saved learner TODOs remain unimplemented; this is not a green test run. The default infrastructure baseline passed separately.
- `npm ci`, `npm run build`, `npm run test:security`, and `npm run test:catalog-fixture` passed. The CAP-168 Chromium flow and CAP-58 browser queue flow passed. The CAP-166 learner acceptance command still stops at its preserved decoder TODO. CAP-54's browser flow is exercised through its test-only session adapter by the passing Maven integration suite; running that script against the ordinary demo server is not a valid equivalent setup.

## Reproduction

```sh
./mvnw test
./mvnw -Plearner-tests test
cd web
npm ci
npm audit
npm sbom --package-lock-only --sbom-format cyclonedx
```

The default Maven suite is the instructor and infrastructure baseline. The learner profile intentionally runs unfinished learner acceptance tests; on this snapshot it reports failures at preserved learner TODOs. A passing instructor baseline or clean npm audit does not establish independent learner mastery or prove absence of all vulnerabilities.

To retry the blocked Java scan when NVD access is available:

```sh
./mvnw org.owasp:dependency-check-maven:13.0.0:check -Dformats=JSON
```
