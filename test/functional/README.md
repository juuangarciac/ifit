# 🧪 Functional Testing Scripts - iFit

Complete E2E testing suite for iFit authentication flow and AI model (Ronnie) response validation.

## 📁 Files Structure

```
test/functional/
├── auth-flow.spec.ts          # Complete authentication flow testing
├── model-testing.spec.ts      # Ronnie model response validation
└── README.md                  # This file
```

## 🔐 Authentication Flow Tests (`auth-flow.spec.ts`)

Complete E2E test of the entire user authentication lifecycle:

### Test Flow

1. **User Registration** (POST `/auth/register`)
   - Create new user with email, password, name, birthdate, phone
   - Get auto-generated verification code
   - Returns access token, refresh token, and user profile

2. **Verification Code Retrieval** (Direct Database Access)
   - Query MySQL directly to get verification code
   - Verify it's a valid 6-digit code
   - Confirm user data in database

3. **Email Verification** (POST `/auth/verify`)
   - Verify user with correct verification code
   - Obtain new tokens after verification
   - Test invalid code rejection

4. **User Login** (POST `/auth/login`)
   - Login with email and password
   - Get valid JWT tokens (access + refresh)
   - Test wrong password/email rejection

5. **Token Management**
   - Refresh tokens using refresh token (POST `/auth/refresh`)
   - Logout and invalidate tokens (POST `/auth/logout`)
   - Validate token expiration

6. **Credentials Summary**
   - Final verification of complete flow
   - Display obtained credentials

### Running Auth Tests

```bash
# Run all auth flow tests
npm test -- test/functional/auth-flow.spec.ts

# Or using npm script
npm run test:auth
```

### Expected Test Results

```
✅ User Registration
✅ Duplicate Email Prevention
✅ Verification Code Retrieval from DB
✅ User Data in Database
✅ Email Verification with Code
✅ Invalid Code Rejection
✅ User Login
✅ Wrong Password Rejection
✅ Non-existent Email Rejection
✅ Token Refresh
✅ Logout & Token Invalidation
✅ Credentials Summary
```

## 🤖 Model Testing (`model-testing.spec.ts`)

Complete validation suite for Ronnie AI model routine generation with multiple profiles.

### Test Profiles

#### 1. **Beginner with Ronnie Coach**
- Experience Level: Principiante (Beginner)
- Coach: Ronnie
- Questionnaire: Nunca he entrenado, No lesiones, Objetivo: Perder peso
- Expected: 3-day full-body routine with basic exercises

#### 2. **Intermediate with Serena Coach**
- Experience Level: Intermedio (Intermediate)
- Coach: Serena
- Questionnaire: 2-3 times/week training, Objetivo: Ganar masa muscular
- Expected: 4-day split routine with compound exercises

#### 3. **Advanced Athlete with Eliud Coach**
- Experience Level: Avanzado (Advanced)
- Coach: Eliud
- Questionnaire: 5+ days/week training, Objetivo: Mejorar performance
- Expected: 5-day high-intensity routine with advanced exercises

#### 4. **Wellness Focus with Kael Coach**
- Experience Level: Principiante (Beginner)
- Coach: Kael
- Questionnaire: Sedentario, Back pain, Objetivo: Mejorar salud general
- Expected: 3-day balanced wellness routine, safe for injuries

#### 5. **Flexible Day Configurations**
- 1-day routine for busy users
- 6-day routine for dedicated athletes

### Quality Validations

Each generated routine is validated against:

✅ **Catalog Conformity**
- All exercise names match exactly with catalog
- No translations, abbreviations, or paraphrasing

✅ **Structure Validation**
- Minimum 4 exercises per day (main block)
- At least 6 digits sets/reps values
- Rest seconds properly configured

✅ **Uniqueness Rules**
- No exercise repeated in same day
- No exercise appears in more than 2 days in routine

✅ **Coach-Specific Rules**
- Cardio Coach: No bench press, bicep curls, deadlifts
- Calisthenics: Bodyweight focus (push-ups, planks, pull-ups)
- Muscle Building: Compound + isolation exercises
- Wellness: Balanced, low-impact exercises

✅ **Response Structure**
- Valid JSON response
- Required fields present
- Proper data types

### Running Model Tests

```bash
# Run all model tests
npm test -- test/functional/model-testing.spec.ts

# Or using npm script
npm run test:ronnie
```

### Expected Test Results

```
✅ Beginner Routine Generation
✅ Intermediate Routine Generation
✅ Advanced Routine Generation
✅ Wellness Routine Generation
✅ 1-Day Routine Generation
✅ 6-Day Routine Generation
✅ No Duplicate Exercises per Day
✅ Minimum 4 Exercises per Day
✅ All Exercises Match Catalog
✅ Valid Response Structure
✅ Required Fields in Exercises
✅ Test Summary
```

## 🛠️ Setup

### Prerequisites

1. All services running:
   ```bash
   docker-compose up -d
   ```

2. Dependencies installed:
   ```bash
   npm install
   ```

