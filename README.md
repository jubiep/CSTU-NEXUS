# CSTU-NEXUS-Backend

Backend และ Database สำหรับโปรเจกต์ **CSTU NEXUS — Course Decision Hub** ซึ่งเป็นระบบสนับสนุนการตัดสินใจเลือกเรียนรายวิชาของนักศึกษา

Repository นี้ประกอบด้วย Spring Boot Backend และ PostgreSQL Database ที่ทำงานผ่าน Docker เพื่อให้สมาชิกในทีมสามารถตั้งค่าสภาพแวดล้อมและพัฒนาโปรเจกต์ร่วมกันได้

เอกสารนี้เขียนขึ้นเพื่อช่วยให้สมาชิกที่เพิ่งเริ่มพัฒนา Backend สามารถตั้งค่าระบบและทดลองรันโปรเจกต์ในเครื่องของตนเองได้ทีละขั้นตอน

---

## สารบัญ

1. [สิ่งที่ต้องติดตั้งก่อนเริ่มต้น](#1-สิ่งที่ต้องติดตั้งก่อนเริ่มต้น)
2. [รู้จักโครงสร้างโปรเจกต์](#2-รู้จักโครงสร้างโปรเจกต์)
3. [ตั้งค่าและเริ่มต้น Database](#3-ตั้งค่าและเริ่มต้น-database)
4. [ทำความเข้าใจ Schema และ Seed Data](#4-ทำความเข้าใจ-schema-และ-seed-data)
5. [รัน Spring Boot Backend](#5-รัน-spring-boot-backend)
6. [ทดสอบ Backend และ Database](#6-ทดสอบ-backend-และ-database)
7. [คำสั่งที่ใช้บ่อย](#7-คำสั่งที่ใช้บ่อย)
8. [การแก้ไขปัญหาเบื้องต้น](#8-การแก้ไขปัญหาเบื้องต้น)
9. [ข้อควรระวังเกี่ยวกับ Database](#9-ข้อควรระวังเกี่ยวกับ-database)

---

## 1. สิ่งที่ต้องติดตั้งก่อนเริ่มต้น

ก่อนเริ่มต้น ให้ตรวจสอบว่ามีโปรแกรมต่อไปนี้แล้ว

| โปรแกรม | ใช้ทำอะไร |
|---|---|
| Git | ดาวน์โหลด Source Code และจัดการ Version ของโปรเจกต์ |
| Docker Desktop | รัน PostgreSQL ใน Container |
| Java JDK 17 | ใช้รันและพัฒนา Spring Boot Backend |
| Visual Studio Code หรือ IDE อื่น | ใช้เปิดและแก้ไข Source Code |

ลิงก์สำหรับติดตั้ง:

- [Git](https://git-scm.com/downloads)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [Eclipse Temurin JDK 17](https://adoptium.net/temurin/releases/?version=17)
- [Visual Studio Code](https://code.visualstudio.com/)

**หมายเหตุ:** โปรเจกต์มี Maven Wrapper (`mvnw`) อยู่แล้ว จึงไม่จำเป็นต้องติดตั้ง Maven แยก

### 1.1 ตรวจสอบโปรแกรม

เปิด Terminal หรือ Git Bash แล้วรัน:

```bash
git --version
java -version
docker --version
docker compose version
```

ตรวจสอบว่า Java เป็นเวอร์ชัน 17 และ Docker สามารถใช้งานได้

หากติดตั้ง Java 17 แล้ว แต่ Terminal ยังแสดง Java เวอร์ชันอื่น ให้ตรวจสอบ `JAVA_HOME` และ `PATH` ให้ชี้ไปยัง JDK 17 ก่อนดำเนินการต่อ

### 1.2 ดาวน์โหลด Repository

หากยังไม่มี Source Code ในเครื่อง ให้เปิด Git Bash แล้วไปยังโฟลเดอร์ที่ต้องการเก็บโปรเจกต์ จากนั้นรัน:

```bash
git clone https://github.com/jubiep/CSTU-NEXUS-Backend.git
cd CSTU-NEXUS-Backend
```

หากมี Repository อยู่ในเครื่องแล้ว สามารถเปิดโฟลเดอร์เดิมได้เลย ไม่จำเป็นต้อง Clone ซ้ำ

---

## 2. รู้จักโครงสร้างโปรเจกต์

โปรเจกต์แบ่งออกเป็น 2 ส่วนหลัก ได้แก่ Database และ Spring Boot Backend

### 2.1 ไฟล์สำคัญในโฟลเดอร์หลัก

| ไฟล์หรือโฟลเดอร์ | หน้าที่ |
|---|---|
| `README.md` | คู่มือการตั้งค่าและใช้งานโปรเจกต์ |
| `database/` | ไฟล์สำหรับตั้งค่า PostgreSQL, Schema และ Seed Data |
| `cstu-course-nexus/` | Source Code ของ Spring Boot Backend |
| `.gitignore` | กำหนดไฟล์ที่ Git ไม่ควรติดตาม |

### 2.2 ไฟล์สำคัญใน `database/`

| ไฟล์ | หน้าที่ |
|---|---|
| `compose.yaml` | กำหนด PostgreSQL Container, Port, Database, User และ Persistent Volume |
| `init.sql` | กำหนดโครงสร้างตารางที่ใช้ตอน Initialize Database ใหม่ |
| `courses.csv` | ข้อมูลรายวิชาต้นทาง |
| `sections.csv` | ข้อมูล Section ต้นทาง |
| `generate-seed.ps1` | สคริปต์ PowerShell สำหรับตรวจสอบข้อมูลและสร้าง SQL จากไฟล์ CSV |
| `seed.sql` | SQL สำหรับนำเข้าข้อมูล Courses และ Sections |
| `migrations/` | เก็บ SQL สำหรับการเปลี่ยนแปลง Schema ในแต่ละช่วงของการพัฒนา |

ไฟล์เหล่านี้มีหน้าที่ต่างกัน ไม่ควรนำมาใช้แทนกัน

ตัวอย่างเช่น การแก้ `init.sql` ไม่ได้หมายความว่า Database ที่สร้างไปแล้วจะเปลี่ยนแปลงตาม และการแก้ CSV ก็ไม่ได้ทำให้ข้อมูลใน Database อัปเดตเอง

### 2.3 ไฟล์สำคัญใน `cstu-course-nexus/`

| ไฟล์หรือโฟลเดอร์ | หน้าที่ |
|---|---|
| `pom.xml` | กำหนด Dependencies และการตั้งค่าการ Build ของ Maven |
| `mvnw` | Maven Wrapper สำหรับเรียก Maven โดยไม่ต้องติดตั้ง Maven แยก |
| `src/main/java/` | เก็บ Java Source Code ของ Backend |
| `src/main/resources/application.properties` | ตั้งค่าการทำงานของ Spring Boot และการเชื่อมต่อ Database |
| `src/test/` | ตำแหน่งสำหรับ Test Code |

ไฟล์สำคัญที่เกี่ยวข้องกับการเชื่อมต่อ Database ได้แก่:

- `pom.xml` มี PostgreSQL JDBC Driver และ Spring JDBC
- `application.properties` กำหนด Database URL, Username และ Password
- `HealthController.java` มี Endpoint สำหรับตรวจสอบการทำงานของแอปและการเชื่อมต่อ Database

ปัจจุบันการทดสอบ Database ใช้ `JdbcTemplate` ผ่าน Spring JDBC ส่วน JPA ยังไม่ได้เปิดใช้งาน

---

## 3. ตั้งค่าและเริ่มต้น Database

ในโปรเจกต์นี้ PostgreSQL 17 ทำงานผ่าน Docker Compose เพื่อให้สมาชิกในทีมใช้ Database Environment ที่สอดคล้องกัน

### ขั้นตอนที่ 1: เปิด Docker Desktop

เปิด Docker Desktop และรอจนกว่า Docker จะพร้อมใช้งาน

### ขั้นตอนที่ 2: ตรวจสอบตำแหน่ง Terminal

เปิด Git Bash ที่โฟลเดอร์หลักของ Repository:

```text
CSTU-NEXUS-Backend/
```

คำสั่งในคู่มือนี้อ้างอิงจากโฟลเดอร์หลัก เว้นแต่จะระบุไว้เป็นอย่างอื่น

### ขั้นตอนที่ 3: เริ่มต้น PostgreSQL

รันคำสั่ง:

```bash
docker compose -f database/compose.yaml up -d
```

คำสั่งนี้จะสร้างหรือเริ่ม Container ตามการตั้งค่าใน `compose.yaml` โดยทำงานอยู่เบื้องหลัง

### ขั้นตอนที่ 4: ตรวจสอบสถานะ Container

```bash
docker compose -f database/compose.yaml ps
```

ตรวจสอบว่า Container `cstu-nexus-postgres` อยู่ในสถานะกำลังทำงาน

สามารถตรวจสอบ Log เพิ่มเติมได้ด้วย:

```bash
docker logs cstu-nexus-postgres
```

### ขั้นตอนที่ 5: เชื่อมต่อ PostgreSQL

```bash
docker exec -it cstu-nexus-postgres psql -U cstu_nexus_user -d cstu_nexus_db
```

เมื่อเชื่อมต่อสำเร็จ จะเข้าสู่ PostgreSQL Command Line ซึ่งเรียกว่า `psql`

ลองแสดงตารางใน Database:

```sql
\dt
```

หากต้องการออกจาก `psql`:

```text
\q
```

เมื่อเริ่มต้น Database สำเร็จแล้ว สามารถดำเนินการขั้นตอนถัดไปได้

---

## 4. ทำความเข้าใจ Schema และ Seed Data

ก่อนรัน Backend ควรเข้าใจความแตกต่างระหว่าง Schema และ Seed Data ก่อน

- **Schema** คือโครงสร้างของ Database เช่น ตาราง คอลัมน์ และข้อกำหนดของข้อมูล
- **Seed Data** คือข้อมูลตั้งต้นที่นำเข้าไปเก็บในตาราง เพื่อใช้ในการพัฒนาและทดสอบระบบ

### 4.1 ตารางใน Database

ไฟล์ `database/init.sql` ใช้กำหนดโครงสร้างตารางของโปรเจกต์ ซึ่งประกอบด้วย:

| ตาราง | หน้าที่โดยสรุป |
|---|---|
| `mock_user` | เก็บข้อมูลผู้ใช้จำลอง |
| `courses` | เก็บข้อมูลรายวิชา |
| `sections` | เก็บข้อมูล Section ของรายวิชา |
| `review` | เก็บข้อมูลรีวิว |

### 4.2 นำเข้า Seed Data

ไฟล์ `database/seed.sql` เตรียมข้อมูลตั้งต้นสำหรับ:

- `courses` จำนวน 38 รายวิชา
- `sections` จำนวน 63 Sections

ข้อมูลต้นทางมาจาก `courses.csv` และ `sections.csv` โดยไม่รวม Placeholder Sections ที่มี `section_number` เป็น `000000`

**สำคัญ:** การเริ่ม PostgreSQL ด้วย Docker Compose ไม่ได้หมายความว่า `seed.sql` จะถูกรันโดยอัตโนมัติ

หากเป็น Database ใหม่ที่สร้างตารางแล้ว แต่ยังไม่มีข้อมูล Courses และ Sections ให้ตรวจสอบก่อนว่าจำเป็นต้องนำเข้า Seed Data หรือไม่

คำสั่งนำเข้าข้อมูล:

```bash
docker exec -i cstu-nexus-postgres psql -U cstu_nexus_user -d cstu_nexus_db -v ON_ERROR_STOP=1 < database/seed.sql
```

หลังนำเข้า ให้ตรวจสอบจำนวนข้อมูล:

```bash
docker exec -it cstu-nexus-postgres psql -U cstu_nexus_user -d cstu_nexus_db
```

จากนั้นรันภายใน `psql`:

```sql
SELECT COUNT(*) FROM courses;
SELECT COUNT(*) FROM sections;
```

ออกจาก `psql` ด้วย `\q`

จำนวนข้อมูลที่คาดหวังสำหรับ Seed Data ชุดปัจจุบันคือ 38 Courses และ 63 Sections

**อย่านำเข้า Seed Data ซ้ำโดยไม่ตรวจสอบก่อน** เพราะ `seed.sql` ไม่ได้ออกแบบมาให้รันซ้ำบน Database ที่มีข้อมูลอยู่แล้ว และอาจเกิด Primary Key ซ้ำได้

### 4.3 เมื่อข้อมูล CSV เปลี่ยน

หากมีการแก้ไข `courses.csv` หรือ `sections.csv` ให้ใช้ Workflow ต่อไปนี้

**ขั้นตอนที่ 1: สร้าง SQL สำหรับทดสอบ**

จากโฟลเดอร์หลักของ Repository:

```bash
powershell.exe -NoProfile -ExecutionPolicy Bypass -File database/generate-seed.ps1 -OutputPath database/seed.generated.test.sql
```

คำสั่งนี้จะสร้างไฟล์ทดสอบแยกต่างหาก โดยไม่เขียนทับ `seed.sql` ตัวจริง

**ขั้นตอนที่ 2: ตรวจสอบผลลัพธ์**

ตรวจสอบว่า Script ทำงานสำเร็จ ไม่มี Error และจำนวนข้อมูลตรงกับ CSV ที่แก้ไข

เปิดไฟล์ `database/seed.generated.test.sql` เพื่อตรวจสอบ SQL ที่สร้างขึ้นก่อนดำเนินการต่อ

**ขั้นตอนที่ 3: สร้าง `seed.sql` ตัวจริง**

เมื่อผลการทดสอบถูกต้องแล้ว ให้รัน:

```bash
powershell.exe -NoProfile -ExecutionPolicy Bypass -File database/generate-seed.ps1
```

คำสั่งนี้จะสร้าง `database/seed.sql` ตาม CSV ปัจจุบัน

**ขั้นตอนที่ 4: ตรวจสอบการเปลี่ยนแปลง**

```bash
git diff --check
git diff -- database/seed.sql
git status
```

ตรวจสอบว่าการเปลี่ยนแปลงใน `seed.sql` สอดคล้องกับข้อมูลที่ตั้งใจแก้ไข

**ข้อควรจำ:** การสร้าง `seed.sql` ใหม่ไม่ได้ทำให้ข้อมูลใน PostgreSQL เปลี่ยนตามโดยอัตโนมัติ การนำข้อมูลชุดใหม่เข้า Database ที่มีข้อมูลอยู่แล้วต้องพิจารณา Primary Key, Foreign Key และข้อมูลเดิมก่อนเสมอ

---

## 5. รัน Spring Boot Backend

เมื่อ PostgreSQL ทำงานแล้ว สามารถเริ่ม Spring Boot Backend ได้

### ขั้นตอนที่ 1: ตรวจสอบ Java

จากโฟลเดอร์หลักของ Repository:

```bash
java -version
```

ตรวจสอบว่าใช้ Java 17

### ขั้นตอนที่ 2: เข้าโฟลเดอร์ Backend

```bash
cd cstu-course-nexus
```

ตรวจสอบว่ามีไฟล์ `pom.xml` และ `mvnw` อยู่ในโฟลเดอร์นี้

### ขั้นตอนที่ 3: รัน Spring Boot

```bash
./mvnw spring-boot:run
```

รอจนแอปเริ่มทำงานสำเร็จ โดยปกติ Spring Boot จะเปิดให้บริการที่ Port `8080`

**อย่าปิด Terminal นี้** ขณะที่ต้องการให้ Backend ทำงานอยู่

หากต้องการรันคำสั่งอื่น ให้เปิด Terminal อีกหน้าต่างหนึ่ง

### ขั้นตอนที่ 4: ทดสอบการ Build และ Test

เปิด Terminal ใหม่ แล้วเข้าโฟลเดอร์ Backend:

```bash
cd cstu-course-nexus
./mvnw test
```

หากคำสั่งสำเร็จ Maven จะแสดง `BUILD SUCCESS`

หาก Test ไม่ผ่าน ให้ตรวจสอบข้อความ Error ก่อนแก้ไข Source Code

---

## 6. ทดสอบ Backend และ Database

หลังจาก Spring Boot เริ่มทำงาน ให้เปิด Terminal อีกหน้าต่างหนึ่ง

### 6.1 ตรวจสอบว่า Backend ทำงานหรือไม่

```bash
curl -i http://localhost:8080/health
```

Endpoint นี้ใช้ตรวจสอบว่าแอปตอบสนองได้หรือไม่

ผลลัพธ์ที่คาดหวังใน Body:

```text
CSTU Course Nexus is running!
```

### 6.2 ตรวจสอบการเชื่อมต่อ Database

```bash
curl -i http://localhost:8080/db-test
```

Endpoint นี้ใช้ `JdbcTemplate` รัน SQL เพื่อนับจำนวนแถวในตาราง `courses`

หาก Database เชื่อมต่อสำเร็จและมี Seed Data ครบตามชุดปัจจุบัน จะได้ข้อความลักษณะนี้:

```text
PostgreSQL connection successful! Courses: 38
```

หากได้ผลลัพธ์ดังกล่าว แสดงว่า Spring Boot สามารถเชื่อมต่อ PostgreSQL และอ่านข้อมูลจากตาราง `courses` ได้

### 6.3 Endpoint ที่มีในปัจจุบัน

| Method | Endpoint | หน้าที่ |
|---|---|---|
| `GET` | `/health` | ตรวจสอบว่า Backend ทำงานอยู่ |
| `POST` | `/echo` | รับข้อความแล้วส่งข้อความตอบกลับ |
| `GET` | `/db-test` | ตรวจสอบการเชื่อมต่อ PostgreSQL และนับ Courses |

Endpoint เหล่านี้เป็นจุดเริ่มต้นสำหรับตรวจสอบระบบ ไม่ได้หมายความว่า Features ทั้งหมดของโปรเจกต์เสร็จสมบูรณ์แล้ว

### 6.4 หยุด Spring Boot

กลับไปที่ Terminal ที่กำลังรัน Spring Boot แล้วกด:

```text
Ctrl + C
```

เมื่อต้องการเริ่มแอปใหม่ ให้รัน `./mvnw spring-boot:run` อีกครั้งจากโฟลเดอร์ `cstu-course-nexus`

---

## 7. คำสั่งที่ใช้บ่อย

### Docker และ PostgreSQL

รันคำสั่งเหล่านี้จากโฟลเดอร์หลักของ Repository

| สิ่งที่ต้องการทำ | คำสั่ง |
|---|---|
| เริ่ม Database | `docker compose -f database/compose.yaml up -d` |
| ตรวจสอบ Container | `docker compose -f database/compose.yaml ps` |
| ดู PostgreSQL Log | `docker logs cstu-nexus-postgres` |
| หยุด Database | `docker compose -f database/compose.yaml stop` |
| เริ่ม Database ที่หยุดไว้ | `docker compose -f database/compose.yaml start` |

### Spring Boot และ Maven

รันคำสั่งเหล่านี้จากโฟลเดอร์ `cstu-course-nexus`

| สิ่งที่ต้องการทำ | คำสั่ง |
|---|---|
| เริ่ม Backend | `./mvnw spring-boot:run` |
| รัน Test | `./mvnw test` |
| ตรวจสอบ Maven และ Java | `./mvnw -version` |

---

## 8. การแก้ไขปัญหาเบื้องต้น

### ปัญหา: Docker ไม่ทำงาน

- ตรวจสอบว่า Docker Desktop เปิดอยู่
- รอจน Docker พร้อมใช้งาน
- ลองตรวจสอบด้วย `docker --version` และ `docker compose version`

### ปัญหา: Port 5432 ถูกใช้งานอยู่

อาจมี PostgreSQL หรือ Container อื่นใช้ Port นี้อยู่ ให้ตรวจสอบ Container และโปรแกรมที่ใช้งาน Port ก่อนเปลี่ยนการตั้งค่า

### ปัญหา: Spring Boot เชื่อมต่อ Database ไม่ได้

ตรวจสอบตามลำดับ:

1. PostgreSQL Container กำลังทำงานหรือไม่
2. Database ชื่อ `cstu_nexus_db` มีอยู่หรือไม่
3. Username และ Password ตรงกับค่าที่กำหนดใน PostgreSQL หรือไม่
4. `spring.datasource.url` ใน `application.properties` ถูกต้องหรือไม่
5. Port `5432` เปิดให้แอปเชื่อมต่อได้หรือไม่

### ปัญหา: ไม่พบตาราง `courses`

- ตรวจสอบว่า Database ถูก Initialize แล้ว
- ตรวจสอบว่ากำลังเชื่อมต่อ Database ที่ถูกต้อง
- ตรวจสอบว่า Schema มีตาราง `courses`
- หากตารางมีอยู่แต่ไม่มีข้อมูล ให้ตรวจสอบ Seed Data แทนการสร้างตารางซ้ำ

### ปัญหา: แก้ `init.sql` แล้ว Database ไม่เปลี่ยนแปลง

PostgreSQL จะรัน Initialization Script เมื่อ Data Directory ยังว่างเท่านั้น การแก้ไฟล์ภายหลังจึงไม่เปลี่ยนตารางที่มีอยู่โดยอัตโนมัติ

ควรใช้วิธีจัดการ Schema Change หรือ Migration ที่ทีมตกลงร่วมกัน ไม่ควรลบ Database เพื่อให้ Initialization Script ทำงานใหม่โดยไม่ตรวจสอบผลกระทบก่อน

### ปัญหา: `java -version` แสดง Java เวอร์ชันผิด

ตรวจสอบ `JAVA_HOME` และ `PATH` ใน Terminal ที่ใช้งาน ให้ชี้ไปยัง JDK 17 จากนั้นเปิด Terminal ใหม่และตรวจสอบอีกครั้ง

### ปัญหา: PowerShell แจ้ง ParserError

ตรวจสอบว่า `generate-seed.ps1` ถูกบันทึกด้วย Encoding ที่รองรับข้อความในไฟล์ หากใช้ Windows PowerShell 5.1 และพบปัญหาการอ่านอักขระ ให้บันทึกไฟล์เป็น UTF-8 with BOM แล้วทดลองใหม่

---

## 9. ข้อควรระวังเกี่ยวกับ Database

### 9.1 Persistent Volume

Docker Compose ใช้ Persistent Volume เพื่อเก็บข้อมูล Database ให้คงอยู่แม้หยุดหรือสร้าง Container ใหม่

คำสั่ง `stop` ใช้หยุด Container โดยไม่ลบข้อมูลใน Volume

**อย่าใช้คำสั่งต่อไปนี้หากยังต้องการเก็บข้อมูล Database ไว้:**

```bash
docker compose -f database/compose.yaml down -v
```

คำสั่งนี้สามารถลบ Volume ที่ Compose จัดการอยู่ได้ และทำให้ข้อมูล Database ที่เก็บใน Volume สูญหาย

### 9.2 การเปลี่ยนแปลง Schema

ก่อนแก้ไข Schema ให้ตรวจสอบผลกระทบกับข้อมูลเดิมและสมาชิกคนอื่นในทีมก่อน

ไฟล์ใน `database/migrations/` เป็นสคริปต์สำหรับการเปลี่ยนแปลง Schema ในแต่ละช่วงของการพัฒนา ไม่ควรรัน Migration เดิมซ้ำโดยไม่ตรวจสอบว่าเคยนำไปใช้กับ Database แล้วหรือไม่

### 9.3 Password และ Environment Variables

Spring Boot อ่านการตั้งค่าการเชื่อมต่อ Database จาก `cstu-course-nexus/src/main/resources/application.properties`

ค่าที่ใช้ในเครื่องสำหรับการพัฒนา ได้แก่:

- Database URL: `jdbc:postgresql://localhost:5432/cstu_nexus_db`
- Username: `cstu_nexus_user`
- Password: อ่านจาก Environment Variable `DB_PASSWORD` หากกำหนดไว้ มิฉะนั้นใช้ค่าเริ่มต้นสำหรับการพัฒนาในเครื่อง

หากกำหนด `DB_PASSWORD` ต้องใช้รหัสผ่านให้ตรงกับ PostgreSQL Container

รหัสผ่านเริ่มต้นมีไว้สำหรับ Local Development เท่านั้น ไม่ควรนำไปใช้ในระบบ Production และไม่ควร Commit รหัสผ่านจริงของระบบเข้าสู่ Repository

---

**หมายเหตุ:** คู่มือนี้อ้างอิงโครงสร้างและการตั้งค่าของ Repository ในปัจจุบัน หากมีการเปลี่ยนแปลงไฟล์ คำสั่ง หรือการตั้งค่า Database ควรปรับปรุง README ให้ตรงกับระบบจริงด้วย
