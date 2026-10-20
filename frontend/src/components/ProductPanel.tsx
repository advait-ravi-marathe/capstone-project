export type CoverageOption = {
  code: string;
  label: string;
};

export type Product = {
  code: string;
  name: string;
  coverageOptions: CoverageOption[];
};

export type ProductPanelState =
  | { status: 'loading' }
  | { status: 'error'; message: string }
  | { status: 'empty' }
  | { status: 'ready'; product: Product };

export function ProductPanel({ state: _state }: { state: ProductPanelState }) {
  // LEARNER TODO: render the state explicitly, use accessible labels for choices,
  // show every typed coverage option, and do not fetch from a hard-coded URL.
  return <section aria-label="Product options">Day 17 learner TODO</section>;
}
