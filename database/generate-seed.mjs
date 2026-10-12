
import fs from "node:fs";

function parseCSV(text) {
  const rows = [];
  let row = [];
  let field = "";
  let quoted = false;

  for (let i = 0; i < text.length; i++) {
    const c = text[i];

    if (quoted) {
      if (c === '"' && text[i + 1] === '"') {
        field += '"';
        i++;
      } else if (c === '"') {
        quoted = false;
      } else {
        field += c;
      }
    } else if (c === '"' && field === "") {
      quoted = true;
    } else if (c === ",") {
      row.push(field);
      field = "";
    } else if (c === "\n" || c === "\r") {
      if (c === "\r" && text[i + 1] === "\n") i++;
      row.push(field);

      if (row.some(value => value !== "")) {
        rows.push(row);
      }

      row = [];
      field = "";
    } else {
      field += c;
    }
  }

  if (quoted) {
    throw new Error("CSV มี double quote ที่ปิดไม่ครบ");
  }

  if (field !== "" || row.length > 0) {
    row.push(field);
    if (row.some(value => value !== "")) rows.push(row);
  }

  const headers = rows.shift();

  if (!headers) {
    throw new Error("CSV ไม่มี header");
  }

  return rows.map((values, index) => {
    if (values.length !== headers.length) {
      throw new Error(`จำนวนคอลัมน์ไม่ตรงกันใน record ${index + 2}`);
    }

    return Object.fromEntries(
      headers.map((header, i) => [header, values[i] ?? ""])
    );
  });
}

function readCSV(path) {
  const text = fs.readFileSync(path, "utf8").replace(/^\uFEFF/, "");
  return parseCSV(text);
}

function checkUnique(rows, key) {
  const values = rows.map(row => row[key]);

  if (values.some(value => !value)) {
    throw new Error(`${key} มีค่าว่าง`);
  }

  if (new Set(values).size !== values.length) {
    throw new Error(`${key} มีค่าซ้ำ`);
  }
}

function sqlString(value) {
  return "E'" + value
    .replace(/\\/g, "\\\\")
    .replace(/'/g, "''")
    .replace(/\r/g, "\\r")
    .replace(/\n/g, "\\n")
    .replace(/\t/g, "\\t") + "'";
}

const courses = readCSV("database/courses.csv");
const allSections = readCSV("database/sections.csv");

const courseHeaders = [
  "course_id", "course_name", "recommended_year"
];

const sectionHeaders = [
  "course_id", "section_id", "section_number", "instructor_name"
];

for (const header of courseHeaders) {
  if (!courses.length || !(header in courses[0])) {
    throw new Error(`courses.csv ไม่มีคอลัมน์ ${header}`);
  }
}

for (const header of sectionHeaders) {
  if (!allSections.length || !(header in allSections[0])) {
    throw new Error(`sections.csv ไม่มีคอลัมน์ ${header}`);
  }
}

checkUnique(courses, "course_id");
checkUnique(allSections, "section_id");

for (const course of courses) {
  const year = Number(course.recommended_year);

  if (
    !course.course_name.trim() ||
    !Number.isInteger(year) ||
    year < 1 ||
    year > 4
  ) {
    throw new Error(`ข้อมูลวิชาไม่ถูกต้อง: ${course.course_id}`);
  }
}

const courseIds = new Set(courses.map(course => course.course_id));

const missingCourses = [
  ...new Set(
    allSections
      .filter(section => !courseIds.has(section.course_id))
      .map(section => section.course_id)
  )
];

if (missingCourses.length > 0) {
  throw new Error(`ไม่พบ course_id: ${missingCourses.join(", ")}`);
}

const placeholders = allSections.filter(
  section => section.section_number === "000000"
);

const expectedPlaceholderIds = [
  "el105_000000",
  "el295_000000",
  "el395_000000"
];

if (
  placeholders.length !== 3 ||
  expectedPlaceholderIds.some(id =>
    !placeholders.some(section => section.section_id === id)
  )
) {
  throw new Error("Placeholder ไม่ตรงกับ 3 รายการที่ยืนยันไว้");
}

const sections = allSections.filter(
  section => section.section_number !== "000000"
);

for (const section of sections) {
  if (
    !section.section_number ||
    section.section_number.length > 10
  ) {
    throw new Error(`section_number ไม่ถูกต้อง: ${section.section_id}`);
  }
}

if (courses.length !== 38 || sections.length !== 63) {
  throw new Error(
    `จำนวนข้อมูลไม่ตรงที่คาด: courses=${courses.length}, sections=${sections.length}`
  );
}

const courseValues = courses.map(course =>
  `(${sqlString(course.course_id)}, ${sqlString(course.course_name)}, ${Number(course.recommended_year)})`
);

const sectionValues = sections.map(section => {
  const instructor = section.instructor_name.trim()
    ? sqlString(section.instructor_name)
    : "NULL";

  return `(${sqlString(section.course_id)}, ${sqlString(section.section_id)}, ${sqlString(section.section_number)}, ${instructor})`;
});

const sql = `-- Generated from courses.csv and sections.csv
-- Placeholder sections with section_number 000000 are excluded.

BEGIN;

INSERT INTO courses (course_id, course_name, recommended_year)
VALUES
${courseValues.join(",\n")};

INSERT INTO sections (course_id, section_id, section_number, instructor_name)
VALUES
${sectionValues.join(",\n")};

COMMIT;
`;

fs.writeFileSync("database/seed.sql", sql, "utf8");

console.log("Seed SQL generated successfully.");
console.log("Courses:", courses.length);
console.log("Sections to import:", sections.length);
console.log("Placeholders excluded:", placeholders.length);
console.log("Missing course IDs:", missingCourses.length);
console.log("Output: database/seed.sql");
