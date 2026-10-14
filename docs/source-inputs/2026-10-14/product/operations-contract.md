# Reliability, cost and acceptance verification

https://dwait-to-adwait.youtrack.cloud/issue/ECOS-20

## Signals and proposed targets
Request logs: timestamp, requestId, route template, status, duration, safe business outcome and catalog revision. Metrics request count, duration histogram, errors, process CPU/memory/GC, DB pool wait/active. Never answer text, contact details, tokens or unbounded user IDs in labels.
SLI technical success = eligible requests meeting expected protocol and durable outcome / all eligible attempts; track validation rejections separately from service failures. Measure server latency histogram end-to-end UI latency separately. A 30-day SLO is proposed later; small synthetic runs establish only test conditions, not production availability.
Views: discovery success/error/latency; DB pool saturation; commands conflict/dedup; later payment unknown age, outbox oldest age/retries and issuance state age. Health liveness means process alive; readiness means able to serve DB-dependent routes. DB loss must not trigger endless process restarts.
Proposed sandbox alerts: errors>5% for2minutes and at least100requests → stop workload and inspect trace; pool wait p95>100ms for5minutes → inspect slow queries/connections; payment unknown>15minutes → reconcile sandbox reference; outbox oldest>5minutes → inspect worker. No alerts installed yet.

## Capacity and benchmark
Assumptions: one2vCPU/4GiB application+DB sandbox,10k catalog versions,1k synthetic applications, response10KiB average; no real adoption. Workload90%catalogGET,10%draftwrites=100%;20offered req/s for10min after2minwarmup,3matched repetitions. Target p95<500ms,error<1%; hypothesis only. 20req/s×600s=12,000offered operations/run,11.72MiB/min raw response traffic approximate; no capacity claim before measurement.
Approximate in-flight=successful req/s×mean seconds only under stable conditions. Storage=rows/day×bytes/row×retention + indexes + backups; record measured serialized/SQL size rather than inventing row size.
Collect offered/completed/failed/timeout/dropped iterations, latency histograms p50/95/99 and scope,CPU/RSS/GC/pool/SQL plans and all3runs. Do not average percentiles. Check generator saturation. Stop memory>80%configured limit,error>5%1minute or any invariant breach; stop generator, preserve logs, restore fixture. Only isolated own environment, never public-site load.
One intervention after baseline: e.g. query/index change with EXPLAIN and identical dataset/config. A worse/inconclusive run stays in evidence. 10×projection is a future test hypothesis.

## Failure and recovery matrix
DB unavailable: readiness fails,API503safe error,selection/draft retained in client; no blind write retry. Restore DB, verify stored version and retry with samekey.
Pool saturation: bounded requesttimeout5s proposed,poolwait1s; reject excess with503, observe queue not unbounded threads.
Concurrent draft: versioncondition yields one winner,412loser; show reload/compare, never overwrite.
Timeout after commit: UI outcome uncertain; GETresource or replay samekey resolves; no duplicate mutation.
Provider timeout after possible charge: UNKNOWN; reconcile provider reference before another attempt; no automatic success/failed assumption.
App death after outbox commit: new worker claims expiredlease, dedupe effect, delivery at-least-once. Bounded5attempts with increasing delay then repairqueue; inspect oldestage and authorization before replay.
Duplicate/out-of-order insurer event: store unique event, compare aggregate/sourceversion, retain older evidence without state regression.
Storage denied: preserve in-memory shortlist and offer export; state device-local limitation.
Restore exercise: snapshot synthetic fixture, write known change, restore isolated copy; compare rowcounts/FKs/immutable hashes and application states; merely restarting is not recovery evidence.
Every incident record: simulated/real, environment+commit, timeline with observed timestamps, impact denominator, causal hypotheses, detection gaps, containment, invariant verification, one prevention and one retest task. Results currently Not run.

## Cost choices and formulas
Authorization: local/sandbox only; no paid purchase/deploy. Local incremental provider spend0, electricity/device/time unknown, no claim totalcost0.
Candidate A: single Lightsail Linux publicIPv4 2vCPU4GB80GB listed USD24/month; Mumbai transfer allowance is half published4TB, hence2TB. No redundancy; app+DB compete. Candidate B: IPv6-only same memory/CPU/disk USD20/month, assess client/network compatibility. Candidate C: managed app+managed PostgreSQL separates recovery responsibilities but costs and region-specific floors unpriced; require quote before selection.
Official source https://aws.amazon.com/lightsail/pricing/ checked10-Sep-2026. Prices exclude applicable taxes/exchange effects; no INR conversion invented. Recheck checkout/region before authorization. Account budget unknown.
Monthly total = compute + database + snapshotsGB×snapshotRate + objectsGB×storageRate + outboundOverageGB×regionRate + logsIngestGB×rate + logsRetainedGB×rate + AI + domain/email/paymentvendor charges + tax. Unknown rates remain variables; never zero-fill.
Per workload = monthly attributable fixedcost/completed eligible operations + variable per-operation cost. Report denominator and utilization; low load makes fixedcost/request high.
AI peroperation = inputTokens×inputRate/1M + outputTokens×outputRate/1M + embeddings + retrieval + storage; record model/date/currency and caching discounts only when verified. Pre-call tokenmax, requestbudget and monthlyhardstop must be configured before paidcalls. Hostedmodel vs local inference includes localhardware/ops tradeoff.
Budgets after user chooses capB: alerts50/80/100%B, stop optional jobs at80%, hardstop AI at100%; cloud billing alert alone is not a spend cap. Local synthetic logs proposed7days, sampled successes/errors retained carefully; rotate disk and capsize. Remove temporary datasets after exercise, retain sanitized evidence.
Shutdown: stop workers/load immediately after session; inventory/delete unused billable compute/IP/storage only after backup/evidence and explicit resource ownership. Stopped resource may still bill—check vendor before assuming savings. No shutdown action executed.
Build vs buy: build product workflow; use existing auth/payment integrations when authorized, do not build cryptography or card vault. No additional broker or shared cache without a measured requirement.

