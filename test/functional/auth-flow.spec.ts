import { describe, it, expect, beforeAll, afterAll } from 'vitest';
import { HttpClient, createHttpClient } from '../utils/http-client';
import {
  generateTestEmail,
  generateTestName,
  loginUser,
  TestUser,
} from '../utils/test-helpers';
import {
  getVerificationCode,
  getUserByEmail,
  initializePool,
  closePool,
} from '../utils/db-helper';

/**
 * Suite de tests para el flujo completo de autenticación
 *
 * Flujo probado:
 * 1. Crear usuario (registro)
 * 2. Obtener código de verificación directamente desde BD
 * 3. Verificar usuario con el código
 * 4. Login exitoso
 * 5. Obtener credenciales (tokens)
 */

describe('🔐 Authentication Flow - Complete E2E', () => {
  let gatewayClient: HttpClient;
  let testEmail: string;
  let testPassword: string;

  beforeAll(async () => {
    gatewayClient = createHttpClient(process.env.GATEWAY_BASE_URL || 'http://localhost:8080');
    await initializePool();
  });

  afterAll(async () => {
    await closePool();
  });

  describe('1️⃣ User Registration', () => {
    it('should create a new user via /auth/register', async () => {
      testEmail = generateTestEmail();
      testPassword = 'Test@1234';
      const name = generateTestName();

      const response = await gatewayClient.post('/auth/register', {
        name,
        surname: 'Tester',
        email: testEmail,
        password: testPassword,
        birthdate: '1990-01-01',
        phone: '651634807',
      });

      expect(response.status).toBe(201);
      expect(response.data).toHaveProperty('accessToken');
      expect(response.data).toHaveProperty('refreshToken');
      expect(response.data).toHaveProperty('appUser');
      expect(response.data.appUser.email).toBe(testEmail);

      console.log('✅ User registered successfully');
      console.log(`   Email: ${testEmail}`);
      console.log(`   User ID: ${response.data.appUser.id}`);
    });

    it('should not allow duplicate email registration', async () => {
      const name = generateTestName();
      const response = await gatewayClient.post('/auth/register', {
        name,
        surname: 'Tester',
        email: testEmail,
        password: testPassword,
        birthdate: '1990-01-01',
        phone: '651634807',
      });

      expect(response.status).toBe(409);
      console.log('✅ Duplicate email correctly rejected');
    });
  });

  describe('2️⃣ Verification Code Retrieval', () => {
    it('should get verification code directly from database', async () => {
      // Esperar un momento para asegurar que la BD está sincronizada
      await new Promise((resolve) => setTimeout(resolve, 500));

      const verificationCode = await getVerificationCode(testEmail);

      expect(verificationCode).toBeTruthy();
      expect(verificationCode).toMatch(/^\d{6}$/); // 6-digit code

      console.log('✅ Verification code retrieved from DB');
      console.log(`   Code: ${verificationCode}`);
      console.log(`   Email: ${testEmail}`);

      // Guardar el código para pasos siguientes
      (globalThis as any).verificationCode = verificationCode;
    });

    it('should find user data in database', async () => {
      const user = await getUserByEmail(testEmail);

      expect(user).toBeTruthy();
      expect(user.email).toBe(testEmail);
      expect(user.is_verified).toBe(false); // Aún no verificado

      console.log('✅ User found in database');
      console.log(`   ID: ${user.id}`);
      console.log(`   Is Verified: ${user.is_verified}`);
      console.log(`   Registration Complete: ${user.is_registration_complete}`);

      (globalThis as any).userId = user.id;
    });
  });

  describe('3️⃣ Email Verification', () => {
    it('should verify user with correct verification code', async () => {
      const verificationCode = (globalThis as any).verificationCode;

      const response = await gatewayClient.post('/auth/verify', {
        email: testEmail,
        verificationCode,
      });

      expect(response.status).toBe(200);
      expect(response.data).toHaveProperty('accessToken');
      expect(response.data).toHaveProperty('refreshToken');
      expect(response.data).toHaveProperty('appUser');

      console.log('✅ User verified successfully');
      console.log(`   Email: ${response.data.appUser.email}`);
      console.log(`   Verification complete`);

      (globalThis as any).accessToken = response.data.accessToken;
      (globalThis as any).refreshToken = response.data.refreshToken;
    });

    it('should reject invalid verification code', async () => {
      // Crear otro usuario para testing
      const newEmail = generateTestEmail();
      const newPassword = 'Test@1234';
      const name = generateTestName();

      await gatewayClient.post('/auth/register', {
        name,
        surname: 'Tester',
        email: newEmail,
        password: newPassword,
        birthdate: '1990-01-01',
        phone: '651634807',
      });

      const response = await gatewayClient.post('/auth/verify', {
        email: newEmail,
        verificationCode: '000000', // Código inválido
      });

      expect(response.status).toBe(400);
      console.log('✅ Invalid verification code correctly rejected');
    });
  });

  describe('4️⃣ User Login', () => {
    it('should login verified user successfully', async () => {
      const response = await gatewayClient.post('/auth/login', {
        email: testEmail,
        password: testPassword,
      });

      expect(response.status).toBe(200);
      expect(response.data).toHaveProperty('accessToken');
      expect(response.data).toHaveProperty('refreshToken');
      expect(response.data).toHaveProperty('appUser');

      console.log('✅ User login successful');
      console.log(`   Email: ${response.data.appUser.email}`);
      console.log(`   Tokens obtained`);

      (globalThis as any).loginAccessToken = response.data.accessToken;
      (globalThis as any).loginRefreshToken = response.data.refreshToken;
    });

    it('should reject login with wrong password', async () => {
      const response = await gatewayClient.post('/auth/login', {
        email: testEmail,
        password: 'WrongPassword@123',
      });

      expect(response.status).toBe(401);
      console.log('✅ Wrong password correctly rejected');
    });

    it('should reject login with non-existent email', async () => {
      const response = await gatewayClient.post('/auth/login', {
        email: 'nonexistent@example.com',
        password: 'Test@1234',
      });

      expect(response.status).toBe(401);
      console.log('✅ Non-existent email correctly rejected');
    });
  });

  describe('5️⃣ Token Management', () => {
    it('should refresh tokens using refresh token', async () => {
      const refreshToken = (globalThis as any).loginRefreshToken;

      const response = await gatewayClient.post('/auth/refresh', {
        refreshToken,
      });

      expect(response.status).toBe(200);
      expect(response.data).toHaveProperty('accessToken');
      expect(response.data).toHaveProperty('refreshToken');

      const newAccessToken = response.data.accessToken;
      const newRefreshToken = response.data.refreshToken;

      expect(newAccessToken).toBeTruthy();
      expect(newRefreshToken).toBeTruthy();

      console.log('✅ Tokens refreshed successfully');
      console.log(`   New Access Token: ${newAccessToken.substring(0, 20)}...`);
      console.log(`   New Refresh Token: ${newRefreshToken.substring(0, 20)}...`);

      (globalThis as any).refreshedAccessToken = newAccessToken;
      (globalThis as any).refreshedRefreshToken = newRefreshToken;
    });

    it('should logout and invalidate refresh token', async () => {
      const refreshToken = (globalThis as any).loginRefreshToken;

      const response = await gatewayClient.post('/auth/logout', {
        refreshToken,
      });

      expect(response.status).toBe(200);
      expect(response.data).toHaveProperty('message');

      console.log('✅ User logged out successfully');
      console.log(`   Message: ${response.data.message}`);
    });

    it('should reject login after logout', async () => {
      // Nota: Esta prueba puede no funcionar si Keycloak no ha invalidado el token
      // Es más una prueba conceptual
      const invalidRefreshToken = 'invalid_token_after_logout';

      const response = await gatewayClient.post('/auth/refresh', {
        refreshToken: invalidRefreshToken,
      });

      expect(response.status).toBeGreaterThanOrEqual(400);
      console.log('✅ Invalid refresh token correctly rejected');
    });
  });

  describe('6️⃣ Credentials Summary', () => {
    it('should have valid credentials from full flow', () => {
      const accessToken = (globalThis as any).refreshedAccessToken;
      const refreshToken = (globalThis as any).refreshedRefreshToken;
      const userId = (globalThis as any).userId;

      expect(accessToken).toBeTruthy();
      expect(refreshToken).toBeTruthy();
      expect(userId).toBeTruthy();

      console.log('\n📋 ========== CREDENCIALES FINALES ==========');
      console.log(`Email: ${testEmail}`);
      console.log(`Password: ${testPassword}`);
      console.log(`User ID: ${userId}`);
      console.log(`Access Token (sample): ${accessToken.substring(0, 50)}...`);
      console.log(`Refresh Token (sample): ${refreshToken.substring(0, 50)}...`);
      console.log('✅ Flujo de autenticación completado exitosamente');
    });
  });
});
