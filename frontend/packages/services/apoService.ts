import request from "@/packages/services/api";
import type { Apo } from "@/packages/types/apo";
import type { Role } from "@/packages/types/user";

const APO_PATH = "/apos";

export async function getApos(): Promise<Apo[]> {
  return request<Apo[]>(APO_PATH);
}

export async function getApoById(id: string): Promise<Apo> {
  return request<Apo>(`${APO_PATH}/${id}`);
}

export async function createApo(apo: Omit<Apo, "id">): Promise<Apo> {
  return request<Apo>(APO_PATH, {
    method: "POST",
    body: JSON.stringify(apo),
  });
}

export async function approveApo(id: string, role: Role): Promise<Apo> {
  switch (role) {
    case "orientador":
      return request<Apo>(`${APO_PATH}/${id}/aprovar-orientador`, {
        method: "POST",
      });

    case "comissao":
      return request<Apo>(`${APO_PATH}/${id}/aprovar-comissao`, {
        method: "POST",
      });

    case "coordenador":
      return request<Apo>(`${APO_PATH}/${id}/aprovar-coordenador`, {
        method: "POST",
      });

    default:
      throw new Error(`Role inválida para aprovação: ${role}`);
  }
}

export async function rejectApo(id: string, justificativa: string): Promise<Apo> {
  return request<Apo>(`${APO_PATH}/${id}/rejeitar`, {
    method: "POST",
    body: JSON.stringify({ justificativa }),
  });
}

export async function deleteApo(id: string): Promise<void> {
  return request<void>(`${APO_PATH}/${id}`, {
    method: "DELETE",
  });
}

export async function updateApo(apo: Apo): Promise<Apo> {
  return request<Apo>(`${APO_PATH}/${apo.id}`, {
    method: "PUT",
    body: JSON.stringify(apo),
  });
}
