# CSTU-NEXUS-Backend

ระบบ Database สำหรับโปรเจกต์ CSTU NEXUS

## สิ่งที่ต้องมีก่อน

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- Git

## 1. การตั้งค่า Database (PostgreSQL)

โปรเจกต์นี้ใช้ PostgreSQL 17 โดยทำงานผ่าน Docker เพื่อให้สมาชิกในทีมใช้สภาพแวดล้อมฐานข้อมูลร่วมกันได้อย่างสม่ำเสมอ

### ขั้นตอนที่ 1: เปิด Docker Desktop

เปิด Docker Desktop และรอจนกว่า Docker จะพร้อมใช้งานก่อนดำเนินการขั้นตอนต่อไป

### ขั้นตอนที่ 2: เริ่มต้น Database

เปิด Terminal ที่โฟลเดอร์หลักของ Repository `CSTU-NEXUS-Backend` แล้วรันคำสั่ง:

```bash
docker compose -f database/compose.yaml up -d
```

Docker Compose จะสร้าง Container และ Persistent Volume หากยังไม่มีอยู่

### ขั้นตอนที่ 3: ตรวจสอบสถานะ Database

```bash
docker compose -f database/compose.yaml ps
```

ตรวจสอบว่า Container ของ PostgreSQL แสดงสถานะกำลังทำงาน (`Up`)

### ขั้นตอนที่ 4: เชื่อมต่อ PostgreSQL

```bash
docker exec -it cstu-nexus-postgres psql -U cstu_nexus_user -d cstu_nexus_db
```

เมื่อต้องการแสดงตารางทั้งหมดใน Database ให้ใช้คำสั่งภายใน psql:

\dt

เมื่อต้องการออกจาก PostgreSQL ให้ใช้:

\q

## 2. ข้อมูลการเชื่อมต่อ Database

ค่าการตั้งค่าสำหรับการพัฒนาในเครื่องอยู่ในไฟล์ `database/compose.yaml`

| รายการ | ค่า |
|---|---|
| Database | `cstu_nexus_db` |
| Username | `cstu_nexus_user` |
| Port | `5432` |
| PostgreSQL Version | `17` |

รหัสผ่านสำหรับการพัฒนากำหนดไว้ในไฟล์ `database/compose.yaml`

**หมายเหตุ:** ข้อมูลรับรองนี้มีไว้สำหรับการพัฒนาในเครื่องเท่านั้น ไม่ควรใช้ในระบบ Production

## 3. โครงสร้าง Database

ไฟล์ `database/init.sql` ใช้กำหนดโครงสร้างตารางของ Database โดยปัจจุบันประกอบด้วย:

- `mock_user` — ข้อมูลผู้ใช้จำลอง
- `courses` — ข้อมูลรายวิชา
- `sections` — ข้อมูล Section ของรายวิชา
- `review` — ข้อมูลรีวิว

**ข้อควรระวัง:** PostgreSQL จะรันไฟล์ Initialization Script อัตโนมัติเฉพาะตอนเริ่มต้นฐานข้อมูลใหม่ที่ Data Directory ยังว่างอยู่เท่านั้น หาก Database มีข้อมูลอยู่แล้ว การแก้ไข `init.sql` จะไม่ทำให้ตารางเดิมเปลี่ยนแปลงโดยอัตโนมัติ ต้องดำเนินการปรับ Schema แยกต่างหากเมื่อจำเป็น

## 4. การนำเข้าข้อมูลเริ่มต้น (Seed Data)

ไฟล์ข้อมูลต้นทางอยู่ใน `database/`:

- `courses.csv` — ข้อมูลรายวิชา
- `sections.csv` — ข้อมูล Section ของรายวิชา
- `generate-seed.mjs` — สคริปต์ตรวจสอบข้อมูลและสร้าง `seed.sql`
- `seed.sql` — คำสั่ง SQL สำหรับนำเข้าข้อมูลเริ่มต้น

ปัจจุบัน `seed.sql` เตรียมนำเข้า 38 Courses และ 63 Sections โดยไม่รวม Placeholder Sections ที่มี `section_number` เป็น `000000`

### การนำเข้าข้อมูลครั้งแรก

ให้ใช้คำสั่งนี้เฉพาะเมื่อฐานข้อมูลมีตารางครบแล้ว แต่ยังไม่มีข้อมูล Courses และ Sections:

```bash
docker exec -i cstu-nexus-postgres psql -U cstu_nexus_user -d cstu_nexus_db -v ON_ERROR_STOP=1 < database/seed.sql
```

### ข้อควรระวัง

- `seed.sql` ไม่ได้ออกแบบให้รันซ้ำบนฐานข้อมูลที่มีข้อมูลแล้ว การรันซ้ำอาจทำให้เกิด Primary Key ซ้ำ
- ก่อนนำเข้าข้อมูล ให้ตรวจจำนวนแถวใน `courses` และ `sections` ก่อนทุกครั้ง
- หากข้อมูล CSV เปลี่ยน ให้รัน `node database/generate-seed.mjs` เพื่อสร้าง `seed.sql` ใหม่ และตรวจสอบผลลัพธ์ก่อนนำเข้า
- อย่าใช้ `docker compose down -v` หากต้องการเก็บข้อมูลใน Persistent Volume ไว้

## 5. การหยุดและเริ่ม Database

### หยุด Database

```bash
docker compose -f database/compose.yaml stop
```

คำสั่งนี้หยุด Container โดยไม่ลบข้อมูลใน Persistent Volume

### เริ่ม Database อีกครั้ง

```bash
docker compose -f database/compose.yaml start
```

**ข้อควรระวังเกี่ยวกับข้อมูล**

หลีกเลี่ยงคำสั่ง `docker compose down -v` หากไม่ต้องการลบ Persistent Volume และข้อมูล Database ที่เก็บอยู่ภายใน

## 6. การแก้ไขปัญหาเบื้องต้น

- หาก Docker ไม่สามารถเริ่มทำงานได้ ให้ตรวจสอบว่า Docker Desktop เปิดอยู่
- หาก Port `5432` ถูกใช้งานอยู่ ให้ตรวจสอบว่ามี PostgreSQL หรือ Container อื่นใช้งาน Port นี้หรือไม่
- หากไม่พบตารางหลังเริ่ม Database ให้ตรวจสอบว่าเชื่อมต่อ Database ถูกต้อง และตรวจสอบการทำงานของ Initialization Script
- หากแก้ไข `init.sql` แล้วตารางเดิมไม่เปลี่ยนแปลง ให้จำไว้ว่าสคริปต์นี้ไม่ได้รันซ้ำอัตโนมัติบนฐานข้อมูลที่มีข้อมูลอยู่แล้ว