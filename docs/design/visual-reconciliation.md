# CoverStory visual reference reconciliation

The selected design input is preserved byte-exact under [`docs/source-inputs/visual-2026-10-14`](../source-inputs/visual-2026-10-14/README.md). Its archive hash is `68443daaa4c59147af5622e9931578ee82f7e8d26b39784818dff34c398a4baa`; all 118 member checksums passed. It contains 87 HTML wireframes, 24 PNG previews, shared CSS/theme/screen inventory, and source generators. It is design evidence, not a completed app.

I inspected these actual previews: `health-390.png`, `compare-1440.png`, `home-signed-in-1440.png`, and `submission-ready-1440-dark.png`. The selected shell adopts their health-first entry, four category panels, step indicator, clear comparison table, compact source-status labels, warm canvas, and responsive card layout. It uses the mandated Inter/system-ui sans font, replacing older serif typography in the screenshots.

## Current shell implementation

- `web/src/ui/Marketplace.tsx` shows the four category entry points, a health-first synthetic catalog, local shortlist selection (maximum three), and a comparison panel. The category filter is reflected in the URL; shortlist and open-comparison state stay local.
- All displayed sample values are explicitly SYNTHETIC or Unknown. There are no insurer names, actual premiums, health answers, or payment/refund actions. Quote review remains unavailable.
- `web/src/state/SessionBoundary.tsx` leaves public discovery visible while session revalidation is pending or unavailable. It withholds the prior identity during a refresh; private resume stays unavailable until authenticated ownership and an API exist.
- `web/src/styles.css` uses semantic Tailwind tokens, responsive cards/table, visible focus indicators, and reduced-motion handling. Inter is bundled from the open-source font package.

## Remaining visual work

Only the health discovery and comparison slice is implemented. The other 86 screens have not been mapped into React routes or validated with accessible interaction tests. The static source pages remain references; their controls do not implement the business journeys. Exact original pixel parity is not a goal where it conflicts with current typography, state, security, or source-evidence contracts.

## Checks

- `npm run build` — TypeScript compile and Vite production build passed.
- `npm run test:security` — session-race/cache smoke passed, including public discovery during session refresh.
- Chromium smoke at 390×844: health discovery rendered, two synthetic examples were selected, the comparison panel opened, and source/Unknown labels remained visible. Screenshot artifacts were temporary in `/tmp` and are not stored as product evidence.
