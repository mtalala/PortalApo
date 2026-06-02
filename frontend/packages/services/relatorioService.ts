import AsyncStorage from "@react-native-async-storage/async-storage";
import { Platform } from "react-native";

import { BASE_URL } from "@/packages/services/api";

export type ReportRow = Record<string, unknown>;

async function authHeaders(): Promise<Record<string, string>> {
  const token = await AsyncStorage.getItem("token");
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export async function getRelatorio(endpoint: string): Promise<ReportRow[]> {
  const res = await fetch(`${BASE_URL}${endpoint}`, {
    headers: {
      "Content-Type": "application/json",
      ...(await authHeaders()),
    } as Record<string, string>,
  });

  if (!res.ok) {
    throw new Error((await res.text()) || "Erro ao carregar relatório");
  }

  return res.json();
}

export async function exportRelatorioPdf(endpoint: string, filename: string) {
  const separator = endpoint.includes("?") ? "&" : "?";
  const res = await fetch(`${BASE_URL}${endpoint}${separator}formato=pdf`, {
    headers: await authHeaders(),
  });

  if (!res.ok) {
    throw new Error((await res.text()) || "Erro ao exportar PDF");
  }

  const blob = await res.blob();

  if (Platform.OS === "web") {
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = filename;
    link.click();
    URL.revokeObjectURL(url);
    return;
  }

  throw new Error("Exportação PDF nativa exige integração com compartilhamento de arquivos.");
}
