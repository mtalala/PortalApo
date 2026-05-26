"use client";

import AsyncStorage from "@react-native-async-storage/async-storage";
import { authService } from "@/packages/services/authService";
import type { Role, User } from "@/packages/types/user";
import { createContext, ReactNode, useContext, useEffect, useState } from "react";

interface UserContextData {
  user: User | null;
  role: string | null;
  login: (username: string, password: string) => Promise<boolean>;
  logout: () => void;
  isAuthenticated: boolean;
}

const UserContext = createContext<UserContextData | undefined>(undefined);

const normalizeRole = (role: string): Role => role.toLowerCase() as Role;

export function UserProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    let isMounted = true;

    async function restoreSession() {
      const [token, role, username, userId] = await Promise.all([
        AsyncStorage.getItem("token"),
        AsyncStorage.getItem("role"),
        AsyncStorage.getItem("username"),
        AsyncStorage.getItem("userId"),
      ]);

      if (!isMounted) return;

      if (token && role) {
        setUser({
          id: userId ?? username ?? "unknown",
          name: username ?? "User",
          role: normalizeRole(role),
        });
        setIsAuthenticated(true);
      }
    }

    restoreSession();

    return () => {
      isMounted = false;
    };
  }, []);

  const login = async (username: string, password: string): Promise<boolean> => {
    try {
      const response = await authService.login({ username, password });
      const role = normalizeRole(response.role);
      const userId = String(response.id);

      await Promise.all([
        AsyncStorage.setItem("role", role),
        AsyncStorage.setItem("username", response.username ?? username),
        AsyncStorage.setItem("userId", userId),
      ]);

      setUser({ id: userId, name: response.username ?? username, role });
      setIsAuthenticated(true);
      return true;
    } catch (error) {
      return false;
    }
  };

  const logout = () => {
    Promise.all([
      AsyncStorage.removeItem("token"),
      AsyncStorage.removeItem("role"),
      AsyncStorage.removeItem("username"),
      AsyncStorage.removeItem("userId"),
    ]);
    setUser(null);
    setIsAuthenticated(false);
  };

  return (
    <UserContext.Provider
      value={{
        user,
        role: user?.role ?? null,
        login,
        logout,
        isAuthenticated,
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
