import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';

/**
 * HTTP Client personalizado para tests
 * Incluye manejo de errores, headers por defecto, y logging
 */
export class HttpClient {
  private client: AxiosInstance;
  private baseURL: string;
  private accessToken?: string;
  private refreshToken?: string;

  constructor(baseURL: string) {
    this.baseURL = baseURL;
    this.client = axios.create({
      baseURL,
      timeout: parseInt(process.env.API_TIMEOUT || '10000'),
      validateStatus: () => true, // No lanzar error para ningún status
    });
  }

  /**
   * GET request
   */
  async get<T = any>(
    path: string,
    config?: AxiosRequestConfig
  ): Promise<{ status: number; data: T; headers: any }> {
    try {
      const response = await this.client.get<T>(path, this.getConfig(config));
      return {
        status: response.status,
        data: response.data,
        headers: response.headers,
      };
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * POST request
   */
  async post<T = any>(
    path: string,
    body?: any,
    config?: AxiosRequestConfig
  ): Promise<{ status: number; data: T; headers: any }> {
    try {
      const response = await this.client.post<T>(
        path,
        body,
        this.getConfig(config)
      );
      return {
        status: response.status,
        data: response.data,
        headers: response.headers,
      };
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * PUT request
   */
  async put<T = any>(
    path: string,
    body?: any,
    config?: AxiosRequestConfig
  ): Promise<{ status: number; data: T }> {
    try {
      const response = await this.client.put<T>(
        path,
        body,
        this.getConfig(config)
      );
      return {
        status: response.status,
        data: response.data,
      };
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * DELETE request
   */
  async delete<T = any>(
    path: string,
    config?: AxiosRequestConfig
  ): Promise<{ status: number; data: T }> {
    try {
      const response = await this.client.delete<T>(path, this.getConfig(config));
      return {
        status: response.status,
        data: response.data,
      };
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Setear tokens
   */
  setTokens(accessToken: string, refreshToken?: string) {
    this.accessToken = accessToken;
    if (refreshToken) {
      this.refreshToken = refreshToken;
    }
  }

  /**
   * Limpiar tokens
   */
  clearTokens() {
    this.accessToken = undefined;
    this.refreshToken = undefined;
  }

  /**
   * Obtener access token
   */
  getAccessToken(): string | undefined {
    return this.accessToken;
  }

  /**
   * Obtener refresh token
   */
  getRefreshToken(): string | undefined {
    return this.refreshToken;
  }

  /**
   * Construir config con headers por defecto
   */
  private getConfig(config?: AxiosRequestConfig): AxiosRequestConfig {
    const headers: any = {
      'Content-Type': 'application/json',
      ...config?.headers,
    };

    if (this.accessToken) {
      headers['Authorization'] = `Bearer ${this.accessToken}`;
    }

    return {
      ...config,
      headers,
    };
  }

  /**
   * Manejar errores
   */
  private handleError(error: any): Error {
    if (axios.isAxiosError(error)) {
      const message = `${error.config?.method?.toUpperCase()} ${error.config?.url} - Status: ${error.response?.status}`;
      return new Error(message);
    }
    return error;
  }
}

/**
 * Crear instancia de cliente HTTP
 */
export function createHttpClient(baseURL: string): HttpClient {
  return new HttpClient(baseURL);
}
