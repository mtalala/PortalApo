"use client";

import AsyncStorage from "@react-native-async-storage/async-storage";
import { authService } from "@/packages/services/authService";
import type { Role, User } from "@/packages/types/user";
import { createContext, ReactNode, useContext, useEffect, useState } from "react";

interface UserContextData {
  user: User | null;
  role: string | null;
  login: (username: string, password: string) => Promise<{ success: boolean; mustChangePassword: boolean }>;
  changePassword: (currentPassword: string, newPassword: string) => Promise<{ success: boolean; message?: string }>;
  forceChangePassword: (newPassword: string) => Promise<{ success: boolean; message?: string }>;
  logout: () => void;
  isAuthenticated: boolean;
  mustChangePassword: boolean;
}

const UserContext = createContext<UserContextData | undefined>(undefined);

const normalizeRole = (role: string): Role => role.toLowerCase() as Role;

export function UserProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [mustChangePassword, setMustChangePassword] = useState(false);

  useEffect(() => {
    let isMounted = true;

    async function restoreSession() {
      const [token, role, username, userId, storedMustChangePassword] = await Promise.all([
        AsyncStorage.getItem("token"),
        AsyncStorage.getItem("role"),
        AsyncStorage.getItem("username"),
        AsyncStorage.getItem("userId"),
        AsyncStorage.getItem("mustChangePassword"),
      ]);

      if (!isMounted) return;

      if (token && role) {
        const needsPasswordChange = storedMustChangePassword === "true";
        setUser({
          id: userId ?? username ?? "unknown",
          name: username ?? "User",
          role: normalizeRole(role),
          mustChangePassword: needsPasswordChange,
        });
        setMustChangePassword(needsPasswordChange);
        setIsAuthenticated(true);
      }
    }

    restoreSession();

    return () => {
      isMounted = false;
    };
  }, []);

  const login = async (
    username: string,
    password: string
  ): Promise<{ success: boolean; mustChangePassword: boolean }> => {
    try {
      const response = await authService.login({ username, password });
      const role = normalizeRole(response.role);
      const userId = String(response.id);
      const needsPasswordChange = response.mustChangePassword;

      await Promise.all([
        AsyncStorage.setItem("role", role),
        AsyncStorage.setItem("username", response.username ?? username),
        AsyncStorage.setItem("userId", userId),
        AsyncStorage.setItem("mustChangePassword", String(needsPasswordChange)),
      ]);

      setUser({
        id: userId,
        name: response.username ?? username,
        role,
        mustChangePassword: needsPasswordChange,
      });
      setMustChangePassword(needsPasswordChange);
      setIsAuthenticated(true);
      return { success: true, mustChangePassword: needsPasswordChange };
    } catch (error) {
      return { success: false, mustChangePassword: false };
    }
  };

  const changePassword = async (
    currentPassword: string,
    newPassword: string
  ): Promise<{ success: boolean; message?: string }> => {
    try {
      const response = await authService.changePassword({ currentPassword, newPassword });
      const role = normalizeRole(response.role);
      const userId = String(response.id);

      await Promise.all([
        AsyncStorage.setItem("role", role),
        AsyncStorage.setItem("username", response.username),
        AsyncStorage.setItem("userId", userId),
        AsyncStorage.setItem("mustChangePassword", String(response.mustChangePassword)),
      ]);

      setUser({
        id: userId,
        name: response.username,
        role,
        mustChangePassword: response.mustChangePassword,
      });
      setMustChangePassword(response.mustChangePassword);
      setIsAuthenticated(true);
      return { success: true };
    } catch (error) {
      return {
        success: false,
        message: error instanceof Error ? error.message : "Não foi possível alterar a senha",
      };
    }
  };

  const forceChangePassword = async (
    newPassword: string
  ): Promise<{ success: boolean; message?: string }> => {
    try {
      const response = await authService.forceChangePassword({ newPassword });
      const role = normalizeRole(response.role);
      const userId = String(response.id);

      await Promise.all([
        AsyncStorage.setItem("role", role),
        AsyncStorage.setItem("username", response.username),
        AsyncStorage.setItem("userId", userId),
        AsyncStorage.setItem("mustChangePassword", String(response.mustChangePassword)),
      ]);

      setUser({
        id: userId,
        name: response.username,
        role,
        mustChangePassword: response.mustChangePassword,
      });
      setMustChangePassword(response.mustChangePassword);
      setIsAuthenticated(true);
      return { success: true };
    } catch (error) {
      return {
        success: false,
        message: error instanceof Error ? error.message : "Não foi possível alterar a senha",
      };
    }
  };

  const logout = () => {
    Promise.all([
      AsyncStorage.removeItem("token"),
      AsyncStorage.removeItem("role"),
      AsyncStorage.removeItem("username"),
      AsyncStorage.removeItem("userId"),
      AsyncStorage.removeItem("mustChangePassword"),
    ]);
    setUser(null);
    setMustChangePassword(false);
    setIsAuthenticated(false);
  };

  return (
    <UserContext.Provider
      value={{
        user,
        role: user?.role as string | null ?? null,
        login,
        changePassword,
        forceChangePassword,
        logout,
        isAuthenticated,
        mustChangePassword,
      }}
    >
      {children}
    </UserContext.Provider>
  );
}

export function useUser() {
  const context = useContext(UserContext);
  if (!context) {
    throw new Error("useUser must be used within a UserProvider");
  }
  return context;
}
