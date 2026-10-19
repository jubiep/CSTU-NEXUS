/* ==========================================
   CSTU NEXUS - Shared JS + API integration
   ========================================== */

const isLocalFrontend = ["localhost", "127.0.0.1"].includes(window.location.hostname);
const API_BASE_URL = (
  window.CSTU_API_BASE_URL
  || (isLocalFrontend ? `http://${window.location.hostname}:8080/api` : `${window.location.origin}/api`)
).replace(/\/+$/, "");
const AUTH_STATE_KEY = "cstu_authenticated";
const mockCurrentUser = {
  name: "มะละกอ ส้มตำ",
  studentId: "6809670670",
  year: 2,
  faculty: "คณะวิทยาศาสตร์และเทคโนโลยี สาขาวิทยาการคอมพิวเตอร์"
};

/* ค้นหารายวิชาจาก Backend (หากยังเชื่อมไม่ได้ ให้ใช้ mockCourses แทนอัตโนมัติ) */
async function apiFetchCourses(keyword = "") {
  const trimmed = keyword.trim();
  const url = trimmed 
    ? `${API_BASE_URL}/courses?keyword=${encodeURIComponent(trimmed)}`
    : `${API_BASE_URL}/courses`;

  try {
    const response = await fetch(url);
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    return await response.json();
  } catch (error) {
    if (!trimmed) return mockCourses;
    const q = trimmed.toLowerCase();
    return mockCourses.filter(c =>
      c.courseId.toLowerCase().includes(q) ||
      c.courseName.toLowerCase().includes(q)
    );
  }
}

/**
 * ดึงข้อมูลวิชาและเซกชันจาก Backend (GET /api/courses/{id})
 * หากยังเชื่อมไม่ได้ ให้ใช้ mockCourses แทนอัตโนมัติ
 */
async function apiFetchCourseDetail(courseId) {
  if (!courseId) return null;
  const url = `${API_BASE_URL}/courses/${encodeURIComponent(courseId)}`;

  try {
    const response = await fetch(url);
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    return await response.json();
  } catch (error) {
    console.warn(`Backend API unavailable for ${courseId}, using mock:`, error.message);
    const c = getCourse(courseId);
    if (!c) return null;
    return {
      courseId: c.courseId,
      courseName: c.courseName,
      recommendedYear: c.recommendedYear,
      teacher: c.teacher,
      credits: c.credits,
      description: c.description,
      sections: c.sections || []
    };
  }
}

