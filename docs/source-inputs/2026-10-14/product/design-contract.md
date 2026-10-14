# Responsive screens and interaction states

https://dwait-to-adwait.youtrack.cloud/issue/ECOS-49

## Design
[Open the wireframe flow](https://insurance-marketplace-wireframes.advait-marathe7016.chatgpt.site/flow.html) · [Components and tokens](https://insurance-marketplace-wireframes.advait-marathe7016.chatgpt.site/components.html) · [Editable source](https://insurance-marketplace-wireframes.advait-marathe7016.chatgpt.site/wireframes-source.zip).

## Journey
Discover category → choose supported scenario and optional cover → compare → before-you-buy summary → login → authoritative quote review → mock Juspay payment → onboarding and adjustment reconciliation → exact final proposal review → explicit submission → insurer decision → policy/help. Renewal and cancellation use their recorded submission and payment boundaries.

## Screen contract
Discovery and comparison cover desktop/mobile and all four categories. Quote records show evidence type, scenario, source date, frequency and tax status. No unsupported profile receives an extrapolated quote. Summary preserves restrictions and source version. Price review shows old/current total and terms; drift above₹1 or changed terms blocks payment until reviewed.
Payment includes pending, unknown, failed and confirmed states. Success opens onboarding; no active-cover message. Onboarding includes checklist, disclosures, optional confirmed extraction, documents, save/resume, validation, session expiry and edit conflict. Revised-offer and policy/help screens distinguish payment from insurer decisions.

## Account and revised-price screens
Homepage/category include Login and signed-out return guidance. Signed-in variants show resume cards and policies. Login retains the quote and leads to authoritative review before mock payment. Onboarding shows initial and edited details, including PIN code, with new quote review. Dedicated adjustment states cover additional payment and refund pending/success/failure/unknown. Changed inputs are outside the unchanged-snapshot ₹1 target.

## Interaction and accessibility
Visible labels, keyboard focus, logical tab order, 44px controls, error association and status announcements. Compare equivalent labelled rows on mobile with deliberate overflow. Keep critical exclusions expanded. Errors preserve selections/drafts. Server validation and ownership remain authoritative.

## Tokens
Ink #242326; paper #F7F4EC; lavender #B9A4E3; blue #7AC5DF; yellow #F2C84B; green-yellow #CCD27A; chocolate #765344; stone #A5ABA4. Georgia display, sans-serif UI,16–18px body. Spacious paper surfaces for discovery; calm purchase/support. coverstory is provisional branding.

## Review evidence
Responsive static wireframes and semantic/link/contrast checks are available. Browser execution testing is not complete. Product implementation must verify focus, state announcements and persistence in the running application. Requirements: [ECOS-1](https://dwait-to-adwait.youtrack.cloud/issue/ECOS-1), [ECOS-3](https://dwait-to-adwait.youtrack.cloud/issue/ECOS-3), [ECOS-4](https://dwait-to-adwait.youtrack.cloud/issue/ECOS-4), [ECOS-5](https://dwait-to-adwait.youtrack.cloud/issue/ECOS-5), [ECOS-6](https://dwait-to-adwait.youtrack.cloud/issue/ECOS-6), [ECOS-8](https://dwait-to-adwait.youtrack.cloud/issue/ECOS-8).

## Premium-change disclosure
Place a readable note beside quote prices, summaries and payment review: premiums depend on the recorded inputs and source version. Changes to PIN, age, health/other disclosures, cover, riders, policy term or payment options may change premium or eligibility; insurer review can also revise the offer. Show changed details and terms before acceptance. After payment, increases use a separate balance payment and reductions use a tracked refund. Footnotes supplement the explicit change-review screen and must not hide material changes.

## Input controls and validation
Use labelled radio chips for short single-choice sets (product, smoking, payment frequency, Yes/No/Unsure), checkbox chips for genuine multi-choice sets, and native calendar date inputs with text/keyboard entry. Date-of-birth is a date-only value; reject future dates and calculate age using the insurer’s documented convention at the quote date. Calendar convenience does not replace server validation.
PIN input accepts exactly six ASCII digits starting1–9; the versioned supported master decides existence/coverage, not regex. Display verified city/locality and state beneath the field; loading, unsupported, ambiguous locality and unavailable lookup are distinct states. Tier is a separate versioned marketplace classification, never an inferred insurer pricing zone.
Names accept Unicode letters/marks, spaces, apostrophes and hyphens with a sensible1–120-character bound; reject control characters and blank-only input. A narrow Latin-only regex is unsuitable. Preserve the entered spelling and escape output. Exceptional legitimate names need an accessible correction/help path.
Normalize vehicle registration case and separators for matching while preserving display. Use separate format validators for standard state registrations and Bharat-series; route supported exceptions rather than treating one regex as proof of registration or ownership. Verify allowed state/RTO/series/year rules against an authoritative source before production. Server rules and clear field-linked errors are authoritative.


## Add-ons and bundles
Show sourced plan-specific optional and included benefits without preselecting optional paid cover. Each selection preserves product/UIN, variant, channel, source/version, eligibility, dependencies, exclusions, limits and selected-option IDs. Explain incompatible options. Compare base cover and selected configuration separately; show unknown incremental prices as Unknown. A source bundle total is not the sum of guessed rider prices. Changing an option requotes and requires review before payment or adjustment. No unsupported combination can proceed with a payable total.

## Light and dark themes
Both themes cover desktop/mobile discovery, forms, comparisons, payment, onboarding, policy/help and all recovery states. Default to the device preference until explicitly changed; retain the chosen theme across pages without account data. A labelled keyboard-operable toggle exposes its state. Dark surfaces use #1D1C20 and #29272D with #F7F4EC text; muted text #C2BEC8 and borders #8F8898. Original lavender/blue/yellow/green category accents retain dark #242326 text. Errors use text and icons, never colour alone. Verify normal-text contrast >=4.5:1, essential control/focus contrast >=3:1, zoom, native form controls, forced-colour support and reduced motion. Print/export uses a legible light treatment. Persisting theme must fail harmlessly if storage is unavailable. Theme choice never changes selections, terms, money or application state.

## Onboarding and proposal boundary
Payment confirmation unlocks owned onboarding; it never sends a proposal to the insurer. Dispatch requires completed required fields/disclosures, confirmed extraction corrections, no unresolved questions, acceptance of the exact current proposal/quote/options revision, completed payment reconciliation and an explicit final submit action. Retain initial and edited details. Insurer submission is a separate durable state with an external reference.
Cancellation before any insurer dispatch is handled by the marketplace. Serialize cancellation with the dispatch claim; cancel or invalidate queued work atomically. An in-flight or unknown dispatch must be reconciled before treating the case as unsubmitted or granting a cancellation refund. After confirmed submission, show the insurer's verified contact route and applicable policy/source information; do not mark cancellation or refund complete from that alert.

## Refund destination and mock wallet
For a confirmed refundable amount, the buyer chooses original payment method or marketplace wallet. Nothing is preselected. Original-method refund shows a proposed 3–5 business-day estimate from provider acceptance, with a configured calendar/timezone and actual status; it is not a verified provider guarantee. A failed or unknown refund retains its reference and recovery action.
Mock wallet credit completes immediately at the successful internal ledger commit and is visible in the balance/history. Eligibility, available captured funds and ownership are checked first. A single unique adjustment allocation prevents crediting the wallet and refunding the same money to origin. Once an origin refund is pending, a route change requires confirmed cancellation of that attempt; uncertainty prevents duplicate credit. Wallet credit is labelled simulated, not a real stored-value facility.
Keep gateway settlement and application payment accounting separate. Application net settled = confirmed external captures + applied wallet debits − confirmed origin refunds − confirmed wallet credits attributed to that application. Pending allocations are reserved. Wallet available = confirmed credits − confirmed debits − active reservations, never negative. Do not double-count a wallet-funded portion as both external capture and wallet debit. Journal entries are immutable and reversals reference originals.
Real stored-value operation, cash-out/expiry/transfer rules, regulatory arrangement and verified provider refund timing are unresolved launch dependencies. The mock does not imply authorization to hold customer funds.

## Renewal and cancellation
Signed-in home and policy detail show expiry date from the verified policy record and a renew action before expiry. A renewal creates a linked application and fresh quote/configuration; never overwrite the existing policy or imply uninterrupted coverage before evidence. Reconfirm changed rating details, eligibility, disclosures, options and effective dates.
If no active mandate exists, show an in-app review-and-pay nudge with current amount/status and a secure route to quote review; the nudge cannot directly charge or skip login/consent. Active mandate, inactive/revoked mandate, unknown payment, already-paid renewal and expired policy have distinct states. Do not create or collect a real mandate. Expired/lapsed cases require the insurer's source-defined process and cannot promise backdated cover.
Before proposal submission, show cancellation confirmation, refundable-amount evidence, refund destination and reconciliation state. Afterwards, show an alert with the verified insurer contact route; retain the record for reference and help. Do not invent cancellation entitlement, amounts or insurer processing time.

## Guided review and data downloads
[10:35 chaptered demo](https://insurance-marketplace-wireframes.advait-marathe7016.chatgpt.site/demo/index.html) covers all87defined wireframe pages plus eight dark views. It is a captioned rendered design walkthrough, not production behavior evidence. [Quote evidence workbook](https://insurance-marketplace-wireframes.advait-marathe7016.chatgpt.site/resources/Insurance-Price-Evidence-2026-09-10.xlsx):82published records,520targets,0personalized quotes. [Postal candidate export](https://insurance-marketplace-wireframes.advait-marathe7016.chatgpt.site/resources/Pincode_Master_8000_Candidate.zip):8,000candidates with city verification still pending.


## Linked configuration and lifecycle work
[Separate proposal dispatch, cancellation and refund allocation](https://dwait-to-adwait.youtrack.cloud/issue/ECOS-64), [Version eligible add-ons and bundles as quote configurations](https://dwait-to-adwait.youtrack.cloud/issue/ECOS-54), [Cancel unsubmitted applications and route submitted cases](https://dwait-to-adwait.youtrack.cloud/issue/ECOS-67), [Validate quote inputs with accessible native controls](https://dwait-to-adwait.youtrack.cloud/issue/ECOS-53), [Publish a verified postal validation catalogue](https://dwait-to-adwait.youtrack.cloud/issue/ECOS-52), [Expand sourced quote coverage and option evidence](https://dwait-to-adwait.youtrack.cloud/issue/ECOS-55), [Renew from policy records with fresh details and quote](https://dwait-to-adwait.youtrack.cloud/issue/ECOS-68), [Prompt direct renewal payment when no mandate is active](https://dwait-to-adwait.youtrack.cloud/issue/ECOS-69), [Gate insurer submission on completed onboarding and reconciliation](https://dwait-to-adwait.youtrack.cloud/issue/ECOS-65), [Deliver accessible light and dark purchase journeys](https://dwait-to-adwait.youtrack.cloud/issue/ECOS-60), [Choose refund destination and reconcile wallet credit](https://dwait-to-adwait.youtrack.cloud/issue/ECOS-66).

