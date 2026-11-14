/* ==========================================
   CSTU NEXUS - Shared JS + API integration
   ========================================== */

const isLocalFrontend = ["localhost", "127.0.0.1"].includes(window.location.hostname);
const isLocalDevServer = isLocalFrontend && ["5500", "5501"].includes(window.location.port);
const API_BASE_URL = (
  window.CSTU_API_BASE_URL
  || (isLocalDevServer
    ? `http://${window.location.hostname}:8080/api`
    : `${window.location.origin}/api`)
).replace(/\/+$/, "");
const AUTH_STATE_KEY = "cstu_authenticated";
const mockCurrentUser = {
  name: "มะละกอ ส้มตำ",
  studentId: "6809670670",
  year: 2,
  faculty: "คณะวิทยาศาสตร์และเทคโนโลยี สาขาวิทยาการคอมพิวเตอร์"
};

/* ค้นหารายวิชาจาก Backend (รองรับ keyword, year, years, category, categories และ fallback เป็น mockCourses) */
async function apiFetchCourses(keywordOrOptions = "", maybeYear = null, maybeCategory = "") {
  let keyword = "";
  let yearList = [];
  let catList = [];

  if (typeof keywordOrOptions === "object" && keywordOrOptions !== null) {
    keyword = keywordOrOptions.keyword || "";

    if (keywordOrOptions.years && Array.isArray(keywordOrOptions.years)) {
      yearList = keywordOrOptions.years.map(String).filter(Boolean);
    } else if (keywordOrOptions.years) {
      yearList = String(keywordOrOptions.years).split(",").map(s => s.trim()).filter(Boolean);
    } else if (keywordOrOptions.year !== undefined && keywordOrOptions.year !== null && String(keywordOrOptions.year).trim() !== "") {
      yearList = [String(keywordOrOptions.year).trim()];
    }

    if (keywordOrOptions.categories && Array.isArray(keywordOrOptions.categories)) {
      catList = keywordOrOptions.categories.map(c => String(c).trim().toUpperCase()).filter(Boolean);
    } else if (keywordOrOptions.categories) {
      catList = String(keywordOrOptions.categories).split(",").map(s => s.trim().toUpperCase()).filter(Boolean);
    } else if (keywordOrOptions.category) {
      catList = [String(keywordOrOptions.category).trim().toUpperCase()];
    }
  } else {
    keyword = keywordOrOptions || "";
    if (maybeYear !== undefined && maybeYear !== null && String(maybeYear).trim() !== "") {
      yearList = [String(maybeYear).trim()];
    }
    if (maybeCategory) {
      catList = [String(maybeCategory).trim().toUpperCase()];
    }
  }

  const trimmed = keyword.trim();
  const params = new URLSearchParams();
  if (trimmed) params.append("keyword", trimmed);
  if (yearList.length === 1) {
    params.append("year", yearList[0]);
  } else if (yearList.length > 1) {
    params.append("years", yearList.join(","));
  }
  if (catList.length === 1) {
    params.append("category", catList[0]);
  } else if (catList.length > 1) {
    params.append("categories", catList.join(","));
  }

  const queryString = params.toString();
  const url = queryString ? `${API_BASE_URL}/courses?${queryString}` : `${API_BASE_URL}/courses`;

  const filterList = (list) => {
    let results = Array.isArray(list) ? list : [];
    if (trimmed) {
      const q = trimmed.toLowerCase();
      results = results.filter(c => {
        const id = (c.courseId || c.code || "").toLowerCase();
        const name = (c.courseName || c.name || "").toLowerCase();
        return id.includes(q) || name.includes(q);
      });
    }
    if (yearList.length > 0) {
      results = results.filter(c => yearList.includes(String(c.recommendedYear)));
    }
    if (catList.length > 0) {
      results = results.filter(c => {
        const id = (c.courseId || c.code || "").toUpperCase();
        return catList.some(cat => {
          if (cat === "OTHER") {
            return !/^(CS|TU|EL|ST)/i.test(id);
          }
          return id.startsWith(cat);
        });
      });
    }
    return results;
  };

  try {
    const response = await fetch(url);
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    const data = await response.json();
    return filterList(data);
  } catch (error) {
    return filterList(mockCourses);
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
  },
  { 
    courseId: "CS299", 
    code: "CS299", 
    courseName: "Special Topics in Computer Science", 
    name: "Special Topics in Computer Science", 
    recommendedYear: 3, 
    credits: "3 (3-0-6)",
    teacher: "คณาจารย์สาขาวิทยาการคอมพิวเตอร์",
    description: "หัวข้อคัดสรรทางวิทยาการคอมพิวเตอร์ (ไม่มี Section - รีวิวรวมรายวิชา)",
    sectionCount: 0,
    sections: []
  },
  { 
    courseId: "MA211", 
    code: "MA211", 
    courseName: "Calculus 1", 
    name: "Calculus 1", 
    recommendedYear: 1, 
    credits: "3 (3-0-6)",
    teacher: "รศ.ดร.ญาณิศา ชัยยา",
    description: "แคลคูลัส 1 อนุพันธ์ ปริพันธ์ และการประยุกต์",
    sectionCount: 2,
    sections: [
      { sectionId: "ma211_020001", sectionNumber: "020001", instructorName: "รศ.ดร.ญาณิศา ชัยยา" }
    ]
  },
  { 
    courseId: "ST216", 
    code: "ST216", 
    courseName: "Statistics for Social Science 1", 
    name: "Statistics for Social Science 1", 
    recommendedYear: 1, 
    credits: "3 (3-0-6)",
    teacher: "ผศ.ดร.ปารวัณ ไพจิตรรัตนะ",
    description: "สถิติสำหรับสังคมศาสตร์ ความน่าจะเป็น และการทดสอบสมมติฐาน",
    sectionCount: 2,
    sections: [
      { sectionId: "st216_040001", sectionNumber: "040001", instructorName: "ผศ.ดร.ปารวัณ ไพจิตรรัตนะ" }
    ]
  },
  { 
    courseId: "LAS101", 
    code: "LAS101", 
    courseName: "Critical Thinking, Reading, and Writing", 
    name: "Critical Thinking, Reading, and Writing", 
    recommendedYear: 1, 
    credits: "3 (3-0-6)",
    teacher: "ผศ.ดร.อำนาจ ปักษาสุข",
    description: "การคิดเชิงวิพากษ์ การอ่านและการเขียนเชิงวิชาการ",
    sectionCount: 2,
    sections: [
      { sectionId: "las101_450001", sectionNumber: "450001", instructorName: "ผศ.ดร.อำนาจ ปักษาสุข" }
    ]
  },
  { 
    courseId: "CS401", 
    code: "CS401", 
    courseName: "Special Projects 2 (Senior Project)", 
    name: "Special Projects 2 (Senior Project)", 
    recommendedYear: 4, 
    credits: "3 (0-6-3)",
    teacher: "คณาจารย์สาขาวิทยาการคอมพิวเตอร์",
    description: "โครงงานวิจัยและพัฒนาทางวิทยาการคอมพิวเตอร์สำหรับนักศึกษาชั้นปีที่ 4",
    sectionCount: 1,
    sections: [
      { sectionId: "cs401_010001", sectionNumber: "010001", instructorName: "คณาจารย์สาขาวิทยาการคอมพิวเตอร์" }
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
    { id: 1, sectionNumber: "870001", sectionId: "cs261_870001", studentId: "6600000000", year: 4, date: "9-11-2569", tags: ["เน้นทฤษฎี", "เน้นทฤษฎี"], text: "อาจารย์สอนดีมาก สอนเข้าใจ ไม่เร็วเกินไป", workload: "ปานกลาง", style: "ทฤษฎี", detail: "ละเอียด" },
    { id: 2, sectionNumber: "870001", sectionId: "cs261_870001", studentId: "6600000000", year: 3, date: "9-11-2569", tags: ["เน้นทฤษฎี", "เน้นทฤษฎี"], text: "อาจารย์สอนดีมาก สอนเข้าใจ ไม่เร็วเกินไป", workload: "น้อย", style: "ปฏิบัติ", detail: "กลาง" },
    { id: 3, sectionNumber: "870001", sectionId: "cs261_870001", studentId: "6600000000", year: 2, date: "9-11-2569", tags: ["เน้นทฤษฎี", "เน้นทฤษฎี"], text: "อาจารย์สอนดีมาก สอนเข้าใจ ไม่เร็วเกินไป", workload: "มาก", style: "ทฤษฎี", detail: "กลาง" },
    { id: 4, sectionNumber: "870001", sectionId: "cs261_870001", studentId: "6609650001", year: 4, date: "11-11-2569", tags: ["เน้นทฤษฎี", "สมดุล"], text: "สไลด์และเอกสารประกอบการสอนครบถ้วนมาก แนะนำให้เข้าเรียนทุกคาบ", workload: "ปานกลาง", style: "ทฤษฎี", detail: "ละเอียด" },
    { id: 5, sectionNumber: "100001", sectionId: "cs261_100001", studentId: "6709650002", year: 3, date: "12/10/2569", tags: ["เน้นปฏิบัติ"], text: "มีการบ้านท้ายคาบช่วยทบทวน มีประโยชน์กับการสอบมาก", workload: "ปานกลาง", style: "ปฏิบัติ", detail: "กลาง" },
    { id: 6, sectionNumber: "100001", sectionId: "cs261_100001", studentId: "6809650003", year: 2, date: "13/10/2569", tags: ["เน้นทฤษฎี"], text: "ข้อสอบตรงกับสิ่งที่สอนในห้อง ถ้าตั้งใจเรียนเก็บเกรดสวยแน่นอน", workload: "น้อย", style: "ทฤษฎี", detail: "ละเอียด" },
    { id: 7, sectionNumber: "100001", sectionId: "cs261_100001", studentId: "6609650004", year: 4, date: "14/10/2569", tags: ["เน้นปฏิบัติ"], text: "เป็นวิชาพื้นฐานที่ดีมาก ช่วยต่อยอดวิชาขั้นสูงได้ดี", workload: "ปานกลาง", style: "ปฏิบัติ", detail: "ละเอียด" },
  ],
  "CS262": [
    { id: 1, anonymous: true, year: 2, date: "10/10/2569", tags: ["เน้นปฏิบัติ"], text: "งาน Lab เยอะ แต่ได้ความรู้จริง", workload: "มาก", style: "ปฏิบัติ", detail: "ละเอียด" },
  ],
};

// ── Helpers ─────────────────────────────────

function getReviews(code, section = "") {
  const list = mockReviews[code] || [];
  if (!section) return list;
  const filtered = list.filter(r => {
    const sec = r.sectionNumber || r.sectionId;
    return !sec || String(sec).includes(String(section).trim());
  });
  return filtered.length > 0 ? filtered : list;
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

  let response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers,
      credentials: "include",
    });
  } catch (error) {
    if (error instanceof TypeError && path !== "/auth/login" && path !== "/auth/logout") {
      sessionStorage.removeItem(AUTH_STATE_KEY);
      const isPages = window.location.pathname.includes("/pages/");
      const loginUrl = isPages ? "index.html" : "pages/index.html";
      window.location.replace(`${loginUrl}?reason=backend-disconnected`);
    }
    throw error;
  }

  if (response.status === 401 && path !== "/auth/login" && path !== "/auth/logout") {
    sessionStorage.removeItem(AUTH_STATE_KEY);
    const isPages = window.location.pathname.includes("/pages/");
    const loginUrl = isPages ? "index.html" : "pages/index.html";
    window.location.replace(`${loginUrl}?reason=session-expired`);
  }

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
async function requireAuth() {
  const protectedContent = document.querySelector(".app-shell");
  if (protectedContent) protectedContent.hidden = true;

  try {
    const user = await apiRequest("/auth/me");
    sessionStorage.setItem(AUTH_STATE_KEY, "1");
    if (protectedContent) protectedContent.hidden = false;
    return user;
  } catch (error) {
    if (error.status !== 401) {
      console.error("Session verification failed:", error);
      if (!(error instanceof TypeError)) {
        window.alert(error.message || "ตรวจสอบสถานะการเข้าสู่ระบบไม่สำเร็จ");
      }
    }
    return null;
  }
}

