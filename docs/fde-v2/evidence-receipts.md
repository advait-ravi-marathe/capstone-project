# Later-milestone evidence artifact receipts

The M2.1–M5.3 acceptance commands accept `--receipt PATH` in `--mode evidence`.
This is a local artifact inventory format, not a milestone pass, trusted human
signature, proof of learner authorship, or verification that a cloud/provider
event occurred. A successful command returns
`ARTIFACTS_PRESENT_REQUIRES_INDEPENDENT_REVIEW`; it verifies only that declared
files exist under the receipt directory and match their SHA-256 digests.

## Manifest format

Place a `receipt.json` beside the artifacts it references:

```json
{
  "schema": "fde-v2-evidence-artifacts/v1",
  "milestone": "AIFDE-V2-M2.2",
  "artifacts": [
    {
      "key": "server_access_tests",
      "path": "server-access-test-output.txt",
      "sha256": "<64 lowercase hex digits>"
    }
  ]
}
```

Use relative paths only. `..`, absolute paths, symlinks, missing files,
duplicate keys, malformed hashes, changed bytes and the wrong milestone are
rejected. Keys represent candidate evidence categories, not trusted assertions.
The command reports missing categories but never converts the manifest into a
`PASS` or completion record.

Example from repository root:

```sh
python -m ai.fde_v2.m22.acceptance \
  --milestone AIFDE-V2-M2.2 --mode evidence --receipt ./evidence/m22/receipt.json
```

The required keys are printed when the manifest is missing keys. Make a hash
with `sha256sum FILE` or Python `hashlib.sha256`; record the exact producing
command, return code, environment/dependency versions, date, actual learner
attempt and source commit alongside each artifact. Do not put credentials,
tokens, private customer data, raw prompts or protected content in receipts.

## Independent review procedure

An independent reviewer checks the artifact bytes against source/provider
records where applicable; confirms exact command and exit status; verifies
whether evidence is actual learner work, a deterministic fixture, a real
identity/provider/cloud response, a peer review, or a role-play; checks current
prerequisites and frozen configuration; and records separate PASS/FAIL/BLOCKED
results per capability. A self-entered JSON boolean, copied log, test double,
synthetic fixture, code presence, CI result or elapsed day is never sufficient
for a learner gate. Real customer contact, account activity, cloud spend and
provider calls retain their separate authorization requirements.

No manifest or learner-progress record is created by the scaffold acceptance
commands. Absence of a receipt remains `BLOCKED`; a valid receipt is only ready
for the indicated human/provenance review.
