const { test, expect } = require('@playwright/test');

test('แสดงข้อความเมื่อบัญชีถูกปฏิเสธ', async ({ page }) => {
  await page.route('**/api/**', async route => {
    const request = route.request();
    const corsHeaders = {
      'Access-Control-Allow-Origin': 'http://localhost:5500',
      'Access-Control-Allow-Credentials': 'true',
      'Access-Control-Allow-Headers': 'Content-Type',
      'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
    };

    if (request.method() === 'OPTIONS') {
      return route.fulfill({ status: 204, headers: corsHeaders });
    }

    if (new URL(request.url()).pathname.endsWith('/auth/login')) {
      return route.fulfill({
        status: 403,
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        body: JSON.stringify({
          detail: 'ระบบนี้อนุญาตให้ใช้งานเฉพาะนักศึกษาสาขาวิชาวิทยาการคอมพิวเตอร์ เท่านั้น',
        }),
      });
    }

    return route.fulfill({
      status: 200,
      headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      body: '[]',
    });
  });

  await page.goto('http://localhost:5500/pages/index.html');
  await page.locator('#student-id').fill('6809600000');
  await page.locator('#password').fill('test-password');
  await page.locator('#login-button').click();

  await expect(page.locator('.alert')).toContainText(
    'ระบบนี้อนุญาตให้ใช้งานเฉพาะนักศึกษาสาขาวิชาวิทยาการคอมพิวเตอร์ เท่านั้น'
  );
});