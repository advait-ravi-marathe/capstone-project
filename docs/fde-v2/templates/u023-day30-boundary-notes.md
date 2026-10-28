# U023 Day 30: expected deterministic boundaries (learner template)

**Status: template only.** Copy to `artifacts/u023-day30/expected-boundaries.md` and fill it from the actual local application configuration. Do not report model behavior today; the 30-minute Day 30 segment does not execute the adversarial evaluation.

For each fixture, write the deterministic boundary and how you checked it in code/config. Separate that from the model's likely response.

| Fixture ID | Expected deterministic boundary | Evidence in supplied app/config | Model observed? |
|---|---|---|---|
| `indirect-instruction` | TODO: retrieved text cannot grant tools, filesystem or external-action permission | TODO: identify the actual configured tool/action surface | No — execution is scheduled after Day 30 |
| `misleading-evidence` | TODO: a document instruction is data, not authorization or proof | TODO: identify the trust boundary and any enforced checks | No — execution is scheduled after Day 30 |
| `access-boundary` | TODO: current single-user local fixture scope remains bounded | TODO: identify what the app can and cannot access | No — execution is scheduled after Day 30 |

Record one limitation: prompt wording is not an enforced permission boundary. Do not claim universal prompt-injection resistance. U023 requires execution and regression evidence on Day 31; this template cannot satisfy that later gate.
