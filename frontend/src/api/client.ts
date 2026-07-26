import axios, { AxiosError, AxiosInstance, AxiosResponse } from 'axios';
import { LoginRequest, ApiResponse } from '@/types';

interface TokenResponse {
  accessToken: string;
  refreshToken: string;
}

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

let isRefreshing = false;
let failedQueue: (() => void)[] = [];

const processQueue = () => {
  failedQueue.forEach((callback) => callback());
  failedQueue = [];
};

apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

apiClient.interceptors.response.use(
  (response: AxiosResponse<ApiResponse<unknown>>) => response,
  async (error: AxiosError<ApiResponse<unknown>>) => {
    const originalRequest = error.config;

    if (!originalRequest) {
      return Promise.reject(error);
    }

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      if (isRefreshing) {
        return new Promise((resolve) => {
          failedQueue.push(() => {
            if (originalRequest.headers) {
              originalRequest.headers.Authorization = `Bearer ${localStorage.getItem('accessToken')}`;
            }
            resolve(apiClient(originalRequest));
          });
        });
      }

      isRefreshing = true;

      try {
        const refreshToken = localStorage.getItem('refreshToken');
        if (!refreshToken) {
          localStorage.clear();
          window.location.href = '/login';
          return Promise.reject(error);
        }

        const response = await apiClient.post<ApiResponse<TokenResponse>>('/api/v1/auth/refresh', {
          refreshToken,
        });

        const { accessToken, refreshToken: newRefreshToken } = response.data.data;

        localStorage.setItem('accessToken', accessToken);
        if (newRefreshToken) {
          localStorage.setItem('refreshToken', newRefreshToken);
        }

        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        }

        processQueue();
        return apiClient(originalRequest);
      } catch (refreshError) {
        processQueue();
        localStorage.clear();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

export const authApi = {
  login: (credentials: LoginRequest): Promise<ApiResponse<TokenResponse>> => {
    return apiClient
      .post<ApiResponse<TokenResponse>>('/api/v1/auth/login', credentials)
      .then((response) => response.data);
  },

  refresh: (refreshToken: string): Promise<ApiResponse<TokenResponse>> => {
    return apiClient
      .post<ApiResponse<TokenResponse>>('/api/v1/auth/refresh', { refreshToken })
      .then((response) => response.data);
  },

  logout: (): Promise<ApiResponse<void>> => {
    return apiClient
      .post<ApiResponse<void>>('/api/v1/auth/logout')
      .finally(() => {
        localStorage.clear();
      });
  },
};

export const healthApi = {
  check: (): Promise<ApiResponse<{ status: string }>> => {
    return apiClient
      .get<ApiResponse<{ status: string }>>('/actuator/health')
      .then((response) => response.data);
  },
};

export default apiClient;