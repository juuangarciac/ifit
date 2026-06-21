import { HttpClient } from './http-client';

/**
 * Generar email único para tests
 */
export function generateTestEmail(): string {
  return `test_${Date.now()}@example.com`;
}

/**
 * Generar nombre único para tests
 */
export function generateTestName(): string {
  return `testuser_${Date.now()}`;
}

/**
 * Interface para datos de usuario creado en tests
 */
export interface TestUser {
  id: number;
  email: string;
  name: string;
  password: string;
  accessToken: string;
  refreshToken: string;
}

/**
 * Crear usuario de prueba via API Gateway
 */
export async function createTestUser(
  gatewayClient: HttpClient
): Promise<TestUser> {
  const email = generateTestEmail();
  const name = generateTestName();
  const password = 'Test@1234';

  const res = await gatewayClient.post('/auth/register', {
    name,
    surname: 'Tester',
    email,
    password,
    birthdate: '1990-01-01',
    phone: '651634807',
  });

  if (res.status !== 201) {
    throw new Error(
      `Failed to create test user: ${res.status} - ${JSON.stringify(res.data)}`
    );
  }

  const { accessToken, refreshToken, appUser } = res.data;

  return {
    id: appUser.id,
    email,
    name,
    password,
    accessToken,
    refreshToken,
  };
}

/**
 * Login de usuario y obtener tokens
 */
export async function loginUser(
  gatewayClient: HttpClient,
  email: string,
  password: string
): Promise<{ accessToken: string; refreshToken: string; userId: number }> {
  const res = await gatewayClient.post('/auth/login', {
    email,
    password,
  });

  if (res.status !== 200) {
    throw new Error(
      `Failed to login: ${res.status} - ${JSON.stringify(res.data)}`
    );
  }

  const { accessToken, refreshToken, appUser } = res.data;

  return {
    accessToken,
    refreshToken,
    userId: appUser.id,
  };
}

/**
 * Esperar a que una condición sea verdadera
 */
export async function waitUntil(
  condition: () => boolean,
  timeout: number = 5000
): Promise<void> {
  const start = Date.now();

  while (!condition()) {
    if (Date.now() - start > timeout) {
      throw new Error(`Timeout waiting for condition after ${timeout}ms`);
    }
    await new Promise((resolve) => setTimeout(resolve, 100));
  }
}

/**
 * Validar estructura de respuesta
 */
export function validateResponseStructure(
  data: any,
  requiredFields: string[]
): void {
  const missingFields = requiredFields.filter((field) => !(field in data));

  if (missingFields.length > 0) {
    throw new Error(
      `Missing required fields in response: ${missingFields.join(', ')}`
    );
  }
}
