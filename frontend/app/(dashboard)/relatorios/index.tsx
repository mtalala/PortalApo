import React, { useMemo, useState } from "react";
import { FlatList, Pressable, ScrollView, Text, View } from "react-native";

import { useUser } from "@/packages/context/UserContext";
import {
  exportRelatorioPdf,
  getRelatorio,
  type ReportRow,
} from "@/packages/services/relatorioService";

interface ReportOption {
  id: string;
  title: string;
  endpoint: string;
  roles: string[];
}

export default function RelatoriosScreen() {
  const { user } = useUser();
  const [selected, setSelected] = useState<ReportOption | null>(null);
  const [rows, setRows] = useState<ReportRow[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loadingId, setLoadingId] = useState<string | null>(null);

  const reports = useMemo<ReportOption[]>(() => {
    if (!user) return [];
    const userId = user.id;
    return [
      {
        id: "creditos-aluno",
        title: "Créditos do aluno",
        endpoint: `/relatorios/aluno/${userId}/creditos`,
        roles: ["aluno", "coordenador", "secretaria", "admin"],
      },
      {
        id: "historico-aluno",
        title: "Histórico do aluno",
        endpoint: `/relatorios/aluno/${userId}/historico`,
        roles: ["aluno", "orientador", "coordenador", "secretaria", "admin"],
      },
      {
        id: "avaliacoes-orientador",
        title: "Avaliações do orientador",
        endpoint: `/relatorios/orientador/${userId}/avaliacoes`,
        roles: ["orientador", "coordenador", "admin"],
      },
      {
        id: "avaliacoes-comissao",
        title: "Avaliações da comissão",
        endpoint: "/relatorios/comissao/avaliacoes",
        roles: ["comissao", "coordenador", "admin"],
      },
      {
        id: "creditos-por-aluno",
        title: "Créditos por aluno",
        endpoint: "/relatorios/coordenador/creditos-por-aluno",
        roles: ["coordenador", "admin"],
      },
      {
        id: "situacao-alunos",
        title: "Situação dos alunos",
        endpoint: "/relatorios/coordenador/situacao-alunos",
        roles: ["coordenador", "admin"],
      },
      {
        id: "apos-aprovadas",
        title: "APOs aprovadas",
        endpoint: "/relatorios/coordenador/apos-aprovadas",
        roles: ["coordenador", "admin"],
      },
      {
        id: "apos-rejeitadas",
        title: "APOs rejeitadas",
        endpoint: "/relatorios/coordenador/apos-rejeitadas",
        roles: ["coordenador", "admin"],
      },
      {
        id: "historico-creditos",
        title: "Histórico de créditos lançados",
        endpoint: "/relatorios/secretaria/historico-creditos",
        roles: ["secretaria", "admin"],
      },
    ].filter((report) => report.roles.includes(user.role));
  }, [user]);

  const loadReport = async (report: ReportOption) => {
    try {
      setLoadingId(report.id);
      setError(null);
      const data = await getRelatorio(report.endpoint);
      setSelected(report);
      setRows(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Erro ao carregar relatório");
    } finally {
      setLoadingId(null);
    }
  };

  const exportPdf = async () => {
    if (!selected) return;
    try {
      setError(null);
      await exportRelatorioPdf(selected.endpoint, `relatorio_${selected.id}.pdf`);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Erro ao exportar PDF");
    }
  };

  if (!user) return null;

  return (
    <ScrollView contentContainerStyle={{ padding: 16, gap: 16 }}>
      <View>
        <Text style={{ fontSize: 28, fontWeight: "700", color: "#111827" }}>
          Relatórios
        </Text>
        <Text style={{ color: "#6b7280", marginTop: 4 }}>
          {reports.length} relatórios disponíveis
        </Text>
      </View>

      <View style={{ flexDirection: "row", flexWrap: "wrap", gap: 12 }}>
        {reports.map((report) => (
          <Pressable
            key={report.id}
            onPress={() => loadReport(report)}
            style={{
              paddingVertical: 10,
              paddingHorizontal: 14,
              borderRadius: 8,
              backgroundColor: selected?.id === report.id ? "#dc2626" : "#fff",
              borderWidth: 1,
              borderColor: selected?.id === report.id ? "#dc2626" : "#e5e7eb",
            }}
          >
            <Text
              style={{
                color: selected?.id === report.id ? "#fff" : "#111827",
                fontWeight: "700",
              }}
            >
              {loadingId === report.id ? "Carregando..." : report.title}
            </Text>
          </Pressable>
        ))}
      </View>

      {selected && (
        <View style={{ flexDirection: "row", justifyContent: "space-between", gap: 12 }}>
          <Text style={{ fontSize: 18, fontWeight: "700", color: "#111827" }}>
            {selected.title}
          </Text>
          <Pressable
            onPress={exportPdf}
            style={{
              backgroundColor: "#111827",
              borderRadius: 8,
              paddingVertical: 8,
              paddingHorizontal: 12,
            }}
          >
            <Text style={{ color: "#fff", fontWeight: "700" }}>Exportar PDF</Text>
          </Pressable>
        </View>
      )}

      {error && <Text style={{ color: "#dc2626" }}>{error}</Text>}

      <FlatList
        data={rows}
        scrollEnabled={false}
        keyExtractor={(_, index) => String(index)}
        ListEmptyComponent={
          selected ? <Text style={{ color: "#6b7280" }}>Nenhum dado encontrado.</Text> : null
        }
        renderItem={({ item }) => (
          <View
            style={{
              backgroundColor: "#fff",
              borderWidth: 1,
              borderColor: "#e5e7eb",
              borderRadius: 8,
              padding: 12,
              marginBottom: 10,
            }}
          >
            {Object.entries(item).map(([key, value]) => (
              <Text key={key} style={{ color: "#374151", marginBottom: 4 }}>
                <Text style={{ fontWeight: "700" }}>{key}: </Text>
                {String(value)}
              </Text>
            ))}
          </View>
        )}
      />
    </ScrollView>
  );
}
