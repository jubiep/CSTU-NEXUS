/* ==========================================
   CSTU NEXUS - Shared JS + API integration
   ========================================== */

const isLocalLiveServer = ["localhost", "127.0.0.1"].includes(window.location.hostname)
  && window.location.port === "5500";
const API_BASE_URL = (
  window.CSTU_API_BASE_URL
  || (isLocalLiveServer ? "http://localhost:8080/api" : `${window.location.origin}/api`)
).replace(/\/+$/, "");
const AUTH_STATE_KEY = "cstu_authenticated";

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
    const message = payload && typeof payload === "object" && (payload.message || payload.detail)
      ? payload.message || payload.detail
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
