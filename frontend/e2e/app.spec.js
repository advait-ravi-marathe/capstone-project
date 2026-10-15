import { test, expect } from '@playwright/test';
test('real React learner app is navigable and usable at a narrow viewport', async ({ page }) => {
  await page.goto('/');
  await expect(page.getByRole('heading', { name: 'Seller registration form with errors' })).toBeVisible();
  await page.getByLabel('Seller name').fill('Synthetic Seller');
  await page.getByLabel('Email address').fill('seller@example.test');
  await page.getByRole('button', { name: 'Validate registration' }).click();
  await expect(page.getByRole('status')).toContainText('Learner TODO: Day 01');
  await page.getByLabel('Go to day').selectOption('28');
  const fees = page.getByRole('tab', { name: 'Fees' });
  await page.getByRole('tab', { name: 'Options' }).focus();
  await page.keyboard.press('ArrowRight');
  await expect(fees).toHaveAttribute('aria-selected', 'true');
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true);
});
