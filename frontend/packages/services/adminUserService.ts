import request from './api';

export interface AdminUser {
  id: number;
  username: string;
  password: string;
  role: string;
  ativo: boolean;
  orientadorUserIds?: number[];
  pessoa?: any;
}

export const adminUserService = {
  getAll: async (): Promise<AdminUser[]> => {
    return request<AdminUser[]>('/users');
  },

  create: async (user: Omit<AdminUser, 'id'>): Promise<AdminUser> => {
    return request<AdminUser>('/users', {
      method: 'POST',
      body: JSON.stringify(user),
    });
  },

  update: async (id: string, user: Partial<AdminUser>): Promise<AdminUser> => {
    return request<AdminUser>(`/users/${id}`, {
      method: 'PUT',
      body: JSON.stringify(user),
    });
  },

  delete: async (id: string): Promise<void> => {
    return request<void>(`/users/${id}`, {
      method: 'DELETE',
    });
  },
};