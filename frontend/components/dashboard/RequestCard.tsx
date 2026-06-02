// src/components/dashboard/RequestCard.tsx
import { APO_DECORATIVE_COLORS } from "@/packages/data/apoDecorativeColors";
import { getApoVisualStatus, getApoVisualStatusForUser } from "@/packages/domain/apoVisualStatus";
import { getApoVisualStatusColor } from "@/packages/domain/apoVisualStatusColor";
import { getApoVisualStatusLabel } from "@/packages/domain/apoVisualStatusLabel";
import { router } from "expo-router";
import type { Apo } from "@/packages/types/apo";
import type { User } from "@/packages/types/user";
import React from "react";
import { StyleSheet, Text, TouchableOpacity, View } from "react-native";

interface Props {
  apo?: Apo;
  currentUser?: User;
  onPress?: () => void;
}

/**
 * Gera cor decorativa determinística a partir do id da APO
 */
function getDecorativeColorByApoId(id: string): string {
  let hash = 0;
  for (let i = 0; i < id.length; i++) {
    hash = id.charCodeAt(i) + ((hash << 5) - hash);
  }
  const index = Math.abs(hash) % APO_DECORATIVE_COLORS.length;
  return APO_DECORATIVE_COLORS[index];
}

export default function RequestCard({ apo, currentUser, onPress }: Props) {
  if (!apo) return null;

  const visualStatus = currentUser
    ? getApoVisualStatusForUser(apo, currentUser)
    : getApoVisualStatus(apo.status);
  const statusLabel = getApoVisualStatusLabel(visualStatus);
  const statusColor = getApoVisualStatusColor(visualStatus);
  const decorativeColor = getDecorativeColorByApoId(apo.id);

  const mainActivityLabel =
    apo.activities.length > 0 ? apo.activities[0].label : "Atividade não informada";

  const handlePress = onPress
    ? onPress
    : () => router.push({ pathname: "/apos/[id]", params: { id: apo.id } });

  return (
    <TouchableOpacity style={styles.card} onPress={handlePress} activeOpacity={0.8}>
      {/* Barra decorativa */}
      <View style={[styles.decorativeBar, { backgroundColor: decorativeColor }]}>
        <Text style={[styles.statusLabel, { backgroundColor: statusColor, color: "#fff" }]}>
          {statusLabel}
        </Text>
      </View>

      {/* Conteúdo */}
      <View style={styles.content}>
        <Text style={styles.apoLabel}>APO</Text>
        <Text style={styles.activity}>{mainActivityLabel}</Text>
        <Text style={styles.student}>
          {apo.nome} — {apo.program}
        </Text>
      </View>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  card: {
    borderRadius: 12,
    borderWidth: 1,
    borderColor: "#E5E7EB",
    backgroundColor: "#fff",
    overflow: "hidden",
    marginVertical: 6,
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
    elevation: 2,
  },
  decorativeBar: {
    height: 48,
    justifyContent: "center",
    alignItems: "flex-end",
    paddingHorizontal: 12,
  },
  statusLabel: {
    fontSize: 12,
    fontWeight: "500",
    paddingHorizontal: 8,
    paddingVertical: 2,
    borderRadius: 12,
    overflow: "hidden",
    textAlign: "center",
  },
  content: {
    padding: 16,
  },
  apoLabel: {
    fontSize: 12,
    color: "#6B7280",
    borderWidth: 1,
    borderColor: "#D1D5DB",
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 12,
    alignSelf: "flex-start",
    marginBottom: 6,
  },
  activity: {
    fontSize: 16,
    fontWeight: "600",
    color: "#111827",
    marginBottom: 4,
  },
  student: {
    fontSize: 14,
    color: "#6B7280",
  },
});