// ── Mock courses & sections (อิงจาก Database จริง: courses.csv และ sections.csv) ──
const mockCourses = [
  { 
    courseId: "CS102", 
    code: "CS102", 
    courseName: "Problem solving Basics and Computer Programming", 
    name: "Problem solving Basics and Computer Programming", 
    recommendedYear: 1, 
    credits: "3 (2-2-5)",
    teacher: "ผศ.ดร.เสาวลักษณ์ วรรธนาภา",
    description: "",
    sectionCount: 2,
    sections: [
      { sectionId: "cs102_030001", sectionNumber: "030001", instructorName: "ผศ.ดร.เสาวลักษณ์ วรรธนาภา" },
      { sectionId: "cs102_030002", sectionNumber: "030002", instructorName: "ผศ.ดร.ฐาปนา บุญชู" }
    ]
  },
  { 
    courseId: "CS261", 
    code: "CS261", 
    courseName: "Introduction to Software Engineering", 
    name: "Introduction to Software Engineering", 
    recommendedYear: 2, 
    credits: "3 (3-0-6)",
    teacher: "ผศ.ดร.ทรงศักดิ์ รองวิริยะพานิช",
    description: "ระเบียบวิธีทางวิศวกรรมซอฟต์แวร์ การจัดการโครงการ และการประกันคุณภาพซอฟต์แวร์",
    sectionCount: 1,
    sections: [
      { sectionId: "cs261_100001", sectionNumber: "100001", instructorName: "ผศ.ดร.ทรงศักดิ์ รองวิริยะพานิช" }
    ]
  },
  { 
    courseId: "CS213", 
    code: "CS213", 
    courseName: "Data Structures", 
    name: "Data Structures", 
    recommendedYear: 2, 
    credits: "3 (2-2-5)",
    teacher: "ผศ.ดร.วนิดา พฤทธิวิทยา",
    description: "โครงสร้างข้อมูลขั้นสูงและการออกแบบโครงสร้างข้อมูลที่มีประสิทธิภาพ",
    sectionCount: 2,
    sections: [
      { sectionId: "cs213_050001", sectionNumber: "050001", instructorName: "ผศ.ดร.วนิดา พฤทธิวิทยา" },
      { sectionId: "cs213_050002", sectionNumber: "050002", instructorName: "ผศ.ดร.ฐาปนา บุญชู" }
    ]
  },
  { 
    courseId: "CS251", 
    code: "CS251", 
    courseName: "Database Systems 1", 
    name: "Database Systems 1", 
    recommendedYear: 2, 
    credits: "3 (3-0-6)",
    teacher: "ผศ.ดร.วิลาวรรณ รักผกาวงศ์",
    description: "ระบบจัดการฐานข้อมูล สถาปัตยกรรม แบบจำลองข้อมูลเชิงสัมพันธ์ และภาษา SQL",
    sectionCount: 2,
    sections: [
      { sectionId: "cs251_090001", sectionNumber: "090001", instructorName: "ผศ.ดร.วิลาวรรณ รักผกาวงศ์" },
      { sectionId: "cs251_090002", sectionNumber: "090002", instructorName: "ผศ.ดร.เสาวลักษณ์ วรรธนาภา" }
    ]
  },
  { 
    courseId: "TU100", 
    code: "TU100", 
    courseName: "Civic Engagement", 
    name: "Civic Engagement", 
    recommendedYear: 1, 
    credits: "3 (3-0-6)",
    teacher: "รศ.ดร.ปริญญา เทวานฤมิตรกุล",
    description: "พลเมืองกับความรับผิดชอบต่อสังคม การมีส่วนร่วมเพื่อการพัฒนาชุมชนอย่างยั่งยืน",
    sectionCount: 2,
    sections: [
      { sectionId: "tu100_820001", sectionNumber: "820001", instructorName: "รศ.ดร.ปริญญา เทวานฤมิตรกุล" },
      { sectionId: "tu100_830001", sectionNumber: "830001", instructorName: "ผศ.ดร.ปาริยา ณ นคร" }
    ]
  },
  { 
    courseId: "TU103", 
    code: "TU103", 
    courseName: "Life and Sustainability", 
    name: "Life and Sustainability", 
    recommendedYear: 1, 
    credits: "3 (3-0-6)",
    teacher: "รศ.โรจน์ คุณเอนก",
    description: "ชีวิตและความยั่งยืน สิ่งแวดล้อม และการดำรงชีวิตในยุคการเปลี่ยนแปลงสภาพภูมิอากาศ",
    sectionCount: 3,
    sections: [
      { sectionId: "tu103_230001", sectionNumber: "230001", instructorName: "รศ.โรจน์ คุณเอนก, ผศ.ชล บุนนาค" },
      { sectionId: "tu103_320001", sectionNumber: "320001", instructorName: "รศ.โรจน์ คุณเอนก, ผศ.ชล บุนนาค" },
      { sectionId: "tu103_000002", sectionNumber: "000002", instructorName: "รศ.โรจน์ คุณเอนก" }
    ]
  },
  { 
    courseId: "CS372", 
    code: "CS372", 
    courseName: "Machine Learning", 
    name: "Machine Learning", 
    recommendedYear: 3, 
    credits: "3 (3-0-6)",
    teacher: "รศ.ดร.ธนาธร ทะนานทอง",
    description: "การเรียนรู้ของเครื่อง การเรียนรู้แบบมีผู้สอน ไม่มีผู้สอน และโครงข่ายประสาทเทียม",
    sectionCount: 1,
    sections: [
      { sectionId: "cs372_110001", sectionNumber: "110001", instructorName: "รศ.ดร.ธนาธร ทะนานทอง" }
    ]
  },
  { 
    courseId: "EL105", 
    code: "EL105", 
    courseName: "English Communication Skills", 
    name: "English Communication Skills", 
    recommendedYear: 1, 
    credits: "3 (3-0-6)",
    teacher: "ไม่ระบุผู้สอน",
    description: "ทักษะการสื่อสารภาษาอังกฤษเพื่อการทำงานและการนำเสนอทางวิชาการ",
    sectionCount: 1,
    sections: [
      { sectionId: "el105_000000", sectionNumber: "000000", instructorName: "ไม่ระบุผู้สอน" }
    ]
  }
];

function getCourse(code) {
  if (!code) return null;
  return mockCourses.find(c => 
    c.courseId.toLowerCase() === code.toLowerCase() || 
    (c.code && c.code.toLowerCase() === code.toLowerCase())
  ) || null;
}

