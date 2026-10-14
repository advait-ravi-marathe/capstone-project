# CoverStory frontend stack and state contract

Status: proposed curriculum specification; package presence, versions, wiring and runnable tests are not established. This supersedes older generic fetch-hook and Georgia/Arial/Proxima Nova requirements in the active CoverStory task mapping. Preserve the recovered clean, youthful, low-clutter visual direction and the existing React SPA/Spring architecture.

## Shared foundations

- TanStack React Query v5 owns fetched server state, read caching and mutation status. Form drafts/validation belong to their local form layer; route/filter/sort state belongs to URL/router state. Do not copy fetched collections into a competing global store.
- One stable QueryClient is supplied at the app boundary. Each domain owns typed query-key/options factories and API-specific hooks. Shared UI primitives receive typed data/actions and never fetch directly from another domain.
- Keys contain a stable schema/resource namespace plus every data-changing owner/resource/page/filter/sort input. Owner IDs partition cache but never authorize server access. Keep credentials and consumer form values out of keys.
- Private cache is memory-only by default. Refresh means session/bootstrap resolution, a truthful skeleton or signed-out/error state, then an authenticated refetch and current owned journey resume. Preserve safe deep links and URL filters. A cold refresh is not SSR hydration.
- Do not introduce Next.js, TanStack Start or SSR. If a later isolated exercise studies cache restoration, use only synthetic public reference data, with version buster, finite maxAge and gcTime at least maxAge. Missing, expired, corrupt or denied storage falls back to API fetching. Private data and queued mutations remain excluded.
- Define staleTime, gcTime and retry by data class. Distinguish pending-without-data from background fetching with existing data, empty, unavailable/offline and failed states. Suitable transient read failures may retry within a fixed cap; 401/403/validation failures do not loop. Do not use blanket write retry or add polling by accident.
- Pass the queryFn AbortSignal into the actual transport. On logout/account switch, cancel old private requests and discard old private query/mutation caches and form state before rendering the new identity. Late A responses cannot refill B's UI; server authorization still applies.
- Begin with server-confirmed mutation updates and narrow invalidation of affected detail/list/summary keys. A failed submission preserves the draft. No page-load/refetch effect creates a payment, submit or renewal mutation. Teach optimistic rollback later only on a safe reversible seam.

## Shared design system

Use Tailwind semantic tokens and shared parameterized primitives/compositions across modules. Verify the installed major before implementation; the current official documentation describes v4 @theme. Do not silently upgrade a repository dependency merely to match a page.

Planned shared areas are frontend/src/coverstory/shared/ui/, frontend/src/coverstory/shared/theme/ and frontend/src/coverstory/shared/query/. Domain modules consume them through public interfaces. The instructor prepares mechanical provider/build wiring; learner tasks each implement or test one bounded seam. Existing paths remain proposals until code-owner reconciliation.

- Tokens cover semantic colors and interaction states, spacing, radius, shadow, focus and typography. No repeated arbitrary module-local palette.
- Typed finite props such as variant, size and tone map to complete static utility strings. Do not interpolate class fragments such as bg-${color}-600. Include shared-source classes in production scanning and verify the built CSS.
- Reusable Button, Field, EmptyState, Card, Dialog and typed list/table compositions expose meaningful data/action slots. Keep domain queries in module hooks. Prefer a small explicit API rather than a growing collection of boolean flags.
- Button checks: accessible name, native keyboard activation, focus, disabled/loading behavior and all supported variants in built CSS.
- Field checks: label/control association, help/error references, required/invalid semantics, keyboard operation and non-color-only error indication.
- Dialog checks: meaningful name, initial focus, Tab/Shift-Tab containment, Escape/close behavior, background inertness and correct focus return.

## Typography

Use verified open-source **Inter** with a system-ui sans-serif fallback. The font remains a token; preserve visual hierarchy, restrained weights, spacing and readability. No paid-font purchase or Proxima Nova licensing gate remains.

Define title, section, body, label and caption tokens with family, size, line-height, weight and tracking. Semantic heading levels remain independent of visual size. Retain the Inter copyright and SIL Open Font License with distributed assets. Future asset download/self-hosting belongs to the later implementation stage.

Test slow font load and request failure, font-display: swap, narrow widths and zoom. Critical text stays readable; no clipped controls, invisible labels or broken layout. Measure layout shift and tune fallback metrics only from observations.

## Bounded progressive acceptance

Each daily frontend task replaces its earlier implementation seam rather than adding this whole specification to the day. Across the mapped days, prove:

1. One state inventory separates server, form, URL and local state; background refetch does not overwrite unsaved input.
2. Query provider/key behavior is stable and test caches are isolated. Semantically equal keys reuse data; owner/resource/filter changes do not.
3. Cold deep-link refresh resolves session before private reads, shows fallback UI and restores current authorized journey state. No previous owner's data flashes.
4. Query cancellation and account-switch tests reject late stale responses; invalidation is limited to affected resources.
5. Each mutation uses the domain's exact version/idempotency/acceptance rules. No duplicate business action follows from refresh or retry.
6. Two domain modules consume the same typed primitive/composition and tokens; the production build includes every finite variant.
7. Accessibility, font fallback, narrow-layout and failure-state evidence is recorded with actual commands/screenshots or NOT_RUN.
8. Screen analytics emits only the separate journey/security contract's allowlisted event metadata, independent of business transitions and essential UI operation.

## Official references and focused sections

Checked by official-document research on 14 October 2026. Match installed versions during later setup.

- Server versus client state: https://tanstack.com/query/latest/docs/framework/react/guides/does-this-replace-client-state
- Query-key dependencies and serializable keys: https://tanstack.com/query/latest/docs/framework/react/guides/query-keys
- Freshness, garbage collection and defaults: https://tanstack.com/query/latest/docs/framework/react/guides/important-defaults
- Bounded query retry: https://tanstack.com/query/latest/docs/framework/react/guides/query-retries
- Targeted invalidation: https://tanstack.com/query/latest/docs/framework/react/guides/query-invalidation
- AbortSignal and cancellation: https://tanstack.com/query/latest/docs/framework/react/guides/query-cancellation
- Optional public-only persistence exercise: https://tanstack.com/query/latest/docs/framework/react/plugins/persistQueryClient
- Conditional SSR terminology only: https://tanstack.com/query/latest/docs/framework/react/guides/ssr
- Tailwind theme variables: https://tailwindcss.com/docs/theme
- Static class maps and source detection: https://tailwindcss.com/docs/detecting-classes-in-source-files#dynamic-class-names
- Font family tokens: https://tailwindcss.com/docs/font-family#customizing-your-theme
- Text size tokens: https://tailwindcss.com/docs/font-size#customizing-your-theme
- Inter source: https://rsms.me/inter/
- Inter SIL OFL 1.1: https://github.com/rsms/inter/blob/master/LICENSE.txt
- Accessible button: https://www.w3.org/WAI/ARIA/apg/patterns/button/
- Accessible modal dialog: https://www.w3.org/WAI/ARIA/apg/patterns/dialog-modal/
- Font-display behavior: https://developer.mozilla.org/en-US/docs/Web/CSS/Reference/At-rules/@font-face/font-display
