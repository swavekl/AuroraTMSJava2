import {expect, test} from '@playwright/test';

test('after two resend requests, Start Over clears credentials and returns to Create Account', async ({page}) => {
  let resendRequests = 0;

  await page.route('**/api/users/isregistered/**', route => route.fulfill({
    status: 200,
    contentType: 'application/json',
    body: JSON.stringify({userRegistered: false})
  }));
  await page.route('**/api/users/register', route => route.fulfill({
    status: 200,
    contentType: 'application/json',
    body: JSON.stringify({status: 'SUCCESS', profileId: 'playwright-user', verificationEmailSent: true})
  }));
  await page.route('**/api/users/resend-verification', route => {
    resendRequests++;
    return route.fulfill({
      status: 202,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 'SUCCESS',
        message: 'Another email was sent. Please check again.'
      })
    });
  });

  await page.goto('/ui/login/register');
  await page.getByPlaceholder('First Name').fill('Playwright');
  await page.getByPlaceholder('Last Name').fill('User');
  await page.getByPlaceholder('Email').fill('playwright@example.com');
  await page.getByPlaceholder('Password', {exact: true}).fill('GoodPassword1!');
  await page.getByPlaceholder('Reenter Password').fill('GoodPassword1!');
  await expect(page.getByRole('button', {name: 'SIGN UP'})).toBeEnabled();
  await page.getByRole('button', {name: 'SIGN UP'}).click();

  await expect(page.getByText('We sent a verification email to playwright@example.com.')).toBeVisible();
  await expect(page.getByText("Can't find the email? Check your spam or junk folder. You can request another below.")).toBeVisible();
  const resendButton = page.getByRole('button', {name: 'Resend Email'});
  await resendButton.click();
  await expect(page.getByText('Another email was sent. Please check again.')).toBeVisible();
  await expect(page.getByText("Can't find the email? Check your spam or junk folder. You can request another below.")).toBeVisible();
  await resendButton.click();

  await expect(page.getByText("You've reached the resend limit. Want to start over with a different email?")).toBeVisible();
  await expect(page.getByText("Can't find the email? Check your spam or junk folder", {exact: false})).toBeVisible();
  await expect(page.getByText('You can request another below.')).toHaveCount(0);
  await expect(resendButton).toHaveCount(0);
  expect(resendRequests).toBe(2);

  await page.getByRole('button', {name: 'Start Over'}).click();

  await expect(page.getByRole('heading', {name: 'Create Account'})).toBeVisible();
  await expect(page.locator('input[name="email"]')).toHaveValue('');
  await expect(page.locator('input[name="password"]')).toHaveValue('');
  await expect(page.locator('input[name="password2"]')).toHaveValue('');
});
