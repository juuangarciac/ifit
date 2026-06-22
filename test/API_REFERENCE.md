# 📖 API Reference - iFit Testing

Quick reference for all API endpoints and request/response structures used in tests.

## 🔐 Authentication API

### POST /auth/register
**Create new user and auto-login**

**Request**:
```json
{
  "name": "Juan",
  "surname": "García",
  "email": "juan@example.com",
  "password": "Test@1234",
  "birthdate": "1990-01-01",
  "phone": "651634807"
}
```

**Response** (201 Created):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 300,
  "appUser": {
    "id": 42,
    "email": "juan@example.com",
    "name": "Juan",
    "isVerified": false,
    "isRegistrationComplete": false,
    "coachModelType": null,
    "experienceLevel": null
  }
}
```

### POST /auth/verify
**Verify user with email verification code**

**Request**:
```json
{
  "email": "juan@example.com",
  "verificationCode": "123456"
}
```

**Response** (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 300,
  "appUser": {
    "id": 42,
    "email": "juan@example.com",
    "isVerified": true,
    "isRegistrationComplete": false
  }
}
```

### POST /auth/login
**Login with email and password**

**Request**:
```json
{
  "email": "juan@example.com",
  "password": "Test@1234"
}
```

**Response** (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 300,
  "appUser": {
    "id": 42,
    "email": "juan@example.com",
    "isVerified": true
  }
}
```

### POST /auth/refresh
**Get new access token using refresh token**

**Request**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**Response** (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 300
}
```

### POST /auth/logout
**Logout and invalidate refresh token**

**Request**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**Response** (200 OK):
```json
{
  "message": "Session closed successfully",
  "success": true
}
```

### POST /auth/resend-verification
**Resend verification email**

**Request**:
```json
{
  "email": "juan@example.com"
}
```

**Response** (204 No Content):
```
(empty body)
```

---

## 👤 User Profile API

### GET /users/{userId}/profile
**Get user profile**

**Headers**:
```
Authorization: Bearer {accessToken}
```

**Response** (200 OK):
```json
{
  "id": 42,
  "email": "juan@example.com",
  "name": "Juan",
  "surname": "García",
  "birthdate": "1990-01-01",
  "phone": "651634807",
  "isVerified": true,
  "isRegistrationComplete": true,
  "coachModelType": {
    "id": 1,
    "name": "Ronnie"
  },
  "experienceLevel": {
    "id": 1,
    "name": "Principiante"
  }
}
```

### PUT /users/{userId}/profile
**Update user profile**

**Headers**:
```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request**:
```json
{
  "coachModelTypeId": 1,
  "experienceLevelId": 1
}
```

**Response** (200 OK):
```json
{
  "id": 42,
  "email": "juan@example.com",
  "coachModelType": {
    "id": 1,
    "name": "Ronnie"
  },
  "experienceLevel": {
    "id": 1,
    "name": "Principiante"
  }
}
```

---

## ❓ Questionnaire API

### POST /users/{userId}/questionnaire
**Answer questionnaire question**

**Headers**:
```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request**:
```json
{
  "questionText": "¿Cuánta experiencia tienes?",
  "answerValue": "Nunca he entrenado"
}
```

**Response** (201 Created):
```json
{
  "id": 1,
  "userId": 42,
  "questionText": "¿Cuánta experiencia tienes?",
  "answerValue": "Nunca he entrenado",
  "createdAt": "2026-06-22T10:30:00"
}
```

### GET /users/{userId}/questionnaire
**Get all questionnaire answers**

**Headers**:
```
Authorization: Bearer {accessToken}
```

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "questionText": "¿Cuánta experiencia tienes?",
    "answerValue": "Nunca he entrenado"
  },
  {
    "id": 2,
    "questionText": "¿Lesiones previas?",
    "answerValue": "No"
  }
]
```

---

## 🤖 Ronnie AI Model API

### POST /ifit/aimodels/api/v1/generate-routine
**Generate personalized training routine**

**URL**: `http://localhost:8082/ifit/aimodels/api/v1/generate-routine`

**Headers**:
```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request**:
```json
{
  "userId": 42,
  "days": 3
}
```

**Response** (200 OK):
```json
{
  "id": 1,
  "userId": 42,
  "title": "Rutina Full Body - 3 días",
  "description": "Rutina equilibrada para principiantes...",
  "days": [
    {
      "day": "Lunes",
      "exercises": [
        {
          "exerciseName": "Flexiones",
          "sets": 3,
          "reps": "10-12",
          "restSeconds": 60
        },
        {
          "exerciseName": "Sentadillas",
          "sets": 3,
          "reps": "12-15",
          "restSeconds": 60
        }
      ]
    },
    {
      "day": "Miércoles",
      "exercises": [...]
    },
    {
      "day": "Viernes",
      "exercises": [...]
    }
  ],
  "createdAt": "2026-06-22T10:35:00"
}
```

### GET /ifit/aimodels/api/v1/routines/{userId}
**Get user's generated routines**

**Headers**:
```
Authorization: Bearer {accessToken}
```

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "userId": 42,
    "title": "Rutina Full Body - 3 días",
    "days": 3,
    "createdAt": "2026-06-22T10:35:00"
  }
]
```