## QA acceptance matrix
QA01 ECOS-1 AC1–3: fourcategories,phonefree,Demo/source labels; UI+API; Not run.
QA02 ECOS-1 AC4–6: compare equalattributes/unknowns,refresh/export exactversion; UI+SQL+storagefailure; Not run.
QA03 ECOS-3: two-principal ownership and stale-save interleaving; realPostgreSQLintegration; Not run.
QA04 ECOS-4: changedoffer exactconsent,duplicatecallback,uncertaincharge,amount/currency mismatch; contract+integration; Not run.
QA05 ECOS-5: out-of-ordertimeline,no prematurecover,documentpermission; integration+UI; Not run.
QA06 ECOS-6: category-specific fields and no guarantee/lowestpricebias; content+UI; Not run.
QA07 ECOS-7: benchmark,restore,redaction,accessibility390/1440/keyboard/200%zoom; Not run.
QA08 ECOS-8: sourcegrounding,abstention,injection,PII isolation,confirmation,latency/cost caps; synthetic holdout; Not run.
Evidence each: commit,environment,date,fixture,steps,expected,actual,Pass/Fail/Blocked/Notrun,raw output link. Critical data/access loss or high coreacceptance failure blocks release. No auto-close from testcount.

## Account, repricing and refund acceptance
QA11: anonymous discovery → login → same selected quote → server validation; home/category resume cards contain only owned applications/policies; generic signed-out guidance; logout and expired-session recovery. QA12: original versus changed PIN/details retained; exact new-scenario quote or unavailable state; increase creates one balance payment; decrease creates one refund; pending/failed/unknown states visible; repeated edits and duplicate callbacks cannot overcharge or over-refund. Net-payment arithmetic uses integer paise and confirmed events, with reservations for pending effects. No real payment/refund is executed. Results pending.

## Quote accuracy and performance
Track exact-lookup hit/miss, static asset bytes/download time/parse time, pre-payment validation latency, drift in integer paise and stale/withdrawn rejection. Keep scenario IDs out of metric labels; use bounded category/outcome labels. Target100% exact amount agreement for the same immutable snapshot; count denominator and all mismatches. A maximum observed regression residual is not a bound on future quotes. Benchmark static lookup versus indexed SQL under matched source data before claiming improvement. No load testing of insurer or aggregator systems.
QA09: stale static file versus updated SQL;0/1/100/101paise drift; same amount but changed terms; missing tax; unsupported scenario; tampered price; no payment before review. QA10: confirmed mock payment unlocks one onboarding case, while pending/unknown does not. Results pending.


## Refund destination and mock wallet
For a confirmed refundable amount, the buyer chooses original payment method or marketplace wallet. Nothing is preselected. Original-method refund shows a proposed 3–5 business-day estimate from provider acceptance, with a configured calendar/timezone and actual status; it is not a verified provider guarantee. A failed or unknown refund retains its reference and recovery action.
Mock wallet credit completes immediately at the successful internal ledger commit and is visible in the balance/history. Eligibility, available captured funds and ownership are checked first. A single unique adjustment allocation prevents crediting the wallet and refunding the same money to origin. Once an origin refund is pending, a route change requires confirmed cancellation of that attempt; uncertainty prevents duplicate credit. Wallet credit is labelled simulated, not a real stored-value facility.
Keep gateway settlement and application payment accounting separate. Application net settled = confirmed external captures + applied wallet debits − confirmed origin refunds − confirmed wallet credits attributed to that application. Pending allocations are reserved. Wallet available = confirmed credits − confirmed debits − active reservations, never negative. Do not double-count a wallet-funded portion as both external capture and wallet debit. Journal entries are immutable and reversals reference originals.
Real stored-value operation, cash-out/expiry/transfer rules, regulatory arrangement and verified provider refund timing are unresolved launch dependencies. The mock does not imply authorization to hold customer funds.
