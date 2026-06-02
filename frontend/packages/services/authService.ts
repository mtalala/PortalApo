import AsyncStorage from '@react-native-async-storage/async-storage';
import request from './api';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  id: number;
  username: string;
  token: string;
  role: string;
  mustChangePassword: boolean;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface ChangePasswordResponse {
  id: number;
  username: string;
  role: string;
  mustChangePassword: boolean;
}

export const authService = {
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const response = await request<LoginResponse>('/auth/login', {
      method: 'POST',
      body: JSON.stringify(credentials),
    });

    await AsyncStorage.setItem('token', response.token);
    await AsyncStorage.setItem('role', response.role);
    await AsyncStorage.setItem('userId', String(response.id));
    await AsyncStorage.setItem('username', response.username);
    await AsyncStorage.setItem('mustChangePassword', String(response.mustChangePassword));

    return response;
  },

  changePassword: async (payload: ChangePasswordRequest): Promise<ChangePasswordResponse> => {
    const response = await request<ChangePasswordResponse>('/auth/change-password', {
      method: 'POST',
      body: JSON.stringify(payload),
    });

    await AsyncStorage.setItem('mustChangePassword', String(response.mustChangePassword));

    return response;
  },

  logout: async () => {
    await AsyncStorage.removeItem('token');
    await AsyncStorage.removeItem('role');
    await AsyncStorage.removeItem('userId');
    await AsyncStorage.removeItem('username');
    await AsyncStorage.removeItem('mustChangePassword');
  },

  getToken: async () => {
    return AsyncStorage.getItem('token');
  },
};
