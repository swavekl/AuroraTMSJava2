import {expect, test} from '@playwright/test';

const registrationEmail = process.env.LIVE_REGISTRATION_EMAIL;
const registrationPassword = process.env.LIVE_REGISTRATION_PASSWORD;
const liveRunEnabled = process.env.LIVE_REGISTRATION_ENABLED === 'true';

test.describe.configure({mode: 'serial', retries: 0});
test.use({trace: 'off', screenshot: 'off'});

test('live registration supports resending verification and starting over', async ({page}) => {
  test.skip(
    !liveRunEnabled || !registrationEmail || !registrationPassword,
    'Set LIVE_REGISTRATION_ENABLED=true, LIVE_REGISTRATION_EMAIL, and LIVE_REGISTRATION_PASSWORD to run against Okta.'
  );
  test.setTimeout(120_000);

  await page.goto('/ui/login/register');
  await page.getByPlaceholder('First Name').fill('Nonokta');
  await page.getByPlaceholder('Last Name').fill('One');
  await page.getByPlaceholder('Email').fill(registrationEmail!);
  await page.getByPlaceholder('Email').press('Tab');
  await page.getByPlaceholder('Password', {exact: true}).fill(registrationPassword!);
  await page.getByPlaceholder('Reenter Password').fill(registrationPassword!);
  await expect(page.getByRole('button', {name: 'SIGN UP'})).toBeEnabled();

  const responsePromise = page.waitForResponse(response =>
    response.url().includes('/api/users/register') && response.request().method() === 'POST'
  );
  await page.getByRole('button', {name: 'SIGN UP'}).click();
  const response = await responsePromise;

  expect(response.status(), 'Live registration endpoint status').toBe(200);
  const body = await response.json();
  expect(body.status).toBe('SUCCESS');
  expect(body.verificationEmailSent).toBe(true);
  await expect(page.getByText(`We sent a verification email to ${registrationEmail}.`, {exact: false})).toBeVisible();

  let resendRequestCount = 0;
  page.on('request', request => {
    if (request.url().includes('/api/users/resend-verification') && request.method() === 'POST') {
      resendRequestCount++;
    }
  });

  const resendButton = page.getByRole('button', {name: 'Resend Email'});
  const firstResend = page.waitForResponse(response =>
    response.url().includes('/api/users/resend-verification') && response.request().method() === 'POST'
  );
  await resendButton.click();
  expect((await firstResend).status()).toBe(202);
  await expect(page.getByText('Another email was sent. Please check again.')).toBeVisible();

  const secondResend = page.waitForResponse(response =>
    response.url().includes('/api/users/resend-verification') && response.request().method() === 'POST'
  );
  await resendButton.click();
  expect((await secondResend).status()).toBe(202);
  expect(resendRequestCount).toBe(2);

  await expect(page.getByText("You've reached the resend limit. Want to start over with a different email?")).toBeVisible();
  await expect(resendButton).toHaveCount(0);
  await page.getByRole('button', {name: 'Start Over'}).click();

  await expect(page.getByRole('heading', {name: 'Create Account'})).toBeVisible();
  await expect(page.locator('input[name="email"]')).toHaveValue('');
  await expect(page.locator('input[name="password"]')).toHaveValue('');
  await expect(page.locator('input[name="password2"]')).toHaveValue('');
});
