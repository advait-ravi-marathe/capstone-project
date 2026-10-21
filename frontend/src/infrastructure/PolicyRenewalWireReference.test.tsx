import { describe, expect, it } from 'vitest';
import { renewalPreviewForDisplay } from '../e2e/PolicyRenewalPreviewBrowserHarness';

const preview = (premium: unknown) => ({ policyId: 'policy-demo', sourcePolicyVersion: 3, productVersion: 'home-secure-v1',
  rateVersion: 'home-secure-rate-v1', currentAnnualPremiumRupees: premium, currency: 'INR',
  nextTermEffectiveDate: '2027-12-01', notice: 'Reference only; not an offer.' });

describe('the narrow renewal numeric JSON display adapter', () => {
  it('formats numeric JSON values without recalculating the reference amount', () => {
    expect(renewalPreviewForDisplay(preview(125)).currentAnnualPremiumRupees).toBe('125.00');
    expect(renewalPreviewForDisplay(preview(987.4)).currentAnnualPremiumRupees).toBe('987.40');
  });
  it('rejects the string representation instead of asserting a cast', () => {
    expect(() => renewalPreviewForDisplay(preview('125.00'))).toThrow(/Unexpected preview response/);
    expect(() => renewalPreviewForDisplay(preview(Number.NaN))).toThrow(/Unexpected preview response/);
  });
});
