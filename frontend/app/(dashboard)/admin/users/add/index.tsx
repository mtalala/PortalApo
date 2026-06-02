import React, { useState } from 'react';
import { View, Text, TextInput, Button, Alert, Pressable } from 'react-native';
import { Picker } from '@react-native-picker/picker';
import { useRouter } from 'expo-router';
import { adminUserService, AdminUser } from '@/packages/services/adminUserService';

const ROLE_OPTIONS = ['ADMIN', 'ALUNO', 'COORDENADOR', 'ORIENTADOR', 'COMISSAO'] as const;

export default function AddUserScreen() {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState<typeof ROLE_OPTIONS[number]>('ALUNO');
  const [loading, setLoading] = useState(false);
  const [availableOrientadores, setAvailableOrientadores] = useState<AdminUser[]>([]);
  const [selectedOrientadores, setSelectedOrientadores] = useState<number[]>([]);
  const router = useRouter();

  React.useEffect(() => {
    const loadOrientadores = async () => {
      try {
        const users = await adminUserService.getAll();
        setAvailableOrientadores(
          users.filter((user) => user.role?.toUpperCase() === 'ORIENTADOR' && user.ativo)
        );
      } catch {
        setAvailableOrientadores([]);
      }
    };

    loadOrientadores();
  }, []);

  const toggleOrientador = (id: number) => {
    setSelectedOrientadores((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]
    );
  };

  const handleSubmit = async () => {
    if (!username || !password) {
      Alert.alert('Erro', 'Preencha todos os campos');
      return;
    }

    setLoading(true);
    try {
      await adminUserService.create({
        username,
        email: email.trim() || undefined,
        password,
        role,
        ativo: true,
        orientadorUserIds: role === 'ALUNO' ? selectedOrientadores : [],
      });
      Alert.alert('Sucesso', 'Usuário criado');
      router.back();
    } catch (error) {
      Alert.alert('Erro', 'Falha ao criar usuário');
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={{ padding: 20 }}>
      <Text style={{ fontSize: 24, marginBottom: 20 }}>Adicionar Usuário</Text>
      <TextInput
        placeholder="Username"
        value={username}
        onChangeText={setUsername}
        style={{ borderWidth: 1, padding: 10, marginBottom: 10 }}
      />
      <TextInput
        placeholder="E-mail"
        value={email}
        onChangeText={setEmail}
        autoCapitalize="none"
        keyboardType="email-address"
        style={{ borderWidth: 1, padding: 10, marginBottom: 10 }}
      />
      <TextInput
        placeholder="Password"
        value={password}
        onChangeText={setPassword}
        secureTextEntry
        style={{ borderWidth: 1, padding: 10, marginBottom: 10 }}
      />
      <Text style={{ marginBottom: 8 }}>Role</Text>
      <View style={{ borderWidth: 1, borderRadius: 4, marginBottom: 20 }}>
        <Picker selectedValue={role} onValueChange={setRole} mode="dropdown">
          {ROLE_OPTIONS.map((item) => (
            <Picker.Item key={item} label={item} value={item} />
          ))}
        </Picker>
      </View>

      {role === 'ALUNO' && (
        <View style={{ marginBottom: 20 }}>
          <Text style={{ fontWeight: '600', marginBottom: 8 }}>Vincular orientadores</Text>
          {availableOrientadores.length === 0 ? (
            <Text>Nenhum orientador ativo encontrado.</Text>
          ) : (
            availableOrientadores.map((orientador) => {
              const selected = selectedOrientadores.includes(orientador.id);
              return (
                <Pressable
                  key={orientador.id}
                  onPress={() => toggleOrientador(orientador.id)}
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

      <Button title={loading ? 'Criando...' : 'Criar'} onPress={handleSubmit} disabled={loading} />
    </View>
  );
}
