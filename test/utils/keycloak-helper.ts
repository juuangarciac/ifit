import { HttpClient } from './http-client';

/**
 * Helper para obtener tokens desde Keycloak directamente
 */
export async function getKeycloakToken(
  keycloakClient: HttpClient,
  username: string,
  password: string
): Promise<{
  access_token: string;
  refresh_token: string;
  expires_in: number;
}> {
  const clientId = process.env.KEYCLOAK_CLIENT_ID || 'springboot-ifit-client';
  const clientSecret = process.env.KEYCLOAK_CLIENT_SECRET;

  const params = new URLSearchParams();
  params.append('grant_type', 'password');
  params.append('client_id', clientId);
  if (clientSecret) {
    params.append('client_secret', clientSecret);
  }
  params.append('username', username);
  params.append('password', password);

  const res = await keycloakClient.post(
    '/realms/ifit-realm/protocol/openid-connect/token',
    params.toString(),
    {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
    }
  );

  if (res.status !== 200) {
    throw new Error(
      `Keycloak token request failed: ${res.status} - ${JSON.stringify(res.data)}`
    );
  }

  return res.data;
}

/**
 * Revocar token en Keycloak
 */
export async function revokeKeycloakToken(
  keycloakClient: HttpClient,
  refreshToken: string
): Promise<void> {
  const clientId = process.env.KEYCLOAK_CLIENT_ID || 'springboot-ifit-client';
  const clientSecret = process.env.KEYCLOAK_CLIENT_SECRET;

  const params = new URLSearchParams();
  params.append('client_id', clientId);
  if (clientSecret) {
    params.append('client_secret', clientSecret);
  }
  params.append('refresh_token', refreshToken);

  const res = await keycloakClient.post(
    '/realms/ifit-realm/protocol/openid-connect/logout',
    params.toString(),
    {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
    }
  );

  if (res.status !== 204 && res.status !== 200) {
    console.warn(`Keycloak token revocation returned: ${res.status}`);
  }
}
