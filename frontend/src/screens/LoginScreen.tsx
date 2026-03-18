import React, { useState } from "react";
import { Alert, Button, Text, TextInput } from "react-native";
import type { NativeStackScreenProps } from "@react-navigation/native-stack";

import { useAuth } from "../auth/AuthContext";
import { Screen } from "../components/Screen";
import type { RootStackParamList } from "../navigation/RootNavigator";

type Props = NativeStackScreenProps<RootStackParamList, "Login">;

export function LoginScreen({ navigation }: Props) {
  const { signIn } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  async function onSubmit() {
    try {
      await signIn({ email, password });
    } catch {
      Alert.alert("Login failed", "Check your credentials and backend URL.");
    }
  }

  return (
    <Screen>
      <Text>Email</Text>
      <TextInput value={email} onChangeText={setEmail} autoCapitalize="none" style={{ borderWidth: 1, padding: 12 }} />
      <Text>Password</Text>
      <TextInput value={password} onChangeText={setPassword} secureTextEntry style={{ borderWidth: 1, padding: 12 }} />
      <Button title="Login" onPress={onSubmit} />
      <Button title="Go to register" onPress={() => navigation.navigate("Register")} />
    </Screen>
  );
}
