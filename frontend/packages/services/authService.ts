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

    return response;
  },

  logout: async () => {
    await AsyncStorage.removeItem('token');
    await AsyncStorage.removeItem('role');
    await AsyncStorage.removeItem('userId');
    await AsyncStorage.removeItem('username');
  },

  getToken: async () => {
    return AsyncStorage.getItem('token');
  },
};
