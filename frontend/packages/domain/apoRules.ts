// src/domain/apoRules.ts
import type { Apo } from "@/packages/types/apo";
import type { User } from "@/packages/types/user";

export function canApproveApo(
  apo: Apo,
  user: User
): boolean {
  // aluno nunca aprova
  if (user.role === "aluno") return false;

  // evita aprovar duas vezes
  const alreadyApproved = apo.approvals.some(
    (a) =>
      String(a.userId) === String(user.id) &&
      a.role.toLowerCase() === user.role
  );
  if (alreadyApproved) return false;

  if (
    user.role === "orientador" &&
    apo.status === "PENDENTE_ORIENTADOR"
  ) {
    return true;
  }

  if (
    user.role === "comissao" &&
    apo.status === "PENDENTE_COMISSAO"
  ) {
    return true;
  }

  if (
    user.role === "coordenador" &&
    apo.status === "PENDENTE_COORDENACAO"
  ) {
    return true;
  }

  return false;
}
