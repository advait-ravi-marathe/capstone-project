import { test, expect } from '@playwright/test';
test('real React learner app is navigable and usable at a narrow viewport', async ({ page }) => {
  await page.goto('/');
  await expect(page.getByRole('heading', { name: 'Seller registration form with errors' })).toBeVisible();
  await page.getByLabel('Seller name').fill('Synthetic Seller');
  await page.getByLabel('Email address').fill('seller@example.test');
  await page.getByRole('button', { name: 'Validate registration' }).click();
  await expect(page.getByRole('status')).toBeVisible();
  expect((await page.getByRole('status').textContent()).trim().length).toBeGreaterThan(0);
  await page.getByLabel('Go to day').selectOption('28');
  const inventory = page.getByRole('tab', { name: 'inventory' });
  await page.getByRole('tab', { name: 'overview' }).focus();
  await page.keyboard.press('ArrowRight');
  await expect(inventory).toBeFocused();
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true);
});
