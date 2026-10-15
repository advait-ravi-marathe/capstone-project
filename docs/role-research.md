# Role research, advice provenance, and practice boundaries

## Selected roles

The role requirements below come from the public job descriptions. They guide the practice themes; they are not claims about a particular interview loop.

### SDE II — Seller Springboard (Amazon job 10579529)

The posting describes ISPX Registration and AI-assisted seller onboarding: seller registration, identity verification, compliance, scalable reliable services, and work with product/business/compliance partners. It calls for technical ownership across design, testing, operations and delivery, and mentions LLM/ML systems. Accordingly the SDE track practices DSA, idempotent registration, country-aware policy, auditability, external identity-provider failures, safe assisted extraction, and system design. The AI exercises are guardrail discussions with synthetic input; they do not imply ML training or production AI expertise.

Source: [Amazon Jobs — Software Development Engineer II, Seller Springboard, job 10579529](https://www.amazon.jobs/en/jobs/10579529/software-dev-engineer-ii-seller-springboard).

### Front-End Engineer II — FBA (Amazon job 10575321)

The posting describes FBA placement experiences that help sellers make inventory placement decisions across cost, capacity, delivery and network constraints. It emphasizes reusable UI patterns, responsive/performance-conscious and accessible interfaces, API-backed distributed systems, graceful handling of latency/failure/asynchronous or changing data, testing and operational ownership. The Front-End track practices form and catalog behavior, accessible state, resilient quote data, typed payload boundaries, performance and privacy. The sample React app is only a synthetic interface demonstration.

Source: [Amazon Jobs — Front-End Engineer II, job 10575321](https://amazon.jobs/en/jobs/10575321/front-end-engineer-ii).

## Verified public interview guidance

- Amazon's [SDE II interview preparation page](https://amazon.jobs/content/en/how-we-hire/sde-ii-interview-prep) describes coding, system design and Leadership Principle/behavioral evaluation. The daily 35/15/10 split here is our adaptation to your strict 60-minute-per-role assignment, not Amazon's official round timing.
- Amazon's [Leadership Principles](https://www.amazon.jobs/content/en/our-workplace/leadership-principles) provide the principle names used in the manifest. STAR prompts ask for true experiences; do not invent outcomes or metrics.
- The official Take U Forward [Striver pattern sheet](https://takeuforward.org/prep-hub/strivers-180-master-dsa-patterns) and [A2Z DSA sheet](https://takeuforward.org/prep-hub/strivers-a2z-dsa-sheet) are the main DSA references. The sequence touches hashing, windows, stacks, binary search, linked structures, trees, heaps, intervals, graphs, recursion, tries, DP, greedy, union-find, caches, API/data and testing patterns.
- Hello Interview provides free [system design, coding, low-level design and behavioral material](https://www.hellointerview.com/learn) and [guided system-design practice](https://www.hellointerview.com/practice/overview). For video-based practice, use the [Hello Interview YouTube search results](https://www.youtube.com/results?search_query=Hello+Interview+system+design) to find current material. This link is a search query, not a verified channel page.

## Public interview accounts: anecdotal, not predictive

Use these to notice formats and topic families, then solve a new variation. Posters' recollections are self-reported and may omit context; dates and locations are recorded only when available on the post itself.

| Role/source | Reported context | Topics the author described | How this sprint uses it |
|---|---|---|---|
| [Amazon SDE 2 — India](https://leetcode.com/discuss/post/6417079/Amazon-or-SDE-2-or-India/) | India label; page date not established | Sliding-window minimum-length subarray and concatenated-words/trie; behavioral discussion | Window/trie practice, with different synthetic inputs |
| [Amazon SDE 2 — selected, June 2025](https://leetcode.com/discuss/post/6923887/amazon-sde-2-interview-experience-select-rwpm/) | June 2025 in title; location not relied on | System-design discussion included syncing configuration across devices; LP | Design a different reliable registration/resume service |
| [Amazon SDE 2 — 4-month journey](https://leetcode.com/discuss/post/6898756/amazon-sde-2-interview-experience-4-mont-fslv/) | Exact date/location not verified | URL-shortener HLD and preparation notes; rejected outcome is the poster's report | Practice architecture communication; no copied prompt |
| [Amazon SDE-2 all rounds](https://leetcode.com/discuss/post/6855714/amazon-sde-2-all-rounds-interview-experi-4q2t/) | Bangalore stated in post; date not verified | DSA, HLD, LLD, LPs | Mix DSA with design and STAR each day |
| [Amazon SDE-2 Chennai](https://leetcode.com/discuss/post/7176045/amazon-sde-2-chennai-rejected-by-anonymo-txfr/) | Chennai; page published 10 Sep 2025 | LRU, booking-system LLD/concurrency, book-recommendation system, LPs | Cache, concurrency and service-design patterns with fresh scenarios |
| [Amazon Front End Engineer 2 — Canada](https://leetcode.com/discuss/post/627600/Amazon-or-Front-End-Engineer-2-or-Canada/) | Canada label; page date not verified | Accessible UI component, tree/DFS, system design, tab/keyboard behavior | Accessible components and UI architecture |
| [Amazon Virtual Onsite — FrontEnd Engineer II (L5)](https://leetcode.com/discuss/post/1984996/amazon-virtual-onsite-april-2022-fronten-qiku/) | Vancouver stated in title; April 2022 | Pagination state, restaurant-ordering design, OOD, coding and LPs | State/pagination and design practice, without reproducing tasks |
| [Front-End Engineer II rejected account](https://discuss.frontendlead.com/t/amazon-front-end-ii-interview-rejected/1749) | Author says USA in discussion; posted Apr 2025 | UI components, algorithms, mobile/system design, relative-time component, LP | UI behavior and mobile/resilience review |

These individual accounts are anecdotal and do not establish that any question will recur. Use them to notice topic families, then practice fresh problems.

## Scope and safety of examples

Fixtures are synthetic and contain no credentials or personal data. The exercises are independent practice contracts, not descriptions of Amazon internal systems. The tasks do not claim legal/compliance correctness, actual company interview questions, or completed learner skills. Day 30 ends 14 November 2026; CoverStory/Scrum resumes 15 November 2026.
