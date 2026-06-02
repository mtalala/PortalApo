import { useUser } from "@/packages/context/UserContext";
import React, { useEffect, useState } from "react";
import { ScrollView, Text, useWindowDimensions, View } from "react-native";

import RequestCard from "@/components/dashboard/RequestCard";
import RequestCardSkeleton from "@/components/dashboard/RequestCardSkeleton";

import { getApos } from "@/packages/services/apoService";
import { getApoVisualStatus, getApoVisualStatusForUser } from "@/packages/domain/apoVisualStatus";
import type { Apo } from "@/packages/types/apo";

export default function EmAndamentoScreen() {
  const [apos, setApos] = useState<Apo[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const { width } = useWindowDimensions();
  const { user } = useUser();

  const getColumns = () => {
    if (width >= 1024) return 4;
    if (width >= 768) return 4;
    return 1;
  };

  useEffect(() => {
    let isActive = true;
    getApos()
      .then((data) => {
        if (isActive) setApos(data);
      })
      .catch(() => {
        if (isActive) setApos([]);
      })
      .finally(() => {
        if (isActive) setIsLoading(false);
      });
    return () => {
      isActive = false;
    };
  }, []);

  if (!user) return null;

  const emAndamento = apos.filter(
    (apo) => getApoVisualStatusForUser(apo, user) === "EM_ANDAMENTO"
  );

  return (
    <ScrollView contentContainerStyle={{ padding: 16 }}>
      <View style={{ marginBottom: 24 }}>
        <Text style={{ fontSize: 32, fontWeight: "700" }}>Em andamento</Text>
        <Text style={{ color: "#6b7280" }}>{emAndamento.length} APOs em andamento</Text>
      </View>

      <View style={{ flexDirection: "row", flexWrap: "wrap", gap: 16 }}>
        {isLoading
          ? Array.from({ length: 6 }).map((_, i) => (
              <View key={i} style={{ flexBasis: `${100 / getColumns()}%` }}>
                <RequestCardSkeleton />
              </View>
            ))
          : emAndamento.length === 0
          ? (
            <Text style={{ color: "#9ca3af" }}>Nenhuma APO em andamento.</Text>
          )
          : emAndamento.map((apo) => (
              <View key={apo.id} style={{ flexBasis: `${100 / getColumns()}%` }}>
                <RequestCard apo={apo} currentUser={user} />
              </View>
            ))}
      </View>
    </ScrollView>
  );
}
