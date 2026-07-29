# WAR Packaging Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** เปลี่ยน Spring Boot backend ให้สร้าง executable WAR ซึ่ง deploy บน external Tomcat ได้ โดยยังรวม React production build ไว้ใน artifact เดียว

**Architecture:** Maven จะสร้าง `dorm-api-0.1.0-SNAPSHOT.war`; Tomcat ถูกกำหนดเป็น `provided` สำหรับ external container และ Spring Boot Maven Plugin จะเก็บ dependencies ที่จำเป็นสำหรับ `java -jar` ไว้ในรูปแบบ executable WAR. `DormApplication` จะรองรับทั้ง `main` method และ servlet-container bootstrap.

**Tech Stack:** Java 21, Spring Boot 4.1, Maven, Tomcat 11, React/Vite static build, MySQL/Flyway

## Global Constraints

- Artifact หลักต้องเป็น `backend/target/dorm-api-0.1.0-SNAPSHOT.war`
- WAR ต้อง deploy บน external Tomcat และรันด้วย `java -jar` ได้
- React build จาก `frontend/dist` ต้องอยู่ใน WAR และให้บริการจาก classpath `static`
- Runtime configuration และ credentials ต้องใช้ environment variables เดิม
- ห้ามเปลี่ยน REST API, database schema, authentication และ frontend behavior
- External runtime ใช้ Java 21 และ Jakarta-compatible Tomcat 11

---

### Task 1: Configure Dual-Mode WAR Bootstrap

**Files:**
- Modify: `backend/pom.xml`
- Modify: `backend/src/main/java/th/ac/dusit/dorm/DormApplication.java`

**Interfaces:**
- Consumes: Spring Boot application entry point `DormApplication.main(String[])`
- Produces: Maven WAR artifact and `SpringBootServletInitializer.configure(SpringApplicationBuilder)` entry point

- [ ] **Step 1: Confirm the current artifact type is not WAR**

Run from `backend`:

```powershell
mvn help:evaluate "-Dexpression=project.packaging" -q -DforceStdout
```

Expected before implementation: `jar`.

- [ ] **Step 2: Change Maven packaging and Tomcat scope**

Add after `<version>0.1.0-SNAPSHOT</version>` in `backend/pom.xml`:

```xml
<packaging>war</packaging>
```

Add after `spring-boot-starter-web`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-tomcat</artifactId>
    <scope>provided</scope>
</dependency>
```

- [ ] **Step 3: Add external servlet-container bootstrap**

Change `DormApplication` to:

```java
package th.ac.dusit.dorm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class DormApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(DormApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(DormApplication.class, args);
    }
}
```

- [ ] **Step 4: Verify compilation and backend tests**

Run:

```powershell
mvn test
```

Expected: exit code `0`, all existing tests pass.

- [ ] **Step 5: Record checkpoint**

This workspace has no `.git` repository, so do not initialize Git implicitly. Record `pom.xml` and `DormApplication.java` in the final changed-file summary instead of committing.

### Task 2: Package, Inspect, Run, and Document the WAR

**Files:**
- Modify: `README.md`
- Verify: `backend/target/dorm-api-0.1.0-SNAPSHOT.war`

**Interfaces:**
- Consumes: WAR packaging and servlet initializer from Task 1
- Produces: documented build/run/deploy commands and verified executable WAR

- [ ] **Step 1: Update build and deployment documentation**

Replace the JAR run command in `README.md` with:

```powershell
cd frontend
npm ci
npm test
npm run build

cd ..\backend
mvn clean test package
java -jar target\dorm-api-0.1.0-SNAPSHOT.war
```

Add external Tomcat deployment instructions:

```text
Copy backend/target/dorm-api-0.1.0-SNAPSHOT.war to TOMCAT_HOME/webapps/dorm-api.war.
The default context path is /dorm-api. Rename the artifact to ROOT.war to deploy at /.
Configure DORM_DB_URL, DORM_DB_USERNAME, DORM_DB_PASSWORD, DORM_STORAGE_PATH, and production cookie settings in the Tomcat service environment before startup.
```

- [ ] **Step 2: Build the production artifact**

Run from `backend`:

```powershell
mvn clean package
```

Expected: exit code `0` and `target/dorm-api-0.1.0-SNAPSHOT.war` exists.

- [ ] **Step 3: Inspect the WAR structure**

Run:

```powershell
& "C:\Program Files\Java\jdk-21\bin\jar.exe" tf target\dorm-api-0.1.0-SNAPSHOT.war |
    Select-String 'WEB-INF/classes/th/ac/dusit/dorm/DormApplication.class|WEB-INF/classes/static/index.html|WEB-INF/lib-provided/.*tomcat|org/springframework/boot/loader'
```

Expected: matches for the application class, React `index.html`, Tomcat under `WEB-INF/lib-provided`, and Spring Boot loader classes.

- [ ] **Step 4: Verify executable WAR startup**

Run the WAR on a free test port:

```powershell
java -jar target\dorm-api-0.1.0-SNAPSHOT.war --server.port=8081
```

In another PowerShell terminal run:

```powershell
Invoke-RestMethod http://127.0.0.1:8081/actuator/health
```

Expected: `status` is `UP`. Stop only the Java process launched for this verification after the health check.

- [ ] **Step 5: Confirm no debug artifacts remain**

Run from the workspace root:

```powershell
rg -n "DEBUG-login|TODO|TBD" backend\src README.md
```

Expected: no temporary debug marker or unfinished WAR instruction.

- [ ] **Step 6: Record final checkpoint**

Because the workspace has no `.git` repository, list the modified source files, test result, WAR path, archive inspection results, and health-check result in the handoff instead of committing.
