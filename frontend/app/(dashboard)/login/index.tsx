import React, { useState } from 'react';
import { View, Text, TextInput, Button, Alert } from 'react-native';
import { useRouter } from 'expo-router';
import { useUser } from '@/packages/context/UserContext';
import { authService } from '@/packages/services/authService';

export default function LoginScreen() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useUser();
  const router = useRouter();

  const handleLogin = async () => {
    if (!username || !password) {
      Alert.alert('Erro', 'Preencha todos os campos');
      return;
    }

    setLoading(true);
    try {
      const result = await login(username, password);
      
      if (result.success) {
        // Enviar código de verificação usando o username
        try {
          await authService.sendVerificationCode(username);
          Alert.alert('Sucesso', 'Código de verificação enviado para seu email');
          router.replace({
            pathname: '/verificar-codigo',
            params: { email: username },
          });
        } catch (error) {
          console.error('Erro ao enviar código:', error);
          Alert.alert('Aviso', 'Não foi possível enviar o código, mas você foi logado');
          // Se não conseguir enviar o código, redireciona normalmente
          if (result.mustChangePassword) {
            router.replace('/trocar-senha');
          } else {
            router.replace('/');
          }
        }
      } else {
        Alert.alert('Erro', 'Credenciais inválidas');
      }
    } catch (error) {
      console.error('Erro ao fazer login:', error);
      Alert.alert('Erro', 'Erro ao fazer login');
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={{ padding: 20 }}>
      <Text style={{ fontSize: 24, marginBottom: 20 }}>Login</Text>

      <TextInput
        placeholder="Username"
        value={username}
        onChangeText={setUsername}
        style={{ borderWidth: 1, padding: 10, marginBottom: 10 }}
      />

      <TextInput
        placeholder="Password"
        value={password}
        onChangeText={setPassword}
        secureTextEntry
        style={{ borderWidth: 1, padding: 10, marginBottom: 20 }}
      />

      <Button
        title={loading ? 'Entrando...' : 'Entrar'}
        onPress={handleLogin}
        disabled={loading}
      />
    </View>
  );
}