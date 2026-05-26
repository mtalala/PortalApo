// src/app/solicitacoes.tsx
import React, { useEffect, useState } from "react";
import {
  Alert,
  FlatList,
  Pressable,
  ScrollView,
  StyleSheet,
  Switch,
  Text,
  TextInput,
  View,
} from "react-native";

import * as DocumentPicker from "expo-document-picker";

import activitiesData from "@/packages/data/activities";
import coordenadoresData from "@/packages/data/coordenadores";
import programsData from "@/packages/data/programs";
import { createApo } from "@/packages/services/apoService";
import { getPrograms } from "@/packages/services/programService";
import type { Activity, Coordenador, Program } from "@/packages/types/types";

type SelectedFile = DocumentPicker.DocumentPickerAsset;

export default function SolicitacoesPage() {
  const [program, setProgram] = useState<number | null>(null);
  const [matricula, setMatricula] = useState("");
  const [nome, setNome] = useState("");
  const [orientador, setOrientador] = useState("");
  const [coordenador, setCoordenador] = useState<number | null>(null);
  const [semestre, setSemestre] = useState("");
  const [codigoApo, setCodigoApo] = useState("");
  const [selectedActivities, setSelectedActivities] = useState<number[]>([]);
  const [selectedFiles, setSelectedFiles] = useState<SelectedFile[]>([]);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [programs, setPrograms] = useState<Program[]>([]);
  const [coordenadores, setCoordenadores] = useState<Coordenador[]>([]);
  const [activities, setActivities] = useState<Activity[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getPrograms()
      .then((data) => setPrograms(data))
      .catch(() => setPrograms(programsData))
      .finally(() => setLoading(false));

    setCoordenadores(coordenadoresData);
    setActivities(activitiesData);
  }, []);

  const toggleActivity = (id: number) => {
    setSelectedActivities((prev) =>
      prev.includes(id) ? prev.filter((a) => a !== id) : [...prev, id]
    );
  };

  const totalPoints = selectedActivities.reduce((acc, id) => {
    const activity = activities.find((a) => a.id === id);
    return acc + (activity?.points ?? 0);
  }, 0);

  const handleFileUpload = async () => {
    const result = await DocumentPicker.getDocumentAsync({ multiple: true });

    if (result.canceled) return;

    setSelectedFiles((prev) => [...prev, ...result.assets]);
  };

  const removeFile = (index: number) => {
    setSelectedFiles((prev) => prev.filter((_, i) => i !== index));
  };

  const handleSubmit = async () => {
    if (
      !program ||
      !coordenador ||
      !matricula ||
      !nome ||
      !orientador ||
      !semestre ||
      !codigoApo
    ) {
      Alert.alert("Erro", "Preencha todos os campos obrigatórios");
      return;
    }

    setIsSubmitting(true);

    const selectedActivitiesPayload = selectedActivities
      .map((id) => activities.find((activity) => activity.id === id))
      .filter((activity): activity is Activity => Boolean(activity))
      .map(({ label, points }) => ({ label, points }));

    const payload = {
      codigoApo,
      matricula,
      nome,
      program: programs.find((p) => p.id === program)?.name ?? "",
      semestre,
      orientador,
      coordenador: coordenadores.find((c) => c.id === coordenador)?.name ?? "",
      status: "PENDENTE_ORIENTADOR" as const,
      dataSubmissao: new Date().toISOString().split("T")[0],
      totalPoints,
      activities: selectedActivitiesPayload,
      files: selectedFiles.map((file) => ({
        name: file.name ?? file.uri.split("/").pop() ?? "arquivo",
        url: file.uri,
      })),
      approvals: [],
      requiredCommissionApprovals: 3,
    };

    try {
      await createApo(payload as any);
      Alert.alert("Sucesso", "Formulário enviado com sucesso.");
      setProgram(null);
      setMatricula("");
      setNome("");
      setOrientador("");
      setCoordenador(null);
      setSemestre("");
      setCodigoApo("");
      setSelectedActivities([]);
      setSelectedFiles([]);
    } catch (error) {
      Alert.alert("Erro", "Não foi possível enviar a solicitação.");
    } finally {
      setIsSubmitting(false);
    }
  };

  if (loading) {
    return <Text style={{ padding: 16 }}>Carregando dados...</Text>;
  }

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.title}>Enviar Nova Solicitação</Text>

      <Text style={styles.label}>Selecionar programa</Text>
      {programs.map((p) => (
        <Pressable
          key={p.id}
          onPress={() => setProgram(p.id)}
          style={[
            styles.option,
            program === p.id && styles.optionActive,
          ]}
        >
          <Text>{p.name}</Text>
        </Pressable>
      ))}

      <Input label="Código de matrícula" value={matricula} onChange={setMatricula} />
      <Input label="Nome" value={nome} onChange={setNome} />
      <Input label="Nome do Orientador" value={orientador} onChange={setOrientador} />
      <Input label="Semestre" value={semestre} onChange={setSemestre} />
      <Input label="Código da APO" value={codigoApo} onChange={setCodigoApo} />

      <Text style={styles.label}>Selecionar Coordenador</Text>
      {coordenadores.map((c) => (
        <Pressable
          key={c.id}
          onPress={() => setCoordenador(c.id)}
          style={[
            styles.option,
            coordenador === c.id && styles.optionActive,
          ]}
        >
          <Text>{c.name}</Text>
        </Pressable>
      ))}

      <Text style={styles.label}>Atividades desenvolvidas</Text>
      {activities.map((a) => {
        const selected = selectedActivities.includes(a.id);
        return (
          <Pressable
            key={a.id}
            onPress={() => toggleActivity(a.id)}
            style={[
              styles.activity,
              selected && styles.optionActive,
            ]}
          >
            <Switch value={selected} />
            <Text style={{ marginLeft: 8 }}>
              {a.label} ({a.points} pts)
            </Text>
          </Pressable>
        );
      })}

      <Text style={styles.total}>Total de pontos: {totalPoints}</Text>

      <Pressable style={styles.upload} onPress={handleFileUpload}>
        <Text style={styles.uploadText}>Anexar arquivos</Text>
      </Pressable>

      {selectedFiles.length > 0 && (
        <FlatList
          data={selectedFiles}
          keyExtractor={(item) => item.uri}
          renderItem={({ item, index }) => (
            <View style={styles.fileItem}>
              <Text style={{ flex: 1 }} numberOfLines={1}>
                {item.name ?? item.uri.split("/").pop()}
              </Text>
              <Pressable onPress={() => removeFile(index)}>
                <Text style={styles.remove}>×</Text>
              </Pressable>
            </View>
          )}
        />
      )}

      <Pressable
        onPress={handleSubmit}
        disabled={isSubmitting}
        style={[
          styles.submit,
          isSubmitting && { opacity: 0.6 },
        ]}
      >
        <Text style={styles.submitText}>
          {isSubmitting ? "Enviando..." : "Enviar"}
        </Text>
      </Pressable>
    </ScrollView>
  );
}

