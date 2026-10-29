# Spring MVC backend interview drill

This isolated Java 21 / Spring Boot 3.5.6 project tests actual Spring MVC request mapping, constructor injection, Jakarta validation, exception mapping and `MockMvc` behavior. It is a 30-minute role-bank exercise and does not change or become a dependency of the capstone application.

The starter `PilotControllerAdvice` intentionally maps validation failures to HTTP200. The reference directory contains the one-file repair. Preserve the original, copy only `src` to a scratch location, implement the error mapping, and run the same three tests.

Run `./verify.sh`. It first proves that the starter fails at the intended assertion, then overlays only the instructor reference advice into an isolated temporary copy and proves all three Spring MVC tests pass. The project-local launcher downloads the pinned Maven 3.9.9 distribution from Maven Central into `.mvn/wrapper/dists`, verifies its published SHA-512 checksum, and stores dependency cache in `.mvn/repository`. No system installation or global settings are changed.

The exercise uses synthetic tenant IDs and a mocked service. It tests request validation and dependency wiring only; it does not establish authentication, tenant authorization or production security. The header is a fixture input, not a trusted identity mechanism.
