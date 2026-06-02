import React from "react";
import { StyleSheet, Text, View } from "react-native";

interface CreditProgressProps {
  totalCreditos: number;
  minimoExigido?: number;
}

export default function CreditProgress({
  totalCreditos,
  minimoExigido = 12,
}: CreditProgressProps) {
  const percent = Math.min((totalCreditos / minimoExigido) * 100, 100);
  const color =
    totalCreditos >= minimoExigido
      ? "#16a34a"
      : totalCreditos >= 6
        ? "#d97706"
        : "#dc2626";

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Créditos acumulados</Text>
        <Text style={styles.value}>
          {totalCreditos.toFixed(1)} / {minimoExigido} créditos
        </Text>
      </View>
      <View style={styles.track}>
        <View style={[styles.bar, { width: `${percent}%`, backgroundColor: color }]} />
      </View>
      {totalCreditos >= minimoExigido && (
        <Text style={styles.done}>✓ Mínimo atingido</Text>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    width: "100%",
    backgroundColor: "#fff",
    borderRadius: 8,
    borderWidth: 1,
    borderColor: "#e5e7eb",
    padding: 16,
    marginBottom: 24,
  },
  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    gap: 12,
    marginBottom: 12,
  },
  title: {
    color: "#111827",
    fontSize: 14,
    fontWeight: "700",
  },
  value: {
    color: "#4b5563",
    fontSize: 14,
    fontWeight: "600",
  },
  track: {
    height: 10,
    borderRadius: 5,
    backgroundColor: "#e5e7eb",
    overflow: "hidden",
  },
  bar: {
    height: "100%",
    borderRadius: 5,
  },
  done: {
    marginTop: 10,
    color: "#166534",
    fontSize: 13,
    fontWeight: "700",
  },
});