function Input({
  label,
  value,
  onChange,
}: {
  label: string;
  value: string;
  onChange: (v: string) => void;
}) {
  return (
    <View style={{ marginTop: 12 }}>
      <Text style={styles.label}>{label}</Text>
      <TextInput value={value} onChangeText={onChange} style={styles.input} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { padding: 16 },
  title: { fontSize: 28, fontWeight: "bold", marginBottom: 16 },
  label: { fontWeight: "600", marginTop: 12 },
  input: {
    borderWidth: 1,
    borderColor: "#d1d5db",
    borderRadius: 12,
    padding: 12,
    marginTop: 8,
  },
  option: {
    borderWidth: 1,
    borderColor: "#d1d5db",
    borderRadius: 12,
    padding: 12,
    marginTop: 8,
  },
  optionActive: {
    backgroundColor: "#eff6ff",
    borderColor: "#e80000",
  },
  activity: {
    flexDirection: "row",
    alignItems: "center",
    borderWidth: 1,
    borderColor: "#d1d5db",
    borderRadius: 12,
    padding: 12,
    marginTop: 8,
  },
  total: { marginTop: 12, fontWeight: "700" },
  upload: {
    marginTop: 16,
    borderWidth: 1,
    borderColor: "#d1d5db",
    borderRadius: 12,
    padding: 14,
    alignItems: "center",
  },
  uploadText: { color: "#2563eb", fontWeight: "600" },
  fileItem: {
    flexDirection: "row",
    alignItems: "center",
    paddingVertical: 8,
  },
  remove: { color: "#dc2626", fontSize: 18, paddingHorizontal: 12 },
  submit: {
    marginTop: 24,
    backgroundColor: "#00bb41",
    borderRadius: 12,
    paddingVertical: 16,
    alignItems: "center",
  },
  submitText: { color: "#fff", fontWeight: "700" },
});