window.addEventListener("pageshow", event => {
  const isLoginPage = window.location.pathname.endsWith("/pages/index.html");
  if (event.persisted && !isLoginPage && !sessionStorage.getItem(AUTH_STATE_KEY)) {
    window.location.replace("index.html");
  }
});

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

const ITEMS_PER_PAGE = 5;

// Auto-render default topbar on DOMContentLoaded if not already rendered
document.addEventListener('DOMContentLoaded', () => {
  const reviewContainer = document.querySelector('.reviews-scroll-container');
  const prevBtn = document.querySelector('.review-prev-btn');
  const nextBtn = document.querySelector('.review-next-btn');

  if (reviewContainer && prevBtn && nextBtn) {
    const scrollAmount = 300;

    const updateButtonStates = () => {
      const maxScrollLeft = reviewContainer.scrollWidth - reviewContainer.clientWidth;
      prevBtn.disabled = reviewContainer.scrollLeft <= 0;
      nextBtn.disabled = reviewContainer.scrollLeft >= maxScrollLeft - 5;
    };

    nextBtn.addEventListener('click', () => {
      reviewContainer.scrollBy({ left: scrollAmount, behavior: 'smooth' });
    });

    prevBtn.addEventListener('click', () => {
      reviewContainer.scrollBy({ left: -scrollAmount, behavior: 'smooth' });
    });

    reviewContainer.addEventListener('scroll', updateButtonStates);
    window.addEventListener('resize', updateButtonStates);
    
    updateButtonStates();
  }
});
