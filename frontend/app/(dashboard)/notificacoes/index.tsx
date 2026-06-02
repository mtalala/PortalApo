import { APO_DECORATIVE_COLORS } from "@/packages/data/apoDecorativeColors";
import { Notification } from "@/packages/types/notification";
import { timeAgo } from "@/packages/utils/timeAgo";
import { MaterialIcons } from "@expo/vector-icons";
import React, { useEffect, useState } from "react";
import {
  Pressable,
  ScrollView,
  Text,
  useWindowDimensions,
  View,
} from "react-native";

/**
 * Gera um índice determinístico a partir do id da notificação/APO
 */
function getDecorativeColorById(id: string): string {
  let hash = 0;
  for (let i = 0; i < id.length; i++) {
    hash = id.charCodeAt(i) + ((hash << 5) - hash);
  }
  const index = Math.abs(hash) % APO_DECORATIVE_COLORS.length;
  return APO_DECORATIVE_COLORS[index];
}

export default function NotificationsScreen({ sidebarWidth = 256 }) {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const { width } = useWindowDimensions();

  const isMobile = width < 768;

  /**
   * CARREGA NOTIFICAÇÕES REAIS DO BACKEND (POSTGRES)
   */
  useEffect(() => {
    const load = async () => {
      try {
        const res = await fetch("/api/notifications");

        if (!res.ok) throw new Error("Erro ao buscar notificações");

        const data: Notification[] = await res.json();
        setNotifications(data);
      } catch (err) {
        console.error(err);
      }
    };

    load();
  }, []);

  const markAsRead = async (id: string) => {
    try {
      await fetch(`/api/notifications/${id}/read`, {
        method: "PATCH",
      });

      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, read: true } : n))
      );
    } catch (err) {
      console.error(err);
    }
  };

  const markAllAsRead = async () => {
    try {
      await fetch(`/api/notifications/read-all`, {
        method: "PATCH",
      });

      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
    } catch (err) {
      console.error(err);
    }
  };

  const getMessage = (role: Notification["role"]) =>
    role === "Aluno"
      ? "Você recebeu uma atualização na sua solicitação"
      : "Você recebeu uma solicitação de aprovação";

  const hasUnread = notifications.some((n) => !n.read);

  const MAX_WIDTH = 1024;

  const containerWidth = isMobile
    ? "100%"
    : Math.min(width - sidebarWidth - 32, MAX_WIDTH);

  return (
    <ScrollView contentContainerStyle={{ padding: 16 }}>
      {/* Botão marcar todas */}
      {hasUnread && (
        <Pressable
          onPress={markAllAsRead}
          style={{
            position: "absolute",
            top: 24,
            right: 24,
            zIndex: 30,
            padding: 8,
            borderRadius: 24,
          }}
        >
          <MaterialIcons name="done-all" size={24} color="#DC2626" />
        </Pressable>
      )}

      <View style={{ width: containerWidth, alignSelf: "center" }}>
        {/* HEADER */}
        <View style={{ marginBottom: 24 }}>
          <Text style={{ fontSize: 32, fontWeight: "700", color: "#111827" }}>
            Notificações
          </Text>
          <Text style={{ fontSize: 14, color: "#6b7280", marginTop: 4 }}>
            Acompanhe as atualizações das suas solicitações
          </Text>
        </View>

        {/* LISTA */}
        <View style={{ flexDirection: "column", gap: 12 }}>
          {notifications.map((n) => {
            const decorativeColor = getDecorativeColorById(n.id);

            return (
              <View
                key={n.id}
                style={{
                  flexDirection: "row",
                  backgroundColor: "#fff",
                  borderRadius: 12,
                  borderWidth: 1,
                  borderColor: "#e5e7eb",
                  padding: 12,
                  position: "relative",
                  overflow: "hidden",
                }}
              >
                {/* barra lateral */}
                <View
                  style={{
                    position: "absolute",
                    left: 0,
                    top: 0,
                    bottom: 0,
                    width: 4,
                    backgroundColor: decorativeColor,
                  }}
                />

                {/* conteúdo */}
                <View style={{ flex: 1, marginLeft: 8 }}>
                  <Text
                    style={{
                      fontSize: 14,
                      fontWeight: "600",
                      color: "#111827",
                    }}
                  >
                    {getMessage(n.role)}
                  </Text>

                  <Text
                    style={{
                      fontSize: 12,
                      color: "#6b7280",
                      marginTop: 2,
                    }}
                  >
                    {timeAgo(n.createdAt)} • {n.topic}
                  </Text>
                </View>

                {/* ações */}
                {!n.read && (
                  <View
                    style={{
                      marginLeft: 8,
                      alignItems: "center",
                      justifyContent: "center",
                      gap: 4,
                    }}
                  >
                    <View
                      style={{
                        width: 8,
                        height: 8,
                        borderRadius: 4,
                        backgroundColor: "#dc2626",
                      }}
                    />

                    <Pressable
                      onPress={() => markAsRead(n.id)}
                      style={{ padding: 4 }}
                    >
                      <MaterialIcons
                        name="remove-red-eye"
                        size={20}
                        color="#6b7280"
                      />
                    </Pressable>
                  </View>
                )}
              </View>
            );
          })}
        </View>
      </View>
    </ScrollView>
  );
}