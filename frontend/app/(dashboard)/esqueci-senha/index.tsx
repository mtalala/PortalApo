import React, { useState } from 'react';
import { View, Text, TextInput, Button, Alert, StyleSheet } from 'react-native';
import { useRouter } from 'expo-router';
import { authService } from '@/packages/services/authService';

export default function ForgotPasswordScreen() {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [sent, setSent] = useState(false);
  const router = useRouter();

  const isValidEmail = (value: string) => {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
  };

  const handleSubmit = async () => {
    const normalizedEmail = email.trim().toLowerCase();

    if (!normalizedEmail) {
      Alert.alert('Erro', 'Informe seu e-mail');
      return;
    }

    if (!isValidEmail(normalizedEmail)) {
      Alert.alert('Erro', 'Informe um e-mail válido');
      return;
    }

    setLoading(true);
    try {
      await authService.forgotPassword(normalizedEmail);
      setSent(true);
    } catch (error) {
      // Mesmo em caso de erro, exibir mensagem genérica
      setSent(true);
    } finally {
      setLoading(false);
    }
  };

  if (sent) {
    return (
      <View style={styles.container}>
        <Text style={styles.title}>E-mail enviado</Text>
        <Text style={styles.description}>
          Se o endereço informado estiver cadastrado, você receberá um link para redefinir sua senha em breve.
          Verifique também a pasta de spam.
        </Text>
        <Button title="Voltar ao login" onPress={() => router.replace('/login')} />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Esqueci minha senha</Text>
      <Text style={styles.description}>
        Informe o e-mail cadastrado e enviaremos um link para redefinir sua senha.
      </Text>

      <TextInput
        placeholder="seu@email.com"
        value={email}
        onChangeText={setEmail}
        autoCapitalize="none"
        autoCorrect={false}
        keyboardType="email-address"
        textContentType="emailAddress"
        style={styles.input}
      />

      <Button
        title={loading ? 'Enviando...' : 'Enviar link de redefinição'}
        onPress={handleSubmit}
        disabled={loading}
      />

      <Text
        style={styles.link}
        onPress={() => router.replace('/login')}
      >
        Voltar ao login
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
