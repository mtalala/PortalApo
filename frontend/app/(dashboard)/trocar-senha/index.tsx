import React, { useState } from 'react';
import { Alert, Button, Text, TextInput, View } from 'react-native';
import { useRouter } from 'expo-router';
import { useUser } from '@/packages/context/UserContext';

export default function ChangePasswordScreen() {
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const { forceChangePassword } = useUser();
  const router = useRouter();

  const handleSubmit = async () => {
    if (!newPassword || !confirmPassword) {
      Alert.alert('Erro', 'Preencha todos os campos');
      return;
    }

    if (newPassword !== confirmPassword) {
      Alert.alert('Erro', 'A confirmação deve ser igual à nova senha');
      return;
    }

    if (newPassword.length < 6) {
      Alert.alert('Erro', 'A nova senha deve ter pelo menos 6 caracteres');
      return;
    }

    setLoading(true);
    const result = await forceChangePassword(newPassword);
    setLoading(false);

    if (result.success) {
      Alert.alert('Sucesso', 'Senha alterada com sucesso');
      router.replace('/');
      return;
    }

    Alert.alert('Erro', result.message ?? 'Não foi possível alterar a senha');
  };

  return (
    <View style={{ padding: 20, maxWidth: 420, width: '100%' }}>
      <Text style={{ fontSize: 24, marginBottom: 8 }}>Trocar senha</Text>
      <Text style={{ color: '#6b7280', marginBottom: 20 }}>
        Defina uma nova senha para continuar usando o portal.
      </Text>

      <TextInput
        placeholder="Nova senha"
        value={newPassword}
        onChangeText={setNewPassword}
        secureTextEntry
        style={{ borderWidth: 1, padding: 10, marginBottom: 10 }}
      />

      <TextInput
        placeholder="Confirmar nova senha"
        value={confirmPassword}
        onChangeText={setConfirmPassword}
        secureTextEntry
        style={{ borderWidth: 1, padding: 10, marginBottom: 20 }}
      />

      <Button
        title={loading ? 'Alterando...' : 'Alterar senha'}
        onPress={handleSubmit}
        disabled={loading}
      />
    </View>
  );
}
