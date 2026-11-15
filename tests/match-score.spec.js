const { test, expect } = require('@playwright/test');

const FRONTEND_ORIGIN = 'http://localhost:5500';
const COURSE_CODE = 'CS261';
const SECTION_ID = 'cs261_100001';
const authenticatedUser = {
  displayName: 'นักศึกษา CSTU ทดสอบ',
  studentId: '6800000000',
  year: 2,
  faculty: 'คณะวิทยาศาสตร์และเทคโนโลยี',
  department: 'วิทยาการคอมพิวเตอร์',
  email: 'student@dome.tu.ac.th',
};
const readyScore = {
  status: 'READY',
  score: 86,
  level: 'เข้ากันได้ดีมาก',
  explanation: 'รูปแบบการเรียนของ Section นี้ตรงกับ Preferences ของคุณหลายด้าน',
  sectionReviewCount: 12,
  availableFactorCount: 5,
  totalFactorCount: 5,
  coveragePercentage: 100,
  details: [{
    key: 'teachingMethod',
    label: 'แนวทางการสอน',
    preferenceScore: 100,
    preferenceLabel: 'เน้นปฏิบัติ',
    sectionProfileScore: 100,
    sectionProfileLabels: ['เน้นปฏิบัติ'],
    reviewCount: 12,
    topVoteCount: 8,
    consensusPercentage: 67,
    similarityScore: 100,
    weightPercentage: 20,
  }],
};

async function openMatchScorePage(page, { authenticated = true, scoreResponse = readyScore, onScoreRequest } = {}) {
  const corsHeaders = {
    'Access-Control-Allow-Origin': FRONTEND_ORIGIN,
    'Access-Control-Allow-Credentials': 'true',
    'Access-Control-Allow-Headers': 'Content-Type',
    'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
  };

  await page.route('**/api/**', async route => {
    const request = route.request();
    const path = new URL(request.url()).pathname;
    const json = body => route.fulfill({
      status: 200,
      headers: { ...corsHeaders, 'Content-Type': 'application/json; charset=utf-8' },
      body: JSON.stringify(body),
    });

    if (path.endsWith('/auth/me')) {
      if (!authenticated) {
        return route.fulfill({
          status: 401,
          headers: { ...corsHeaders, 'Content-Type': 'application/json; charset=utf-8' },
          body: JSON.stringify({ error: 'กรุณาเข้าสู่ระบบก่อน' }),
        });
      }
      return json(authenticatedUser);
    }

    if (path === `/api/courses/${COURSE_CODE}`) {
      return json({
        courseId: COURSE_CODE,
        courseName: 'Introduction to Software Engineering',
        teacher: 'อาจารย์ทดสอบ',
        sections: [{ sectionNumber: '1', sectionId: SECTION_ID, instructorName: 'อาจารย์ทดสอบ' }],
      });
    }

    if (path.endsWith('/match-score')) {
      if (onScoreRequest) return onScoreRequest(route);
      return json(scoreResponse);
    }

    if (path.endsWith('/reviews/eligibility')) return json({ eligible: false, alreadyReviewed: false });
    if (path.includes('/reviews')) return json([]);
    return json({});
  });

  await page.goto(`${FRONTEND_ORIGIN}/pages/course-detail.html?code=${COURSE_CODE}&section=1`);
}

test('แสดงคะแนน Match Score จาก API ได้ตรงค่า', async ({ page }) => {
  await openMatchScorePage(page);
  await expect(page.locator('#match-score-value')).toHaveText('86');
  await expect(page.locator('#match-score-progress')).toHaveAttribute('aria-valuenow', '86');
  await expect(page.locator('#match-score-content')).toBeVisible();
});

test('เปิดรายละเอียดแล้วเห็นปัจจัยและคะแนนย่อย', async ({ page }) => {
  await openMatchScorePage(page);
  await page.locator('.match-score-details summary').click();
  await expect(page.locator('#match-score-details-list')).toContainText('แนวทางการสอน');
  await expect(page.locator('#match-score-details-list')).toContainText('Preference ของคุณ: เน้นปฏิบัติ');
  await expect(page.locator('#match-score-details-list')).toContainText('รูปแบบของ Section: เน้นปฏิบัติ');
  await expect(page.locator('#match-score-details-list')).toContainText('100% เหมือนกัน');
});

