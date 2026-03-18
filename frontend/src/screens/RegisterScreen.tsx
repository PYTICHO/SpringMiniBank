import React, { useState } from "react";
import { Alert, Button, Text, TextInput } from "react-native";

import { useAuth } from "../auth/AuthContext";
import { Screen } from "../components/Screen";

export function RegisterScreen() {
  const { signUp } = useAuth();
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [phone, setPhone] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  async function onSubmit() {
    try {
      await signUp({ firstName, lastName, phone, email, password });
    } catch {
      Alert.alert("Register failed", "Check input data and backend availability.");
    }
  }

  return (
    <Screen>
      <Text>First name</Text>
      <TextInput value={firstName} onChangeText={setFirstName} style={{ borderWidth: 1, padding: 12 }} />
      <Text>Last name</Text>
      <TextInput value={lastName} onChangeText={setLastName} style={{ borderWidth: 1, padding: 12 }} />
      <Text>Phone</Text>
      <TextInput value={phone} onChangeText={setPhone} style={{ borderWidth: 1, padding: 12 }} />
      <Text>Email</Text>
      <TextInput value={email} onChangeText={setEmail} autoCapitalize="none" style={{ borderWidth: 1, padding: 12 }} />
      <Text>Password</Text>
      <TextInput value={password} onChangeText={setPassword} secureTextEntry style={{ borderWidth: 1, padding: 12 }} />
      <Button title="Register" onPress={onSubmit} />
    </Screen>
  );
}
