/* ==========================================
   CSTU NEXUS - Shared JS + Mock Data
   (ยังไม่มี backend — ใช้ mockData แทน)
   ========================================== */

// ── Mock current user ──────────────────────
const currentUser = {
  name: "มะละกอ สันตำ",
  studentId: "6809670670",
  year: 2,
  faculty: "คณะวิทยาศาสตร์และเทคโนโลยี สาขาวิทยาการคอมพิวเตอร์",
  verified: true,
  avatar: null,   // null = ใช้ emoji fallback
};

// ── Mock courses ────────────────────────────
const mockCourses = [
  { code: "CS261", name: "Introduction to Software Engineering", teacher: "ดร.กรงศักดิ์ รองวิชาหมัน", sections: ["001","002","003"] },
  { code: "CS262", name: "Data Structures", teacher: "ผศ.ดร.อลิษา วงศ์ทอง", sections: ["001","002"] },
  { code: "CS270", name: "Database Systems", teacher: "ดร.ชัยวัฒน์ พรมพิทักษ์", sections: ["001"] },
  { code: "CS301", name: "Algorithm Design", teacher: "ศ.ดร.วิไลวรรณ สุขใจ", sections: ["001","002","003"] },
  { code: "CS310", name: "Computer Networks", teacher: "ดร.ประสิทธิ์ มีชัย", sections: ["001","002"] },
  { code: "CS320", name: "Operating Systems", teacher: "ผศ.ณัฐพล เจริญสุข", sections: ["001"] },
  { code: "CS340", name: "Web Development", teacher: "ดร.พิมพ์ใจ ศรีนวล", sections: ["001","002"] },
  { code: "MA201", name: "Calculus II", teacher: "ผศ.ดร.สุชาดา วัฒนาสุข", sections: ["001","002","003","004"] },
];

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
function getCourse(code) {
  return mockCourses.find(c => c.code === code) || null;
}

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

// ── Auth guard (mock) ────────────────────────
function requireAuth() {
  const loggedIn = sessionStorage.getItem("cstu_logged_in");
  if (!loggedIn) {
    const isPages = window.location.pathname.includes("/pages/");
    window.location.href = isPages ? "index.html" : "pages/index.html";
  }
}

function logout() {
  sessionStorage.removeItem("cstu_logged_in");
  const isPages = window.location.pathname.includes("/pages/");
  window.location.href = isPages ? "index.html" : "pages/index.html";
}

// ── Render bottom nav ────────────────────────
function renderNav(active) {
  const nav = document.getElementById("bottom-nav");
  if (!nav) return;
  const items = [
    { id: "home",    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9.5L12 3l9 6.5V20a1 1 0 01-1 1H4a1 1 0 01-1-1V9.5z"/><path d="M9 21V12h6v9"/></svg>`, label: "หน้าหลัก", href: "dashboard.html" },
    { id: "search",  icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>`, label: "ค้นหา", href: "courses.html" },
    { id: "profile", icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="8" r="4"/><path d="M4 20c0-4 3.6-7 8-7s8 3 8 7"/></svg>`, label: "โปรไฟล์", href: "profile.html" },
  ];
  nav.innerHTML = items.map(it => `
    <a class="nav-item ${it.id === active ? 'active' : ''}" href="${it.href}">
      ${it.icon}
      <span>${it.label}</span>
    </a>
  `).join('');
}
