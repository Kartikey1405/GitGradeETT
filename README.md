# GitGrade

GitGrade is an AI-powered code analysis platform designed to evaluate GitHub repositories. It provides developers with instant feedback on code quality, structural organization, and adherence to best practices.

By leveraging AI-driven analysis and custom scoring algorithms, the application assigns a repository health score (0–100) and generates actionable improvement roadmaps to help developers enhance their projects efficiently.

---

## Features

- Instant repository analysis using GitHub metadata, file structure, and README content
- AI-powered insights into code quality, security vulnerabilities, and documentation standards
- Smart scoring system that calculates a comprehensive score (0–100)
- Interactive dashboard for visualizing repository structure and feedback
- AI mentor roadmap with step-by-step improvement suggestions
- PDF report generation with email delivery
- Secure authentication using JWT and Google OAuth

---

## Tech Stack

### Frontend
- React.js (Vite)
- TypeScript
- Tailwind CSS
- Framer Motion
- React Hooks
- Axios

### Backend
- Java Spring Boot
- Maven
- Spring Security & JWT
- PostgreSQL
- Google Gemini API
- GitHub REST API
- JavaMailSender (PDF generation & email delivery)

---

## Deployment

- Cloud Provider: Render
- CI/CD: Automatic deployments from GitHub

---

## Project Structure

### Backend (Spring Boot)

```
src/main/java/com/gitgrade/GitGradeSpring
├── controller      # REST API endpoints (Analyze, Auth, Payment)
├── dto             # Data Transfer Objects
├── entity          # Database entities (User, Analysis)
├── repository      # JPA repositories
├── service         # Business logic (GitHub, AI, Email services)
├── util            # Utility classes (JWT helpers)
└── Application.java
```

### Frontend (React)

```
src
├── components
│   ├── dashboard   # Analysis views (ScoreGauge, FileTree, AIMentor)
│   ├── layout      # Layout components
│   └── ui          # Reusable UI components
├── lib
│   └── api.ts      # Axios configuration
├── types           # TypeScript interfaces
└── App.tsx         # Application entry point
```

---

## Getting Started

### Prerequisites
- Java JDK 17 or higher
- Node.js (v16+) and npm
- PostgreSQL
- Git

---

## Installation

### Clone the Repository

```bash
git clone https://github.com/yourusername/gitgrade.git
cd gitgrade
```

---

## Backend Setup

Configure environment variables in `application.properties`.

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/gitgrade
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password

# API Keys
gemini.api.key=your_google_gemini_key
github.token=your_github_personal_access_token
jwt.secret=your_jwt_secret_key
```

Run the backend:

```bash
./mvnw spring-boot:run
```

Backend URL:
```
http://localhost:8080
```

---

## Frontend Setup

```bash
cd frontend
npm install
```

Create `.env`:

```env
VITE_API_BASE_URL=http://localhost:8080
```

Run frontend:

```bash
npm run dev
```

Frontend URL:
```
http://localhost:5173
```

---

## Usage

1. Open http://localhost:5173 in your browser  
2. Log in using the authentication flow  
3. Paste a public GitHub repository URL (e.g., https://github.com/facebook/react)  
4. Click **Analyze** to generate the report  
5. Use **Send Report** to receive the PDF summary via email  

---

## Contributing

Contributions are welcome.

1. Fork the repository  
2. Create your feature branch:
   ```bash
   git checkout -b feature/AmazingFeature
   ```
3. Commit your changes:
   ```bash
   git commit -m "Add AmazingFeature"
   ```
4. Push to the branch:
   ```bash
   git push origin feature/AmazingFeature
   ```
5. Open a Pull Request

---

## License

This project is licensed under the MIT License.
