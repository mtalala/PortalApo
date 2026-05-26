// src/types/user.ts
export type Role = "admin" | "aluno" | "coordenador" | "orientador" | "comissao";

export interface User {
  id: string;
  name: string;
  email?: string;
  role: Role;
}

export const ROLES: Role[] = ["admin", "aluno", "coordenador", "orientador", "comissao"];
