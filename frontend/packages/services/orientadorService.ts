import request from './api';

export interface Orientador {
  id: number;
  nome: string;
  email?: string;
  ativo?: boolean;
}

export const orientadorService = {
  getAll: async (): Promise<Orientador[]> => {
    return request<Orientador[]>('/orientadores');
  },
};
