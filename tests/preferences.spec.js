const { test, expect } = require('@playwright/test');

const API_ORIGIN = 'http://localhost:8080';
const FRONTEND_ORIGIN = 'http://localhost:5500';
const preferencesEndpoint = `${API_ORIGIN}/api/users/preferences`;
const authenticatedUser = {
  displayName: 'นักศึกษา CSTU ทดสอบ',
  studentId: '6809000000',
  year: 2,
  faculty: 'คณะวิทยาศาสตร์และเทคโนโลยี',
  department: 'วิทยาการคอมพิวเตอร์',
  email: 'student@tu.ac.th',
};
const emptyPreferences = {
  preferredTeachingMethod: null,
  preferredEvaluation: null,
  preferredTeachingSpeed: null,
  preferredAssignmentStyle: null,
  preferredWorkload: null,
};

async function mockPreferencesApi(page, initialPreferences, onSave = () => {}) {
  await page.route('**/api/**', async route => {
    const request = route.request();
    const path = new URL(request.url()).pathname;
    const corsHeaders = {
      'Access-Control-Allow-Origin': FRONTEND_ORIGIN,
      'Access-Control-Allow-Credentials': 'true',
      'Access-Control-Allow-Headers': 'Content-Type',
      'Access-Control-Allow-Methods': 'GET, PUT, OPTIONS',
    };

    if (request.method() === 'OPTIONS') {
      return route.fulfill({ status: 204, headers: corsHeaders });
    }

    if (path.endsWith('/auth/me')) {
      return route.fulfill({
        status: 200,
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        body: JSON.stringify(authenticatedUser),
      });
    }

    if (path.endsWith('/users/preferences') && request.method() === 'GET') {
      return route.fulfill({
        status: 200,
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        body: JSON.stringify(initialPreferences),
      });
    }

    if (path.endsWith('/users/preferences') && request.method() === 'PUT') {
      const body = request.postDataJSON();
      onSave(request, body);
      return route.fulfill({
        status: 200,
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      });
    }

    return route.fulfill({
      status: 404,
      headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      body: JSON.stringify({ error: `Unexpected API request: ${path}` }),
    });
  });
}

test('โหลดค่าความชอบจาก API แล้วแสดงค่าที่เลือกเดิม', async ({ page }) => {
  await mockPreferencesApi(page, {
    preferredTeachingMethod: 100,
    preferredEvaluation: 50,
    preferredTeachingSpeed: 75,
    preferredAssignmentStyle: null,
    preferredWorkload: 25,
  });

  await page.goto(`${FRONTEND_ORIGIN}/pages/preference.html`);

  await expect(page.locator('input[name="teachingApproach"][value="3"]')).toBeChecked();
  await expect(page.locator('input[name="gradingStyle"][value="2"]')).toBeChecked();
  await expect(page.locator('input[name="teachingPace"][value="4"]')).toBeChecked();
  await expect(page.locator('input[name="assignmentStyle"]:checked')).toHaveCount(0);
  await expect(page.locator('input[name="workload"][value="2"]')).toBeChecked();
});

test('คลิกหัวใจที่เลือกอยู่ซ้ำเพื่อล้างค่าความชอบหัวข้อนั้น', async ({ page }) => {
  let savedPreferences;
  await mockPreferencesApi(page, emptyPreferences, (_request, body) => {
    savedPreferences = body;
  });

  await page.goto(`${FRONTEND_ORIGIN}/pages/preference.html`);
  const selectedHeart = page.locator(
    'label.heart-choice:has(input[name="teachingApproach"][value="2"])'
  );
  await selectedHeart.click();
  await expect(page.locator('input[name="teachingApproach"][value="2"]')).toBeChecked();

  await selectedHeart.click();
  await expect(page.locator('input[name="teachingApproach"]:checked')).toHaveCount(0);
  await expect(
    page.locator('[data-preference="teachingApproach"] .heart-choice.is-filled')
  ).toHaveCount(0);

  await page.locator('.preference-save').click();
  await expect(page.locator('#save-dialog')).toBeVisible();
  expect(savedPreferences.preferredTeachingMethod).toBeNull();
});

test('บันทึกคะแนนผ่าน PUT ตาม API contract', async ({ page }) => {
  let savedPreferences;
  await mockPreferencesApi(page, emptyPreferences, (request, body) => {
    expect(request.method()).toBe('PUT');
    savedPreferences = body;
  });

  await page.goto(`${FRONTEND_ORIGIN}/pages/preference.html`);
  await page.locator('label.heart-choice:has(input[name="teachingApproach"][value="2"])').click();
  await page.locator('label.heart-choice:has(input[name="gradingStyle"][value="3"])').click();
  await page.locator('label.heart-choice:has(input[name="teachingPace"][value="4"])').click();
  await page.locator('label.heart-choice:has(input[name="assignmentStyle"][value="1"])').click();
  await page.locator('label.heart-choice:has(input[name="workload"][value="5"])').click();
  await page.locator('.preference-save').click();

  await expect(page.locator('#save-dialog')).toBeVisible();
  expect(savedPreferences).toEqual({
    preferredTeachingMethod: 50,
    preferredEvaluation: 100,
    preferredTeachingSpeed: 75,
    preferredAssignmentStyle: 0,
    preferredWorkload: 100,
  });
});

test('ย้าย Preferences เดิมจาก localStorage เมื่อยังไม่มีข้อมูลบน API', async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.setItem('cstu_preferences', JSON.stringify({
      teachingApproach: 3,
      workload: 1,
    }));
  });

  let migratedPreferences;
  await mockPreferencesApi(page, emptyPreferences, (_request, body) => {
    migratedPreferences = body;
  });

  await page.goto(`${FRONTEND_ORIGIN}/pages/preference.html`);

  await expect(page.locator('input[name="teachingApproach"][value="3"]')).toBeChecked();
  await expect(page.locator('input[name="workload"][value="1"]')).toBeChecked();
  await expect.poll(() => page.evaluate(() => localStorage.getItem('cstu_preferences'))).toBeNull();
  expect(migratedPreferences).toEqual({
    preferredTeachingMethod: 100,
    preferredEvaluation: null,
    preferredTeachingSpeed: null,
    preferredAssignmentStyle: null,
    preferredWorkload: 0,
  });
});

test('แสดง error เมื่อ API บันทึก Preferences ไม่สำเร็จ', async ({ page }) => {
  await mockPreferencesApi(page, emptyPreferences);
  await page.route(`${preferencesEndpoint}`, async route => {
    if (route.request().method() === 'PUT') {
      return route.fulfill({
        status: 503,
        headers: {
          'Access-Control-Allow-Origin': FRONTEND_ORIGIN,
          'Access-Control-Allow-Credentials': 'true',
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ error: 'ระบบฐานข้อมูลไม่พร้อมใช้งาน กรุณาลองใหม่ภายหลัง' }),
      });
    }
    return route.fallback();
  });

  await page.goto(`${FRONTEND_ORIGIN}/pages/preference.html`);
  await page.locator('label.heart-choice:has(input[name="workload"][value="3"])').click();
  await page.locator('.preference-save').click();

  await expect(page.locator('#preference-status')).toContainText(
    'ระบบฐานข้อมูลไม่พร้อมใช้งาน กรุณาลองใหม่ภายหลัง'
  );
  await expect(page.locator('#save-dialog')).not.toBeVisible();
});
