import { Platform } from "react-native";
import AsyncStorage from "@react-native-async-storage/async-storage";

const LOCAL_API_HOST = Platform.OS === "android" ? "10.0.2.2" : "localhost";
export const BASE_URL =
  process.env.EXPO_PUBLIC_API_URL ?? `http://${LOCAL_API_HOST}:8080/api`;

async function getToken(): Promise<string | null> {
  return AsyncStorage.getItem("token");
}

export async function request<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const token = await getToken();

  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(options.headers as Record<string, string>),
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const res = await fetch(`${BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  if (!res.ok) {
    const error = await res.text();
    throw new Error(error || "API Error");
  }

  return res.json();
}

export default request;
