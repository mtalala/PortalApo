// src/types/apo.ts
import type { ApoStatus } from "@/packages/domain/apoStatus";
import type { Role } from "./user";

export interface ApoApproval {
  userId: string;
  role: Role | Uppercase<Role> | string;
  approved: boolean;
}

export interface Apo {
  id: string;
  codigoApo: string;
  nome: string;
  matricula: string;
  program: string;
  semestre: string;
  orientador: string;
  coordenador: string;

  status: ApoStatus;

  /**
   * Data de conclusão da APO.
   * Presente apenas quando o status é final (APROVADA ou REJEITADA).
   */
  completedAt?: string;

  activities: {
    label: string;
    points: number;
  }[];

  totalPoints: number;

  files: {
    name: string;
    url: string;
  }[];

  approvals: ApoApproval[];

  requiredCommissionApprovals: number;

  orientadorUserIds?: number[];
  orientadorUsernames?: string[];
  coordenadorUserId?: number;
}
