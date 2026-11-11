# คู่มือเตรียมความพร้อมและติดตั้ง Tech Stack (CSTU NEXUS)

เอกสารฉบับนี้จัดทำขึ้นเพื่อให้สมาชิกในทีมทุกคนสามารถติดตั้ง ดาวน์โหลด และตรวจสอบสภาพแวดล้อม (Environment & Tools) ที่จำเป็นต้องใช้ในการพัฒนาโปรเจกต์ **CSTU NEXUS** ได้อย่างรวดเร็วและตรงกันทั้งหมด

---

## สารบัญ
1. [ภาพรวม Tech Stack ที่ต้องใช้](#1-ภาพรวม-tech-stack-ที่ต้องใช้)
2. [ลิงก์ดาวน์โหลดอย่างเป็นทางการ (Official Download Links)](#2-ลิงก์ดาวน์โหลดอย่างเป็นทางการ-official-download-links)
3. [คำสั่งติดตั้งด่วนผ่าน Package Manager (ทางเลือก)](#3-คำสั่งติดตั้งด่วนผ่าน-package-manager-ทางเลือก)
4. [ขั้นตอนการตั้งค่าโปรแกรมสำคัญ](#4-ขั้นตอนการตั้งค่าโปรแกรมสำคัญ)
5. [แนะนำ Extension สำหรับ Visual Studio Code](#5-แนะนำ-extension-สำหรับ-visual-studio-code)
6. [คำสั่งตรวจสอบความพร้อมของระบบ (Verification Checklist)](#6-คำสั่งตรวจสอบความพร้อมของระบบ-verification-checklist)
7. [ขั้นตอนเริ่มต้นทำงานหลังติดตั้งเสร็จ (Next Steps)](#7-ขั้นตอนเริ่มต้นทำงานหลังติดตั้งเสร็จ-next-steps)
8. [ปัญหาที่พบบ่อยและการแก้ไข (Troubleshooting)](#8-ปัญหาที่พบบ่อยและการแก้ไข-troubleshooting)

---

## 1. ภาพรวม Tech Stack ที่ต้องใช้

| ลำดับ | รายการ | เวอร์ชันที่ต้องการ | เหตุผลและความจำเป็น | ความสำคัญ |
|:---:|---|:---:|---|:---:|
| 1 | **Git** | `2.x+` | จัดการ Source Code และทำงานร่วมกันผ่าน GitHub | **จำเป็น** |
| 2 | **Java JDK** | **17 (LTS)** | ใช้รันและพัฒนา Spring Boot 4.1.x Backend | **จำเป็น** |
| 3 | **Docker Desktop** | ล่าสุด (พร้อม Docker Compose v2) | ใช้รันฐานข้อมูล PostgreSQL 17 ใน Container | **จำเป็น** |
| 4 | **VS Code / IDE** | ล่าสุด | Editor หลักสำหรับเขียน Frontend, Backend และสคริปต์ | **จำเป็น** |
| 5 | **Web Browser** | Chrome / Edge / Firefox | ใช้ทดสอบหน้าเว็บและระบบ Authentication | **จำเป็น** |
| 6 | **DBeaver / pgAdmin** | ล่าสุด | GUI ดูและจัดการข้อมูลใน PostgreSQL Database | *แนะนำ* |
| 7 | **Thunder Client / Postman** | ล่าสุด | เครื่องมือทดสอบยิง REST API Backend | *แนะนำ* |

> [!NOTE]
> **Maven ไม่จำเป็นต้องติดตั้งแยก:** โปรเจกต์นี้มี Maven Wrapper (`./mvnw` หรือ `mvnw.cmd`) บรรจุมาให้แล้วในโฟลเดอร์ `backend/cstu-course-nexus/`  
> **Node.js / npm ไม่จำเป็นต้องใช้:** Frontend ของเราสร้างด้วย HTML5, CSS3 และ Vanilla JavaScript ES6+ สามารถรันผ่าน VS Code Live Server ได้ทันที

---

## 2. ลิงก์ดาวน์โหลดอย่างเป็นทางการ (Official Download Links)

### 2.1 Git
- **ดาวน์โหลด:** [https://git-scm.com/downloads](https://git-scm.com/downloads)
- *คำแนะนำสำหรับ Windows:* ตอนติดตั้งในหน้า "Choosing the default editor" เลือก VS Code หรือตามที่สะดวก และหน้า "Adjusting your PATH environment" เลือก *Git from the command line and also from 3rd-party software*

### 2.2 Java JDK 17 (แนะนำ Eclipse Temurin)
- **ดาวน์โหลด:** [Eclipse Temurin OpenJDK 17 (LTS)](https://adoptium.net/temurin/releases/?version=17)
- *คำแนะนำสำหรับ Windows:* ดาวน์โหลดตัวติดตั้ง `.msi` (x64) และในขั้นตอนติดตั้งให้ติ๊กเลือก **"Set JAVA_HOME variable"** เป็น Enable เพื่อให้ระบบตั้งค่า Path ให้โดยอัตโนมัติ

### 2.3 Docker Desktop
- **ดาวน์โหลด:** [https://www.docker.com/products/docker-desktop/](https://www.docker.com/products/docker-desktop/)
- *สำหรับ Windows:* จำเป็นต้องเปิดใช้งาน **WSL 2 (Windows Subsystem for Linux)** ก่อน หากยังไม่มีระบบจะแจ้งให้ติดตั้งแพ็กเกจ WSL 2 kernel update

### 2.4 Code Editor
- **VS Code:** [https://code.visualstudio.com/](https://code.visualstudio.com/) *(แนะนำ)*
- หรือ **IntelliJ IDEA Ultimate / Community:** [https://www.jetbrains.com/idea/](https://www.jetbrains.com/idea/)

### 2.5 เครื่องมือเสริม (Database GUI & API Client)
- **DBeaver Community (ฟรี):** [https://dbeaver.io/download/](https://dbeaver.io/download/) *(สำหรับต่อดูตาราง PostgreSQL)*
- **Postman:** [https://www.postman.com/downloads/](https://www.postman.com/downloads/) หรือใช้ส่วนขยาย **Thunder Client** ใน VS Code แทนได้

---

## 3. คำสั่งติดตั้งด่วนผ่าน Package Manager (ทางเลือก)

หากเครื่องของเพื่อน ๆ มี Package Manager ติดตั้งอยู่แล้ว สามารถรันคำสั่งบรรทัดเดียวได้เลย:

### บน Windows (ผ่าน Windows Terminal / PowerShell แบบ Run as Administrator):
```powershell
# ติดตั้ง Git, Temurin JDK 17, Docker Desktop และ VS Code
winget install --id Git.Git -e --source winget
winget install --id EclipseAdoptium.Temurin.17.JDK -e --source winget
winget install --id Docker.DockerDesktop -e --source winget
winget install --id Microsoft.VisualStudioCode -e --source winget
winget install --id dbeaver.dbeaver -e --source winget
```

### บน macOS (ผ่าน Homebrew):
```bash
brew install git
brew install --cask temurin@17
brew install --cask docker
brew install --cask visual-studio-code
brew install --cask dbeaver-community
```

---

## 4. ขั้นตอนการตั้งค่าโปรแกรมสำคัญ

### 4.1 ตั้งค่า Git ชื่อและอีเมล
เมื่อติดตั้ง Git เสร็จ ให้ตั้งค่าระบุตัวตนของคุณใน Git เพื่อให้ commit มีชื่อที่ถูกต้อง:
```bash
git config --global user.name "ชื่อ นามสกุล หรือ Nickname ของคุณ"
git config --global user.email "อีเมลของคุณ@gmail.com หรือ @dome.tu.ac.th"
```

ตั้งค่า Line Ending สำหรับผู้ใช้ Windows:
```bash
git config --global core.autocrlf true
```

### 4.2 ตรวจสอบตัวแปรระบบ `JAVA_HOME`
ตรวจสอบว่า `JAVA_HOME` ชี้ไปยัง JDK 17 อย่างถูกต้อง:
- **Windows (PowerShell):**
  ```powershell
  $env:JAVA_HOME
  ```
  *(ผลลัพธ์ควรชี้ไปที่โฟลเดอร์ของ JDK 17 เช่น `C:\Program Files\Eclipse Adoptium\jdk-17.x.x`)*
- **macOS / Linux:**
  ```bash
  echo $JAVA_HOME
  ```

---

## 5. แนะนำ Extension สำหรับ Visual Studio Code

เมื่อเปิด VS Code แนะนำให้ติดตั้ง Extension ต่อไปนี้ เพื่อให้เขียนโค้ดและดีบั๊กโปรเจกต์ได้สะดวก:

| Extension Name | Publisher | รหัสสำหรับค้นหา (ID) | ประโยชน์ |
|---|---|---|---|
| **Live Server** | Ritwick Dey | `ritwickdey.LiveServer` | **จำเป็นสำหรับ Frontend** คลิกขวารันหน้าเว็บและ Auto Reload ได้ทันที |
| **Extension Pack for Java** | Microsoft | `vscjava.vscode-java-pack` | รัน, คอมไพล์, โค้ดออโต้คอมพลีทสำหรับภาษา Java |
| **Spring Boot Extension Pack** | VMware | `vmware.vscode-boot-dev-pack` | ไฮไลต์โค้ดและรัน Spring Boot Application ได้สะดวก |
| **Lombok Annotations Support** | Gabriel Oliveira | `gableroux.lombok` | รองรับ Annotation `@Getter`, `@Setter`, `@Data` ของ Lombok |
| **Docker** | Microsoft | `ms-azuretools.vscode-docker` | ดูสถานะ Container, Logs และ Restart PostgreSQL ได้จาก VS Code |
| **Thunder Client** | Thunder Client | `rangav.vscode-thunder-client` | ใช้ยิงทดสอบ REST API ภายใน VS Code โดยไม่ต้องเปิดโปรแกรมอื่น |

> [!TIP]
> **ติดตั้งรวดเร็วผ่านคำสั่ง:**  
> สามารถเปิด Terminal แล้วคัดลอกคำสั่งด้านล่างไปวางเพื่อลง Extension ทั้งหมดได้ในครั้งเดียว:
> ```bash
> code --install-extension ritwickdey.LiveServer
> code --install-extension vscjava.vscode-java-pack
> code --install-extension vmware.vscode-boot-dev-pack
> code --install-extension gableroux.lombok
> code --install-extension ms-azuretools.vscode-docker
> code --install-extension rangav.vscode-thunder-client
> ```

---

## 6. คำสั่งตรวจสอบความพร้อมของระบบ (Verification Checklist)

เปิด Terminal / PowerShell แล้วรันคำสั่งเหล่านี้ทีละบรรทัดเพื่อเช็คความพร้อม:

```bash
# 1. ตรวจสอบ Git
git --version
# ตัวอย่างผลลัพธ์ที่ถูกต้อง: git version 2.40.0 ขึ้นไป

# 2. ตรวจสอบ Java (ต้องเป็นเวอร์ชัน 17 เท่านั้น)
java -version
# ตัวอย่างผลลัพธ์ที่ถูกต้อง: openjdk version "17.0.x" ...

# 3. ตรวจสอบ Docker
docker --version
# ตัวอย่างผลลัพธ์ที่ถูกต้อง: Docker version 24.x.x หรือ 27.x.x ขึ้นไป

# 4. ตรวจสอบ Docker Compose
docker compose version
# ตัวอย่างผลลัพธ์ที่ถูกต้อง: Docker Compose version v2.x.x ขึ้นไป
```

### สคริปต์ตรวจสอบอัตโนมัติ (Automated Check Scripts):

คุณสามารถรันสคริปต์ที่เตรียมไว้ให้ที่ root โฟลเดอร์ เพื่อตรวจเช็คความพร้อมทุกตัวในครั้งเดียวได้เลย:

- **บน Windows (PowerShell):**
  ```powershell
  powershell -ExecutionPolicy Bypass -File .\check-requirements.ps1
  ```
- **บน macOS / Linux / Git Bash:**
  ```bash
  chmod +x ./check-requirements.sh
  ./check-requirements.sh
  ```

---

## 7. ขั้นตอนเริ่มต้นทำงานหลังติดตั้งเสร็จ (Next Steps)

เมื่อติดตั้ง Tool ครบถ้วนแล้ว สามารถเริ่มรันโปรเจกต์ได้ตามลำดับดังนี้:

### สเต็ปที่ 1: เตรียมไฟล์ `.env`
คัดลอกไฟล์ `.env.example` เป็น `.env` ที่ root ของโปรเจกต์:
```powershell
# บน Windows PowerShell
copy .env.example .env

# บน macOS / Linux / Git Bash
cp .env.example .env
```
จากนั้นเปิดไฟล์ `.env` แล้วนำ `TU_API_APPLICATION_KEY` มาใส่ (ขอรับ Key ได้จากเพื่อนในกลุ่มหรือตัวแทนโปรเจกต์)

### สเต็ปที่ 2: รัน PostgreSQL Database
เปิดโปรแกรม **Docker Desktop** ให้พร้อมใช้งาน จากนั้นเปิด Terminal ที่โฟลเดอร์ root ของโปรเจกต์แล้วรัน:
```bash
docker compose -f backend/database/compose.yaml up -d db
```
*(ตารางข้อมูลและชุด Seed Data 38 รายวิชา 63 เซกชัน จะถูกสร้างและโหลดเข้าอัตโนมัติ)*

### สเต็ปที่ 3: รัน Backend (Spring Boot)
เปิด Terminal เข้าไปยังโฟลเดอร์ Backend:
```bash
cd backend/cstu-course-nexus

# บน Windows:
.\mvnw.cmd spring-boot:run

# บน macOS / Linux:
./mvnw spring-boot:run
```
เมื่อรันสำเร็จ Backend จะเปิดให้บริการที่: `http://localhost:8080`  
สามารถเปิดเบราว์เซอร์ไปทดสอบดูสถานะได้ที่: `http://localhost:8080/api/health`

### สเต็ปที่ 4: รัน Frontend
1. ใน VS Code ให้เปิดไฟล์ `frontend/pages/index.html`
2. คลิกขวาในหน้าโค้ด แล้วเลือก **"Open with Live Server"**
3. เบราว์เซอร์จะเปิดหน้าเว็บขึ้นมาที่ `http://127.0.0.1:5500/frontend/pages/index.html` พร้อมใช้งาน

---

## 8. ปัญหาที่พบบ่อยและการแก้ไข (Troubleshooting)

### 8.1 `java -version` แสดงเวอร์ชันอื่นที่ไม่ใช่ 17 (เช่น Java 8, 11 หรือ 21)
- ตรวจสอบ `JAVA_HOME` และ Environment Variable ในระบบ ให้ย้าย Path ของ JDK 17 ขึ้นมาไว้บนสุด
- ปิด Terminal ทั้งหมดแล้วเปิดใหม่

### 8.2 Docker ไม่ยอมรัน หรือแจ้งเตือน WSL 2 Error บน Windows
- เปิด PowerShell ด้วยสิทธิ์ Administrator แล้วสั่งอัปเดต WSL:
  ```powershell
  wsl --update
  ```
- รีสตาร์ตเครื่องคอมพิวเตอร์ 1 ครั้ง แล้วเปิด Docker Desktop ใหม่

### 8.3 พอร์ต `5432` ชน (Port already allocated)
- สาเหตุ: มี PostgreSQL ตัวเดิมที่เคยติดตั้งไว้ในเครื่องกำลังรันอยู่เบื้องหลัง
- วิธีแก้บน Windows: เปิดโปรแกรม `Services.msc` ค้นหา Service ที่ชื่อ `postgresql-x64-xx` แล้วกด Stop ชั่วคราว ก่อนรัน Docker

### 8.4 สิทธิ์การรัน `./mvnw` บน macOS / Linux แจ้ง `Permission denied`
- ให้เพิ่มสิทธิ์รันให้ไฟล์ Maven Wrapper:
  ```bash
  chmod +x backend/cstu-course-nexus/mvnw
  ```

---

> หากพบปัญหาในการติดตั้ง สามารถทักถามเพื่อน ๆ ในกลุ่มหรือส่ง Log ในแชตทีมได้ทันทีครับ! 🚀