3. Environment configured (`.env.test`):
   ```env
   GATEWAY_BASE_URL=http://localhost:8080
   IFIT_BASE_URL=http://localhost:8081
   RONNIE_BASE_URL=http://localhost:8082
   KEYCLOAK_BASE_URL=http://localhost:9090
   
   DB_HOST=localhost
   DB_PORT=3306
   DB_ROOT_PASSWORD=root
   DB_NAME=ifit
   
   TEST_USER_EMAIL=test@example.com
   TEST_USER_PASSWORD=Test@1234
   ```

### Install Dependencies

```bash
# From test directory
npm install

# This will install mysql2 for database access
```

## 🚀 Running Tests

### All Tests
```bash
npm test
```

### Watch Mode
```bash
npm test -- --watch
```

### Specific Test File
```bash
npm test -- test/functional/auth-flow.spec.ts
npm test -- test/functional/model-testing.spec.ts
```

### Verbose Output
```bash
npm test -- --reporter=verbose
```

### With Coverage
```bash
npm run test:coverage
```

## 📊 Test Reports

Test output includes:
- ✅ Passed tests with timestamps
- ❌ Failed tests with error details
- ⏱️ Execution time per test
- 📝 Detailed logs for debugging

Example output:
```
✅ User registered successfully
   Email: test_1718962845123@example.com
   User ID: 42

✅ Verification code retrieved from DB
   Code: 123456
   Email: test_1718962845123@example.com

✅ Beginner routine generated with 3 days
   Day 1: 5 exercises
   Day 2: 5 exercises
   Day 3: 4 exercises
```

## 🔍 Debugging

### View Database Directly

```bash
# Access MySQL container
docker exec -it ifit-mysql mysql -u root -proot ifit

# Query users and verification codes
SELECT email, verification_code, is_verified, created_at FROM user ORDER BY created_at DESC;

# Query generated routines
SELECT id, user_id, title, days FROM routine ORDER BY created_at DESC;
```

### View Service Logs

```bash
# Gateway logs
docker-compose logs -f gateway

# iFit service logs
docker-compose logs -f ifit

# Ronnie service logs
docker-compose logs -f ronnie

# Keycloak logs
docker-compose logs -f keycloak
```

### Enable Debug Mode

```bash
DEBUG=true npm test
```

## 📋 Test Helpers

### Database Helpers (`utils/db-helper.ts`)

```typescript
// Get verification code
const code = await getVerificationCode('user@example.com');

// Get user data
const user = await getUserByEmail('user@example.com');

// Get all users
const users = await getAllUsers();

// Get exercise catalog
const exercises = await getExerciseCatalog();

// Get coach types
const coaches = await getCoachTypes();

// Get experience levels
const levels = await getExperienceLevels();
```

### HTTP Client (`utils/http-client.ts`)

```typescript
const client = createHttpClient('http://localhost:8080');

// Set bearer token
client.setTokens(accessToken, refreshToken);

// Make requests
const res = await client.post('/auth/login', { email, password });
const res = await client.get('/users/profile');
const res = await client.put('/users/profile', { ...data });
```

### Test Helpers (`utils/test-helpers.ts`)

```typescript
// Generate unique test email
const email = generateTestEmail(); // test_1718962845123@example.com

// Generate unique test name
const name = generateTestName(); // testuser_1718962845123

// Create test user via API
const user = await createTestUser(gatewayClient);

// Login user
const tokens = await loginUser(gatewayClient, email, password);

// Wait for condition
await waitUntil(() => condition, 5000);

// Validate response fields
validateResponseStructure(data, ['id', 'email', 'name']);
```

## 🎯 Common Issues & Solutions

### Issue: Verification Code is NULL

**Solution**: The register endpoint should generate the code. Check:
1. Email service is configured
2. User was created in ifit service, not just Keycloak
3. Wait 500ms after register before querying DB

```typescript
await new Promise(r => setTimeout(r, 500));
const code = await getVerificationCode(email);
```

### Issue: Tests Timeout

**Solution**: Services may be slow on first startup
1. Check all services are healthy: `docker-compose ps`
2. View service logs: `docker-compose logs -f`
3. Increase timeout: `API_TIMEOUT=20000 npm test`

### Issue: Database Connection Error

**Solution**: Ensure environment variables are correct
```bash
# Test connection
docker exec ifit-mysql mysql -u root -proot -e "SELECT 1"

# Check env variables
cat .env.test
```

### Issue: Auth Token Expires

**Solution**: Tokens are valid for ~5 minutes. If tests take longer:
1. Use refresh token to get new access token
2. Run tests more quickly
3. Increase token expiration in Keycloak config

## 📚 Documentation References

- [Vitest Documentation](https://vitest.dev)
- [Axios Documentation](https://axios-http.com)
- [MySQL2 Documentation](https://github.com/sidorares/node-mysql2)
- [iFit API Documentation](../../README.md)

## 👤 Author

Created for iFit TFG Project

**Email**: juangarcandon@gmail.com  
**Date**: 2026-06-22  
**Version**: 1.0

---

**Last Updated**: 2026-06-22
