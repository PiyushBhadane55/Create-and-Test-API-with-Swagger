const { test, expect } = require('@playwright/test');

test.describe('Swagger UI Interface Tests', () => {

  test('Swagger UI page loads and displays Customer API title', async ({ page }) => {
    await page.goto('/swagger-ui.html');

    // Verify title header contains "Customer Management API"
    const apiTitle = page.locator('.title');
    await expect(apiTitle).toContainText('Customer Management API');

    // Verify version tag
    const versionBadge = page.locator('.version');
    await expect(versionBadge).toContainText('1.0');
  });

  test('Swagger UI renders all Customer API endpoints', async ({ page }) => {
    await page.goto('/swagger-ui.html');

    // Check presence of API operations
    const getButtons = page.locator('.opblock-summary-method:has-text("GET")');
    const postButtons = page.locator('.opblock-summary-method:has-text("POST")');
    const putButtons = page.locator('.opblock-summary-method:has-text("PUT")');
    const deleteButtons = page.locator('.opblock-summary-method:has-text("DELETE")');

    await expect(getButtons.first()).toBeVisible();
    await expect(postButtons.first()).toBeVisible();
    await expect(putButtons.first()).toBeVisible();
    await expect(deleteButtons.first()).toBeVisible();
  });
});