// ── Mock reviews ────────────────────────────
const mockReviews = {
  "CS261": [
    { id: 1, anonymous: true, studentId: "6600000000", year: 4, date: "10/10/2569", tags: ["เน้นทฤษฎี", "เน้นทฤษฎี"], text: "อาจารย์สอนดีมาก สอนเข้าใจ ไม่เร็วเกินไป", workload: "ปานกลาง", style: "ทฤษฎี", detail: "ละเอียด" },
    { id: 2, anonymous: true, studentId: "6700000000", year: 3, date: "10/10/2569", tags: ["เน้นทฤษฎี", "เน้นทฤษฎี"], text: "อาจารย์สอนดีมาก สอนเข้าใจ ไม่เร็วเกินไป", workload: "น้อย", style: "ปฏิบัติ", detail: "กลาง" },
    { id: 3, anonymous: true, studentId: "6800000000", year: 2, date: "10/10/2569", tags: ["เน้นทฤษฎี", "เน้นทฤษฎี"], text: "อาจารย์สอนดีมาก สอนเข้าใจ ไม่เร็วเกินไป", workload: "มาก", style: "ทฤษฎี", detail: "กลาง" },
  ],
  "CS262": [
    { id: 1, anonymous: true, studentId: "6800000000", year: 2, date: "10/10/2569", tags: ["เน้นปฏิบัติ"], text: "งาน Lab เยอะ แต่ได้ความรู้จริง", workload: "มาก", style: "ปฏิบัติ", detail: "ละเอียด" },
  ],
};

// ── Helpers ─────────────────────────────────

function getReviews(code) {
  return mockReviews[code] || [];
}

function navigate(page, params = {}) {
  const qs = new URLSearchParams(params).toString();
  window.location.href = qs ? `${page}?${qs}` : page;
}

function getParam(key) {
  return new URLSearchParams(window.location.search).get(key);
}

async function apiRequest(path, options = {}) {
  const headers = new Headers(options.headers || {});
  if (options.body !== undefined) {
    headers.set("Content-Type", "application/json");
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
    credentials: "include",
  });

  const contentType = response.headers.get("content-type") || "";
  let payload = null;
  if (response.status !== 204) {
    payload = contentType.includes("application/json") || contentType.includes("+json")
      ? await response.json()
      : await response.text();
  }

  if (!response.ok) {
    const message = payload && typeof payload === "object" && (payload.message || payload.detail || payload.error)
      ? payload.message || payload.detail || payload.error
      : `Backend ตอบกลับด้วยสถานะ ${response.status}`;
    const error = new Error(message);
    error.status = response.status;
    throw error;
  }

  return payload;
}

// ── Auth ────────────────────────────────────
function requireAuth() {
  if (!sessionStorage.getItem(AUTH_STATE_KEY)) {
    const isPages = window.location.pathname.includes("/pages/");
    window.location.replace(isPages ? "index.html" : "pages/index.html");
    return false;
  }
  return true;
}

async function logout() {
  let logoutError = false;
  if (sessionStorage.getItem(AUTH_STATE_KEY)) {
    try {
      await apiRequest("/auth/logout", { method: "POST" });
    } catch (error) {
      console.error("Backend logout failed:", error);
      logoutError = true;
    }
  }

  sessionStorage.removeItem(AUTH_STATE_KEY);
  const isPages = window.location.pathname.includes("/pages/");
  const loginUrl = isPages ? "index.html" : "pages/index.html";
  window.location.replace(logoutError ? `${loginUrl}?reason=logout-failed` : loginUrl);
}

// ── Render top bar (Navbar กลาง) ─────────────
function renderTopbar() {
  const topbar = document.getElementById("topbar") || document.querySelector(".topbar");
  if (!topbar) return;

  const isPages = window.location.pathname.includes("/pages/") || !window.location.pathname.endsWith("/NEXUS/");
  const srcPrefix = isPages ? "../src/" : "src/";
  const homeUrl = isPages ? "courses.html" : "pages/courses.html";

  topbar.className = "topbar";
  topbar.innerHTML = `
    <div class="topbar-left">
      <a href="${homeUrl}" class="topbar-logo-link" title="CSTU NEXUS">
        <img src="${srcPrefix}full-logo.png" alt="CSTU NEXUS" class="topbar-logo-img" />
      </a>
    </div>
    <div class="topbar-right">
      <button class="btn-logout" onclick="logout()" title="ออกจากระบบ">
        <img src="${srcPrefix}logout-icon.png" alt="" class="logout-icon-img" />
        <span>Log out</span>
      </button>
    </div>
  `;
}


// Auto-render default topbar on DOMContentLoaded if not already rendered
document.addEventListener("DOMContentLoaded", () => {
  const topbar = document.getElementById("topbar");
  if (topbar && !topbar.hasAttribute("data-rendered")) {
    topbar.setAttribute("data-rendered", "true");
    renderTopbar();
  }
});
