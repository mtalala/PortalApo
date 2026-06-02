// src/domain/apoVisualStatus.ts
import type { ApoStatus } from "./apoStatus";
import type { Apo } from "@/packages/types/apo";
import type { User } from "@/packages/types/user";

export type ApoVisualStatus =
  | "PENDENTE"
  | "EM_ANDAMENTO"
  | "CONCLUIDA";

export function getApoVisualStatus(
  status: ApoStatus
): ApoVisualStatus {
  switch (status) {
    case "PENDENTE_ORIENTADOR":
      return "PENDENTE";

    case "PENDENTE_COMISSAO":
    case "PENDENTE_COORDENACAO":
      return "EM_ANDAMENTO";

    case "APROVADA":
    case "REJEITADA":
      return "CONCLUIDA";
  }
}

export function getApoVisualStatusForUser(
  apo: Apo,
  user: User
): ApoVisualStatus {
  const hasUserReview = apo.approvals.some(
    (approval) =>
      String(approval.userId) === String(user.id) &&
      approval.role.toLowerCase() === user.role
  );

  if (
    apo.status === "PENDENTE_ORIENTADOR" &&
    user.role === "orientador"
  ) {
    return hasUserReview ? "EM_ANDAMENTO" : "PENDENTE";
  }

  if (
    apo.status === "PENDENTE_COMISSAO" &&
    user.role === "comissao"
  ) {
    return hasUserReview ? "EM_ANDAMENTO" : "PENDENTE";
  }

  if (apo.status === "PENDENTE_COORDENACAO" && user.role === "coordenador") {
    return hasUserReview ? "EM_ANDAMENTO" : "PENDENTE";
  }

  return getApoVisualStatus(apo.status);
}