test('แสดงคำอธิบายที่สอดคล้องกับผลคะแนน', async ({ page }) => {
  await openMatchScorePage(page);
  await expect(page.locator('#match-score-explanation')).toContainText('ตรงกับ Preferences');
  await expect(page.locator('#match-score-level')).toHaveText('เข้ากันได้ดีมาก');
});

test('แสดงสถานะไม่มีข้อมูลโดยไม่แสดงคะแนน', async ({ page }) => {
  await openMatchScorePage(page, {
    scoreResponse: {
      status: 'NO_DATA', score: null, level: null,
      explanation: 'ยังไม่มีข้อมูล Preferences หรือรีวิวของ Section นี้เพียงพอสำหรับคำนวณ Match Score',
      sectionReviewCount: 0, availableFactorCount: 0, totalFactorCount: 5,
      coveragePercentage: 0, details: [],
    },
  });
  await expect(page.locator('#match-score-empty')).toContainText('ยังไม่มีข้อมูล');
  await expect(page.locator('#match-score-content')).toBeHidden();
});

test('เมื่อเชื่อมต่อ Backend ไม่ได้ แจ้งข้อผิดพลาดและไม่แสดงคะแนนเดิม', async ({ page }) => {
  await openMatchScorePage(page, {
    onScoreRequest: route => route.abort('failed'),
  });
  await expect(page).toHaveURL(/\/pages\/index\.html\?reason=backend-disconnected$/);
  await expect(page.locator('#error-msg')).toContainText('backend หลุด');
  await expect(page.locator('#match-score-value')).toHaveCount(0);
});

test('ผู้ไม่มี session ถูกพาไปหน้าเข้าสู่ระบบก่อนเห็น Match Score', async ({ page }) => {
  await openMatchScorePage(page, { authenticated: false });
  await expect(page).toHaveURL(/\/pages\/index\.html\?reason=session-expired$/);
  await expect(page.locator('#error-msg')).toContainText('เข้าสู่ระบบอีกครั้ง');
});

for (const score of [0, 100]) {
  test(`แสดงคะแนนขอบเขต ${score} ได้ถูกต้อง`, async ({ page }) => {
    await openMatchScorePage(page, {
      scoreResponse: {
        ...readyScore,
        score,
        level: score === 0 ? 'เข้ากันได้น้อย' : 'เข้ากันได้ดีมาก',
      },
    });
    await expect(page.locator('#match-score-value')).toHaveText(String(score));
    await expect(page.locator('#match-score-progress')).toHaveAttribute('aria-valuenow', String(score));
    expect(await page.locator('#match-score-progress-fill').evaluate(element => element.style.width)).toBe(`${score}%`);
  });
}

test('หยุด Loading และแสดงข้อผิดพลาดเมื่อ API ตอบกลับล้มเหลว', async ({ page }) => {
  await openMatchScorePage(page, {
    onScoreRequest: route => route.fulfill({
      status: 503,
      headers: { 'Content-Type': 'application/json; charset=utf-8' },
      body: JSON.stringify({ error: 'ระบบฐานข้อมูลไม่พร้อมใช้งาน' }),
    }),
  });
  await expect(page.locator('#match-score-loading')).toBeHidden();
  await expect(page.locator('#match-score-error')).toContainText('ระบบฐานข้อมูลไม่พร้อมใช้งาน');
  await expect(page.locator('#match-score-retry')).toBeVisible();
  await expect(page.locator('#match-score-content')).toBeHidden();
});
test('แสดง Loading จนกว่า Match Score จะโหลดเสร็จ', async ({ page }) => {
  let releaseResponse;
  const delayedResponse = new Promise(resolve => { releaseResponse = resolve; });
  await openMatchScorePage(page, {
    onScoreRequest: async route => {
      await delayedResponse;
      await route.fulfill({
        status: 200,
        headers: { 'Content-Type': 'application/json; charset=utf-8' },
        body: JSON.stringify(readyScore),
      });
    },
  });

  await expect(page.locator('#match-score-loading')).toBeVisible();
  await expect(page.locator('#match-score-content')).toBeHidden();
  releaseResponse();
  await expect(page.locator('#match-score-value')).toHaveText('86');
  await expect(page.locator('#match-score-loading')).toBeHidden();
});
