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

  await expect(page.locator('#error-msg')).toContainText(
    'ระบบนี้อนุญาตให้ใช้งานเฉพาะนักศึกษาสาขาวิชาวิทยาการคอมพิวเตอร์ เท่านั้น'
  );
});

test('logout แล้วกด Back ไม่แสดงข้อมูลที่ต้อง login และกลับหน้า login', async ({ page }) => {
  let authenticated = true;
  const corsHeaders = {
    'Access-Control-Allow-Origin': 'http://localhost:5500',
    'Access-Control-Allow-Credentials': 'true',
    'Access-Control-Allow-Headers': 'Content-Type',
    'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
  };
  const testUser = {
    displayName: 'นักศึกษา CSTU ทดสอบ',
    studentId: '6809000000',
    year: 2,
    faculty: 'คณะวิทยาศาสตร์และเทคโนโลยี',
    department: 'วิทยาการคอมพิวเตอร์',
    email: 'student@tu.ac.th',
  };

  await page.route('**/api/**', async route => {
    const request = route.request();
    const path = new URL(request.url()).pathname;

    if (request.method() === 'OPTIONS') {
      return route.fulfill({ status: 204, headers: corsHeaders });
    }

    if (path.endsWith('/auth/me')) {
      return route.fulfill({
        status: authenticated ? 200 : 401,
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        body: JSON.stringify(authenticated ? testUser : { detail: 'กรุณาเข้าสู่ระบบอีกครั้ง' }),
      });
    }

    if (path.endsWith('/auth/logout')) {
      authenticated = false;
      return route.fulfill({ status: 204, headers: corsHeaders });
    }

    return route.fulfill({
      status: 200,
      headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      body: '[]',
    });
  });

  await page.goto('http://localhost:5500/pages/courses.html');
  await expect(page.locator('.app-shell')).toBeVisible();
  await expect(page.locator('#user-id')).toHaveText('6809000000');

  await page.locator('.profile-banner').click();
  await expect(page.locator('#p-id')).toHaveText('6809000000');

  await page.locator('.btn-logout').click();
  await expect(page).toHaveURL(/\/pages\/index\.html$/);

  await page.goBack();
  await expect(page).toHaveURL(/\/pages\/index\.html\?reason=session-expired$/);
  await expect(page.locator('#login-form')).toBeVisible();
  await expect(page.locator('#p-id')).toHaveCount(0);
});