---

## 💾 Database Schema Reference

### users Table
```sql
CREATE TABLE user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  birthdate DATE,
  phone VARCHAR(20),
  
  -- Verification
  verification_code VARCHAR(6),
  is_verified BOOLEAN DEFAULT FALSE,
  verification_code_expires_at TIMESTAMP,
  
  -- Registration
  is_registration_complete BOOLEAN DEFAULT FALSE,
  
  -- Keycloak Integration
  keycloak_id VARCHAR(255) UNIQUE,
  
  -- Profile
  coach_model_type_id BIGINT,
  experience_level_id BIGINT,
  
  -- Timestamps
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  FOREIGN KEY (role_id) REFERENCES app_role(id),
  FOREIGN KEY (coach_model_type_id) REFERENCES coach_model_type(id),
  FOREIGN KEY (experience_level_id) REFERENCES experience_level(id)
);
```

### coach_model_type Table
```sql
SELECT id, name, description FROM coach_model_type;
-- Results:
-- 1 | Ronnie | Entrenador generalista equilibrado
-- 2 | Serena | Enfoque en musculación y fuerza
-- 3 | Eliud | Enfoque en cardio y resistencia
-- 4 | Kael  | Enfoque en bienestar y wellness
```

### experience_level Table
```sql
SELECT id, name, description FROM experience_level;
-- Results:
-- 1 | Principiante  | Sin experiencia en ejercicio
-- 2 | Intermedio    | Entrena 2-3 veces por semana
-- 3 | Avanzado      | Entrena 5+ veces por semana
```

### exercise_catalog Table
```sql
SELECT 
  id, exercise_name, category, level,
  sets, reps, rest_seconds, description
FROM exercise_catalog
ORDER BY level, category;
-- Contiene ejercicios divididos en:
--   warmup, beginner, intermediate, advanced, stretching
```

### questionnaire_answer Table
```sql
SELECT 
  qa.id, qa.user_id, q.question_text, qa.answer_value
FROM questionnaire_answer qa
JOIN questionnaire q ON qa.questionnaire_id = q.id
WHERE qa.user_id = ?;
```

### routine Table
```sql
SELECT 
  id, user_id, title, description, days, created_at
FROM routine
WHERE user_id = ?
ORDER BY created_at DESC;
```

---

## 🔑 Error Codes

| Status | Code | Message | Solution |
|--------|------|---------|----------|
| 201 | - | User created | ✅ Registration successful |
| 200 | - | Success | ✅ Request successful |
| 204 | - | No content | ✅ Logout successful |
| 400 | INVALID_CODE | Verification code incorrect | Get new code from DB |
| 400 | INVALID_INPUT | Missing required fields | Check request body |
| 401 | INVALID_CREDENTIALS | Email/password incorrect | Verify email exists and verified |
| 401 | INVALID_TOKEN | Token expired/invalid | Use refresh token |
| 409 | EMAIL_EXISTS | Email already registered | Use different email |
| 500 | SERVER_ERROR | Internal error | Check service logs |

---

## 🧪 Test Data

### Standard Test User
```json
{
  "email": "test_1718962845123@example.com",
  "password": "Test@1234",
  "name": "testuser_1718962845123",
  "birthdate": "1990-01-01",
  "phone": "651634807"
}
```

### Test Profiles
```typescript
// Beginner
{
  coach: "Ronnie",
  level: "Principiante",
  questionnaire: {
    "Experiencia": "Nunca he entrenado",
    "Lesiones": "No",
    "Objetivo": "Perder peso"
  }
}

// Intermediate
{
  coach: "Serena",
  level: "Intermedio",
  questionnaire: {
    "Experiencia": "Entreno 2-3 veces por semana",
    "Lesiones": "No",
    "Objetivo": "Ganar masa muscular"
  }
}

// Advanced
{
  coach: "Eliud",
  level: "Avanzado",
  questionnaire: {
    "Experiencia": "Entreno más de 5 días por semana",
    "Lesiones": "No",
    "Objetivo": "Mejorar performance"
  }
}

// Wellness
{
  coach: "Kael",
  level: "Principiante",
  questionnaire: {
    "Experiencia": "Sedentario",
    "Lesiones": "Dolor de espalda",
    "Objetivo": "Mejorar salud general"
  }
}
```

---

## 📝 Sample cURL Requests

### Register
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Juan",
    "surname": "García",
    "email": "juan@example.com",
    "password": "Test@1234",
    "birthdate": "1990-01-01",
    "phone": "651634807"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "juan@example.com",
    "password": "Test@1234"
  }'
```

### Generate Routine
```bash
curl -X POST http://localhost:8082/ifit/aimodels/api/v1/generate-routine \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 42,
    "days": 3
  }'
```

---

**Last Updated**: 2026-06-22  
**Version**: 1.0
