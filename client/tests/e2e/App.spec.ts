import { expect, test } from "@playwright/test";
import { clearUser } from "../../src/utils/api";

/**
  The general shapes of tests in Playwright Test are:
    1. Navigate to a URL
    2. Interact with the page
    3. Assert something about the page against your expectations
  Look for this pattern in the tests below!
 */

const SPOOF_UID = "mock-user-id";

test.beforeEach(
  "add spoof uid cookie to browser",
  async ({ context, page }) => {
    // - Add "uid" cookie to the browser context
    await context.addCookies([
      {
        name: "uid",
        value: SPOOF_UID,
        url: "http://localhost:8000",
      },
    ]);

    // wipe everything for this spoofed UID in the database.
    await clearUser(SPOOF_UID);
  }
);

/**
 * Don't worry about the "async" yet. We'll cover it in more detail
 * for the next sprint. For now, just think about "await" as something
 * you put before parts of your test that might take time to run,
 * like any interaction with the page.
 */
test("on page load, I see the gearup screen and skip auth.", async ({
  page,
}) => {
  // Notice: http, not https! Our front-end is not set up for HTTPs.
  await page.goto("http://localhost:8000/");
  await expect(page.getByLabel("Gearup Title")).toBeVisible();
  // <i> with aria-label favorite-words-header should include the SPOOF_UID
  await expect(page.getByLabel("user-header")).toContainText(SPOOF_UID);
});

test("I can add a word to my favorites list", async ({ page }) => {
  await page.goto("http://localhost:8000/");
  // - get the <p> elements inside the <ul> with aria-label="favorite-words"
  const favoriteWords = await page.getByLabel("favorite-words");
  await expect(favoriteWords).not.toContainText("hello");

  await page.getByLabel("word-input").fill("hello");
  await page.getByLabel("add-word-button").click();

  const favoriteWordsAfter = await page.getByLabel("favorite-words");
  await expect(favoriteWordsAfter).toContainText("hello");

  // .. and this works on refresh
  await page.reload();
  const favoriteWordsAfterReload = await page.getByLabel("favorite-words");
  await expect(favoriteWordsAfterReload).toContainText("hello");
});

test("Map is visible", async ({ page }) => {
  await page.goto("http://localhost:8000");

  const map = await page.$(".map");
  expect(await map?.isVisible()).toBe(true);
});

test("Search input and button are visible", async ({ page }) => {
  await page.goto("http://localhost:8000");

  const searchInput = await page.$(".search-container input");
  const searchButton = await page.$(".search-container button");
  expect(await searchInput?.isVisible()).toBe(true);
  expect(await searchButton?.isVisible()).toBe(true);
});

test("Search input and button are functional", async ({ page }) => {
  await page.goto("http://localhost:8000");

  const searchInput = await page.$(".search-container input");
  const searchButton = await page.$(".search-container button");
  await searchInput?.fill("test");
  await searchButton?.click();
  await page.waitForResponse(
    (response) =>
      response.url().includes("overlayData") && response.status() === 200
  );
});

test("Clear Pins button is visible", async ({ page }) => {
  await page.goto("http://localhost:8000");

  const clearPinsButton = await page.$(".map button");
  expect(await clearPinsButton?.isVisible()).toBe(true);
});

test("Clear Pins button is functional", async ({ page }) => {
  await page.goto("http://localhost:8000");

  const clearPinsButton = await page.$(".map button");
  await clearPinsButton?.click();
  await page.waitForResponse(
    (response) =>
      response.url().includes("clearUser") && response.status() === 200
  );
});

test("I can add a pin to the map", async ({ page }) => {
  await page.goto("http://localhost:8000/");

  // Assume that the map has a label "map"
  const map = await page.getByLabel("map");

  // Simulate a click on the map
  await map.click();

  // Wait for the pin to be added
  await page.waitForSelector(".pin");

  // Check if the pin is visible
  const pin = await page.$(".pin");
  expect(await pin?.isVisible()).toBe(true);

  // Reload the page
  await page.reload();

  // Check if the pin is still there after reload
  const pinAfterReload = await page.$(".pin");
  expect(await pinAfterReload?.isVisible()).toBe(true);
});
