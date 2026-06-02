import { router, useLocalSearchParams } from "expo-router";
import React, { useEffect, useState } from "react";
import {
  Linking,
  Platform,
  Pressable,
  SafeAreaView,
  ScrollView,
  Text,
  useWindowDimensions,
  View,
} from "react-native";

import { useUser } from "@/packages/context/UserContext";
import { canApproveApo } from "@/packages/domain/apoRules";
import { getApoStatusColor } from "@/packages/domain/apoStatusColor";
import { getApoStatusLabel } from "@/packages/domain/apoStatusLabel";
import {
  approveApo,
  getApoById,
  rejectApo,
} from "@/packages/services/apoService";
import type { Apo } from "@/packages/types/apo";

export default function ApoViewScreen() {
  const rawId = useLocalSearchParams<{ id?: string | string[] }>().id;
  const id = Array.isArray(rawId) ? rawId[0] : rawId;

  const { user } = useUser();
  const { width } = useWindowDimensions();

  const [apo, setApo] = useState<Apo | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const isDesktop =
    (Platform.OS === "web" && width >= 768) ||
    Platform.OS === "windows" ||
    Platform.OS === "macos";

  useEffect(() => {
    if (!id) {
      setIsLoading(false);
      setError("ID não informado");
      return;
    }

    let isActive = true;

    const load = async () => {
      try {
        setIsLoading(true);
        setError(null);

        const data = await getApoById(id);

        if (isActive) setApo(data);
      } catch (err: any) {
        if (isActive) {
          setError(err?.message ?? "Erro ao carregar APO");
        }
      } finally {
        if (isActive) setIsLoading(false);
      }
    };

    load();

    return () => {
      isActive = false;
    };
  }, [id]);

  const handleApprove = async () => {
    if (!apo || !user?.role) return;

    try {
      const updated = await approveApo(apo.id, user.role);
      setApo(updated);

      router.replace({
        pathname: "/apos/[id]",
        params: { id: apo.id },
      });
    } catch (err: any) {
      setError(err?.message ?? "Não foi possível aprovar");
    }
  };

  const handleReject = async () => {
    if (!apo) return;

    try {
      const updated = await rejectApo(apo.id);
      setApo(updated);

      router.replace({
        pathname: "/apos/[id]",
        params: { id: apo.id },
      });
    } catch (err: any) {
      setError(err?.message ?? "Não foi possível rejeitar");
    }
  };

  const Card = ({ children }: { children: React.ReactNode }) => (
    <View
      style={{
        backgroundColor: "#fff",
        padding: 20,
        marginBottom: 24,
        borderRadius: 12,
        shadowColor: "#000",
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.05,
        shadowRadius: 4,
        elevation: 3,
        width: "100%",
      }}
    >
      {children}
    </View>
  );

  const InfoRow = ({
    label,
    value,
  }: {
    label: string;
    value: string | number;
  }) => (
    <View
      style={{
        width: isDesktop ? "48%" : "100%",
        marginBottom: 12,
      }}
    >
      <Text style={{ fontSize: 12, color: "#6b7280" }}>{label}</Text>
      <Text style={{ fontWeight: "500", color: "#111827", fontSize: 14 }}>
        {value}
      </Text>
    </View>
  );

  if (!user) return null;

  if (!id) {
    return <Text style={{ padding: 24 }}>ID inválido.</Text>;
  }

  if (isLoading) {
    return <Text style={{ padding: 24 }}>Carregando APO...</Text>;
  }

  if (error) {
    return <Text style={{ padding: 24, color: "#dc2626" }}>{error}</Text>;
  }

  if (!apo) {
    return <Text style={{ padding: 24 }}>APO não encontrada.</Text>;
  }

  const allowedToApprove = canApproveApo(apo, user);
  const userApproval = apo.approvals.find(
    (approval) =>
      String(approval.userId) === String(user.id) &&
      approval.role.toLowerCase() === user.role
  );

  return (
    <SafeAreaView style={{ flex: 1 }}>
      <ScrollView
        contentContainerStyle={{
          paddingHorizontal: isDesktop ? 48 : 16,
          paddingTop: 24,
          paddingBottom: 40,
        }}
      >
        <View
          style={{
            flexDirection: isDesktop ? "row" : "column",
            justifyContent: "space-between",
            alignItems: isDesktop ? "center" : "flex-start",
            marginBottom: 24,
          }}
        >
          <View>
            <Text
              style={{
                fontSize: isDesktop ? 32 : 22,
                fontWeight: "700",
                color: "#111827",
                marginBottom: isDesktop ? 0 : 8,
              }}
            >
              APO {apo.codigoApo}
            </Text>
            {userApproval && (
              <View
                style={{
                  marginTop: 8,
                  backgroundColor: userApproval.approved
                    ? "#16a34a"
                    : "#dc2626",
                  paddingHorizontal: 12,
                  paddingVertical: 6,
                  borderRadius: 999,
                  alignSelf: "flex-start",
                }}
              >
                <Text style={{ color: "#fff", fontSize: 12, fontWeight: "600" }}>
                  {userApproval.approved ? "Aprovada" : "Rejeitada"}
                </Text>
              </View>
            )}
          </View>

          <View
            style={{
              backgroundColor: getApoStatusColor(apo.status),
              paddingHorizontal: 14,
              paddingVertical: 6,
              borderRadius: 999,
              marginTop: isDesktop ? 0 : 12,
            }}
          >
            <Text style={{ color: "#fff", fontSize: 12, fontWeight: "600" }}>
              {getApoStatusLabel(apo.status)}
            </Text>
          </View>
        </View>

        <Card>
          <Text style={{ fontWeight: "700", marginBottom: 16 }}>
            Dados principais
          </Text>

          <View style={{ flexDirection: "row", flexWrap: "wrap" }}>
            <InfoRow label="Aluno" value={apo.nome} />
            <InfoRow label="Matrícula" value={apo.matricula} />
            <InfoRow label="Programa" value={apo.program} />
            <InfoRow label="Semestre" value={apo.semestre} />
            <InfoRow label="Orientador" value={apo.orientador} />
            <InfoRow label="Coordenador" value={apo.coordenador} />
          </View>
        </Card>

        <Card>
          <Text style={{ fontWeight: "700", marginBottom: 16 }}>
            Atividades
          </Text>

          {apo.activities.map((a, i) => (
            <Text key={i} style={{ marginBottom: 6 }}>
              • {a.label} — {a.points} pts
            </Text>
          ))}

          <Text style={{ marginTop: 12, fontWeight: "700" }}>
            Total: {apo.totalPoints} pts
          </Text>
        </Card>

        <Card>
          <Text style={{ fontWeight: "700", marginBottom: 16 }}>
            Arquivos
          </Text>

          {apo.files.map((f, i) => (
            <Pressable key={i} onPress={() => Linking.openURL(f.url)}>
              <Text style={{ color: "#2563eb", marginBottom: 6 }}>
                {f.name}
              </Text>
            </Pressable>
          ))}
        </Card>

        {allowedToApprove && (
          <View style={{ flexDirection: "row", gap: 12 }}>
            <Pressable
              onPress={handleApprove}
              style={{
                backgroundColor: "#16a34a",
                padding: 14,
                borderRadius: 8,
                flex: 1,
                alignItems: "center",
              }}
            >
              <Text style={{ color: "#fff" }}>Aprovar</Text>
            </Pressable>

            <Pressable
              onPress={handleReject}
              style={{
                backgroundColor: "#dc2626",
                padding: 14,
                borderRadius: 8,
                flex: 1,
                alignItems: "center",
              }}
            >
              <Text style={{ color: "#fff" }}>Rejeitar</Text>
            </Pressable>
          </View>
        )}
      </ScrollView>
    </SafeAreaView>
  );
}
