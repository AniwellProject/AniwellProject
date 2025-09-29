# Aniwell Project 🐾

Aniwell Project는 Spring Boot와 관련 기술을 활용하여 **반려동물 관리 및 커뮤니티 서비스**를 구현한 웹 애플리케이션입니다.  
사용자는 반려동물 정보를 등록하고, 게시판을 통해 소통하며, AI FAQ 기능을 통해 자주 묻는 질문에 자동으로 답변을 받을 수 있습니다.

---

## 🚀 주요 기능
- **회원 관리**
  - 회원가입, 로그인, 로그아웃
  - 사용자 프로필 관리

- **게시판 기능**
  - 글 작성, 수정, 삭제
  - 댓글 작성 및 소통
  - 파일 업로드 지원

- **AI FAQ 자동응답**
  - 사용자가 질문 입력 시, FAQ DB 검색
  - FAQ에 없는 질문은 AI 프롬프트 응답 제공

- **반려동물 관리**
  - 반려동물 프로필 등록
  - 건강 기록 및 일정 관리

---

## 🛠 기술 스택
- **Backend**: Spring Boot, Spring MVC, MyBatis/JPA
- **Frontend**: Thymeleaf, HTML5, CSS3, JavaScript
- **Database**: MySQL (DB.sql 포함)
- **Infra**: Docker, Terraform, AWS
- **CI/CD**: GitHub Actions

---

## 📂 프로젝트 구조
```
AniwellProject-main/
├── my-app/            # 주요 애플리케이션 코드
├── src/               # 소스 코드
├── upload/            # 업로드 파일 저장
├── DB.sql             # 데이터베이스 초기화 스크립트
├── Dockerfile         # Docker 환경 설정
├── main.tf            # Terraform 인프라 설정
├── pom.xml            # Maven 빌드 설정
└── README.md          # 프로젝트 설명
```

---

## ⚙️ 설치 및 실행 방법

### 1. 프로젝트 클론
```bash
git clone https://github.com/your-repo/AniwellProject.git
cd AniwellProject-main
```

### 2. 데이터베이스 설정
```bash
mysql -u root -p < DB.sql
```

### 3. 애플리케이션 실행
```bash
./mvnw spring-boot:run
```

### 4. 접속
```
http://localhost:8080
```

---

## 📌 향후 계획
- 반려동물 위치 기반 서비스 추가
- 모바일 반응형 UI 개선
- AI 챗봇 대화 기능 확장

---

## 👨‍💻 기여 방법
1. Fork 저장소
2. 기능 개발 후 Pull Request 요청
3. Issue 등록을 통해 버그 및 개선사항 공유

---

## 📜 라이선스
이 프로젝트는 MIT License를 따릅니다.
