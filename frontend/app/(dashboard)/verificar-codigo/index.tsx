import React, { useState } from 'react';
import { Alert, Button, Text, TextInput, View } from 'react-native';
import { useRouter, useLocalSearchParams } from 'expo-router';
import { useUser } from '@/packages/context/UserContext';
import { authService } from '@/packages/services/authService';

export default function VerifyCodeScreen() {
  const [code, setCode] = useState('');
  const [loading, setLoading] = useState(false);
  const router = useRouter();
  const params = useLocalSearchParams<{ email?: string }>();
  const email = Array.isArray(params.email) ? params.email[0] : params.email;
  const { user } = useUser();

  const handleVerify = async () => {
    if (!code) {
      Alert.alert('Erro', 'Digite o código de verificação');
      return;
    }

    if (!email && !user?.name) {
      Alert.alert('Erro', 'Email não encontrado');
      return;
    }

    setLoading(true);
    try {
      const result = await authService.verifyCode({
        email: email || user?.name || '',
        code,
      });

      if (result) {
        Alert.alert('Sucesso', 'Código verificado com sucesso');
        // Redirecionar para trocar senha se necessário, senão para home
        if (user?.mustChangePassword) {
          router.replace('/trocar-senha');
        } else {
          router.replace('/');
        }
      }
    } catch (error) {
      Alert.alert('Erro', 'Código inválido ou expirado');
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={{ padding: 20, maxWidth: 420, width: '100%' }}>
      <Text style={{ fontSize: 24, marginBottom: 8 }}>Verificar código</Text>
      <Text style={{ color: '#6b7280', marginBottom: 20 }}>
        Digite o código de verificação enviado para seu email.
      </Text>

      <TextInput
        placeholder="Código de verificação (6 dígitos)"
        value={code}
        onChangeText={setCode}
        keyboardType="numeric"
        maxLength={6}
        style={{ 
          borderWidth: 1, 
          padding: 10, 
          marginBottom: 20,
          fontSize: 18,
          letterSpacing: 2,
          textAlign: 'center',
        }}
      />

      <Button
        title={loading ? 'Verificando...' : 'Verificar código'}
        onPress={handleVerify}
        disabled={loading}
      />
    </View>
  );
}