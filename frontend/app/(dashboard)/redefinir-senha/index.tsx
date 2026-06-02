import React, { useState } from 'react';
import { View, Text, TextInput, Button, Alert, StyleSheet } from 'react-native';
import { useRouter, useLocalSearchParams } from 'expo-router';
import { authService } from '@/packages/services/authService';

export default function ResetPasswordScreen() {
  const params = useLocalSearchParams<{ token?: string }>();
  const token = Array.isArray(params.token) ? params.token[0] : params.token;

  const [novaSenha, setNovaSenha] = useState('');
  const [confirmarSenha, setConfirmarSenha] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const router = useRouter();

  const handleSubmit = async () => {
    if (!token) {
      Alert.alert('Erro', 'Token de redefinição inválido ou ausente. Use o link recebido por e-mail.');
      return;
    }

    if (!novaSenha || !confirmarSenha) {
      Alert.alert('Erro', 'Preencha todos os campos');
      return;
    }

    if (novaSenha !== confirmarSenha) {
      Alert.alert('Erro', 'As senhas não coincidem');
      return;
    }

    if (novaSenha.length < 6) {
      Alert.alert('Erro', 'A nova senha deve ter pelo menos 6 caracteres');
      return;
    }

    setLoading(true);
    try {
      await authService.resetPassword({ token, novaSenha });
      setSuccess(true);
    } catch (error) {
      const msg = error instanceof Error ? error.message : 'Token inválido ou expirado. Solicite um novo link.';
      Alert.alert('Erro', msg);
    } finally {
      setLoading(false);
    }
  };

  if (success) {
    return (
      <View style={styles.container}>
        <Text style={styles.title}>Senha redefinida!</Text>
        <Text style={styles.description}>
          Sua senha foi alterada com sucesso. Faça login com sua nova senha.
        </Text>
        <Button title="Ir para o login" onPress={() => router.replace('/login')} />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Redefinir senha</Text>
      <Text style={styles.description}>
        Digite sua nova senha abaixo.
      </Text>

      <TextInput
        placeholder="Nova senha"
        value={novaSenha}
        onChangeText={setNovaSenha}
        secureTextEntry
        style={styles.input}
      />

      <TextInput
        placeholder="Confirmar nova senha"
        value={confirmarSenha}
        onChangeText={setConfirmarSenha}
        secureTextEntry
        style={styles.input}
      />

      <Button
        title={loading ? 'Salvando...' : 'Redefinir senha'}
        onPress={handleSubmit}
        disabled={loading}
      />

      <Text
        style={styles.link}
        onPress={() => router.replace('/esqueci-senha')}
      >
        Solicitar novo link
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    padding: 20,
    maxWidth: 420,
    width: '100%',
  },
  title: {
    fontSize: 24,
    fontWeight: '600',
    marginBottom: 8,
  },
  description: {
    color: '#6b7280',
    marginBottom: 20,
    lineHeight: 20,
  },
  input: {
    borderWidth: 1,
    borderColor: '#d1d5db',
    borderRadius: 6,
    padding: 10,
    marginBottom: 16,
  },
  link: {
    marginTop: 16,
    color: '#2563eb',
    textAlign: 'center',
  },
});
