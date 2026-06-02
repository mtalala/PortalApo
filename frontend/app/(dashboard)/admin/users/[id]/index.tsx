import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, Button, Alert, Switch, Pressable } from 'react-native';
import { useRouter, useLocalSearchParams } from 'expo-router';
import { adminUserService, AdminUser } from '@/packages/services/adminUserService';

export default function EditUserScreen() {
  const { id } = useLocalSearchParams<{ id: string | string[] }>();

  const [user, setUser] = useState<AdminUser | null>(null);
  const [availableOrientadores, setAvailableOrientadores] = useState<AdminUser[]>([]);
  const [loading, setLoading] = useState(false);
  const router = useRouter();

  const userId = Array.isArray(id) ? id[0] : id;

  useEffect(() => {
    loadUser();
  }, [userId]);

  const loadUser = async () => {
    if (!userId) return;

    try {
      const data = await adminUserService.getAll();
      setAvailableOrientadores(
        data.filter((u) => u.role?.toUpperCase() === 'ORIENTADOR' && u.ativo)
      );

      const foundUser = data.find((u) => String(u.id) === userId);

      if (foundUser) setUser(foundUser);
    } catch {
      Alert.alert('Erro', 'Falha ao carregar usuário');
    }
  };

  const handleSubmit = async () => {
    if (!user || !userId) return;

    setLoading(true);

    try {
      const payload = {
        ...user,
        orientadorUserIds: user.role?.toUpperCase() === 'ALUNO' ? user.orientadorUserIds ?? [] : [],
      };
      await adminUserService.update(userId, payload);
      Alert.alert('Sucesso', 'Usuário atualizado');
      router.back();
    } catch {
      Alert.alert('Erro', 'Falha ao atualizar usuário');
    } finally {
      setLoading(false);
    }
  };

  if (!user) return <Text>Carregando...</Text>;

  return (
    <View style={{ padding: 20 }}>
      <Text style={{ fontSize: 24, marginBottom: 20 }}>Editar Usuário</Text>

      <TextInput
        placeholder="Username"
        value={user.username}
        onChangeText={(text) => setUser({ ...user, username: text })}
        style={{ borderWidth: 1, padding: 10, marginBottom: 10 }}
      />

      <TextInput
        placeholder="Password (deixe vazio para manter)"
        value={user.password || ''}
        onChangeText={(text) => setUser({ ...user, password: text })}
        secureTextEntry
        style={{ borderWidth: 1, padding: 10, marginBottom: 10 }}
      />

      <TextInput
        placeholder="Role"
        value={user.role}
        onChangeText={(text) => setUser({ ...user, role: text })}
        style={{ borderWidth: 1, padding: 10, marginBottom: 10 }}
      />

      <View style={{ flexDirection: 'row', alignItems: 'center', marginBottom: 20 }}>
        <Text>Ativo: </Text>
        <Switch
          value={user.ativo}
          onValueChange={(value) => setUser({ ...user, ativo: value })}
        />
      </View>

      {user.role?.toUpperCase() === 'ALUNO' && (
        <View style={{ marginBottom: 20 }}>
          <Text style={{ fontWeight: '600', marginBottom: 8 }}>Orientadores vinculados</Text>
          {availableOrientadores.length === 0 ? (
            <Text>Nenhum orientador ativo encontrado.</Text>
          ) : (
            availableOrientadores.map((orientador) => {
              const selected = user.orientadorUserIds?.includes(orientador.id) ?? false;
              return (
                <Pressable
                  key={orientador.id}
                  onPress={() =>
                    setUser((prev) => {
                      if (!prev) return prev;
                      const current = prev.orientadorUserIds ?? [];
                      const updated = current.includes(orientador.id)
                        ? current.filter((id) => id !== orientador.id)
                        : [...current, orientador.id];
                      return { ...prev, orientadorUserIds: updated };
                    })
                  }
                  style={{
                    flexDirection: 'row',
                    alignItems: 'center',
                    padding: 10,
                    borderWidth: 1,
                    borderColor: selected ? '#2563eb' : '#d1d5db',
                    borderRadius: 6,
                    marginBottom: 8,
                    backgroundColor: selected ? '#e0f2fe' : '#fff',
                  }}
                >
                  <Text>{orientador.username}</Text>
                </Pressable>
              );
            })
          )}
        </View>
      )}

      <Button
        title={loading ? 'Atualizando...' : 'Atualizar'}
        onPress={handleSubmit}
        disabled={loading}
      />
    </View>
  );
}