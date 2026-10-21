# Day 67 — Inspect the packaged artifact and process

**Study: 25 minutes.** Spring Boot “Packaging Executable Archives”, `repackage`, `mainClass`, `classifier`. Trace the parcel archive example before packaging the native slice.

## Worked example

`scripts/teaching/ParcelDemo.java` and `scripts/teaching/parcel-assets/` form a complete JDK-only archive example. Run this independent example with `mkdir -p /tmp/parcel-demo/assets /tmp/parcel-demo/out && cp scripts/teaching/ParcelDemo.java /tmp/parcel-demo/ && cp scripts/teaching/parcel-assets/* /tmp/parcel-demo/assets/ && (cd /tmp/parcel-demo && javac --add-modules jdk.httpserver -d out ParcelDemo.java && jar --create --file parcel-demo.jar --main-class ParcelDemo -C out . -C assets . && jar --list --file parcel-demo.jar)`. Launch it with `java --add-modules jdk.httpserver -jar /tmp/parcel-demo/parcel-demo.jar`; this command leaves your working directory unchanged. The process prints an ephemeral loopback port; query `/health` (200), `/api/session` (401), and `/test/parcel` (404). Fetch the root and referenced JS to see the rendered asset text. Omitting the asset files from the JAR leaves the process alive but makes the root return 500.

## Transfer

Run `scripts/package-sprint5-demo.sh` to build and receipt a fresh `...-sprint5.jar`; it does not launch or clean up a database. Inspect manifest Start-Class and `BOOT-INF/classes/static`, then use `scripts/sprint5-demo.sh` for the unique tmpfs PostgreSQL project bound to loopback, which removes only its project on exit. A missing JDK/Node/Chromium/Docker prerequisite is a setup blocker. The process is a JAR launch, not cloud deployment.

**Practice 85:** 15 preflight, 30 package/inspect, 25 endpoint/cleanup observations, 15 diagnose. **Commands:** `scripts/package-sprint5-demo.sh`; `./mvnw -Plocal-app,learner-tests -Dtest=Sprint5PackagedInfrastructureTest#day67FreshSprint5ArchiveHasDedicatedEntryAndNormalAssets+day67Sprint5ArchiveServesOnlyTheBoundedPackagedSurface test`.
